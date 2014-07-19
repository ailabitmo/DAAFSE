(function(angular){ 'use strict';
    var module = angular.module('metersApp.meters', [
        'ngRDFResource', 'ngSTOMP', 'ngSPARQL', 'ngSPARQL.config'
    ]);
    
    module.controller('MeterPageCtrl', function(
            $scope, $stateParams, ResourceManager) {
        ResourceManager.findByURI($stateParams.meterUri, [
            'em:hasStream', 'em:hasSerialNumber'
        ]).then(function(meters) {
            $scope.meter = meters[0];
        });
    });
    
    module.controller('MeterChartCtrl', function($scope, $stateParams, 
        ResourceManager, GraphFactory, utils, stomp, sparql, SPARQL_CONFIG) {
        var thisArg = this;
        var sub;
        $scope.chartConfig = {
            options: {
                chart: { type: 'line', zoomType: 'x'},
                rangeSelector: {
                    inputEnabled: false,
                    enabled: true,
                    buttons: [
                        {
                            type: 'minute',
                            count: 60,
                            text: '1h'
                        },
                        {
                            type: 'day',
                            count: 1,
                            text: '1d'
                        },
                        {
                            type: 'all',
                            text: 'All'
                        }
                    ]
                }
            },
            useHighStocks: true,
            series: [
                { name: 'Phase 1', data: [] },
                { name: 'Phase 2', data: [] },
                { name: 'Phase 3', data: [] }
            ],
            legend: { enabled: true },
            yAxis: { title: { text: 'Voltage (V)' } },
            xAxis: { type: 'datetime', minRange: 15*60000 },
            loading: true
        };
        $scope.fromDate = new Date();
        $scope.untilDate = null;
        
        ResourceManager.findByURI($stateParams.meterUri, ['em:hasStream'])
        .then(function(meters) {
            return $scope.meter = meters[0];
        })
        .then(function(meter) {
            return thisArg._loadObservations(meter, 
                            toZeroTimeDate($scope.fromDate), 
                            toZeroTimeDate($scope.untilDate));
        })
        .then(function() {
            sub = stomp.subscribe($scope.meter.get('em:hasStream'), 
                thisArg._onMessage);
        });
        
        $scope._addObservation = function(observation, length) {
            utils.shiftAndPush($scope.chartConfig.series[0].data, 
                observation[1], length);
            utils.shiftAndPush($scope.chartConfig.series[1].data, 
                        observation[2], length);
            utils.shiftAndPush($scope.chartConfig.series[2].data,
                        observation[3], length);
        };
        $scope._removeObservations = function() {
            $scope.chartConfig.series[0].data = [];
            $scope.chartConfig.series[1].data = [];
            $scope.chartConfig.series[2].data = [];
        };
        $scope.changedDateRange = function() {                        
            thisArg._loadObservations($scope.meter, 
                        toZeroTimeDate($scope.fromDate),
                        toZeroTimeDate($scope.untilDate));
        };
        $scope.$on('$destroy', function() {
            sub.then(function(subscription) {
                subscription.unsubscribe();
            });
        });
        
        this._onMessage = function(message) {
            utils.parseTTL(message.body)
            .then(GraphFactory.newFromTriples)
            .then(function(graph) {
                var points = [];
                var observation = graph.getByType('em:PolyphaseVoltageObservation')[0];
                var output = graph.getByURI(observation.get('ssn:observationResult'));
                output['ssn:hasValue'].forEach(function(valueURI){
                    var value = graph.getByURI(valueURI);
                    var phaseNumber = value.get('em:hasPhaseNumber');
                    points[phaseNumber] = [
                        new Date(observation.get('ssn:observationResultTime')).getTime(),
                        parseFloat(value.get('em:hasQuantityValue'))
                    ];
                });
                
                $scope._addObservation(points);
            });
        };
        this._loadObservations = function(meter, from, till) {
            $scope._removeObservations();
            var query = "DESCRIBE ?observation ?output ?value\n\
                    WHERE {\n\
                        GRAPH <" + meter.uri + "> {\n\
                            ?observation a em:PolyphaseVoltageObservation ;\n\
                                ssn:observationResultTime ?time ;\n\
                                ssn:observationResult ?output ;\n\
                                ssn:observedBy <" + meter.uri + "> .\n\
                            ?output ssn:hasValue ?value .\n";
            if(from) {
                query += "FILTER(?time >= '" + from.toISOString() + "'^^xsd:dateTime";
                if(till) {
                    query += "&& ?time <= '" + till.toISOString() + "'^^xsd:dateTime";
                }
                query += ")\n";
            }
            query += "}} ORDER BY ?time";
            $scope.chartConfig.loading = true;
            return sparql.describe(query, SPARQL_CONFIG.ENDPOINTS.ENDPOINT_2)
            .then(function(graph) {
                var observations = graph.getByType('em:PolyphaseVoltageObservation');
                if(observations.length > 0) {
                    var TZoffset = new Date(observations[0].get('ssn:observationResultTime')).getTimezoneOffset();
                    Highcharts.setOptions({
                        global : {
                            timezoneOffset: TZoffset
                        }
                    });
                }
                observations.sort(function(a, b) {
                    var ta = a.get('ssn:observationResult');
                    var tb = b.get('ssn:observationResult');
                    if(ta < tb) {
                        return -1;
                    } else if(ta > tb) {
                        return 1;
                    }
                    return 0;
                });
                observations.forEach(function(observation) {
                    var points = [];
                    var output = graph.getByURI(observation.get('ssn:observationResult'));
                    output['ssn:hasValue'].forEach(function(valueURI) {
                        var value = graph.getByURI(valueURI);
                        var phaseNumber = value.get('em:hasPhaseNumber');
                        points[phaseNumber] = [
                            new Date(observation.get('ssn:observationResultTime')).getTime(),
                            parseFloat(value.get('em:hasQuantityValue'))
                        ];
                    });
                    $scope._addObservation(points);
                }, thisArg);
                $scope.chartConfig.loading = false;
            });
        };
    });
    
    function toZeroTimeDate(date) {
        return date ? 
                new Date(date.getFullYear(), date.getMonth(), date.getDate())
                : null;
    };
    
})(window.angular);