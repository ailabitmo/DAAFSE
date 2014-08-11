(function(angular){ 'use strict';
    var module = angular.module('metersApp.meters', [
        'metersApp.meters.services', 'ngRDFResource', 'ngSTOMP', 'ngSPARQL', 
        'ngSPARQL.config'
    ]);
    
    module.controller('MeterListCtrl', function($scope, ResourceManager) {
        $scope.meters = [];

        ResourceManager.findByType('em:Mercury230', [
            'em:hasSerialNumber', 'dul:hasLocation/rdfs:label','rdf:type/rdfs:label'
        ])
        .then(function(meters) {
            $scope.meters = meters;
        });
    });
    
    module.controller('MeterPageCtrl', function(
            $scope, $routeParams, ResourceManager) {
        ResourceManager.findByURI($routeParams.meterUri, [
            'dul:hasLocation/rdfs:label', 'em:hasSerialNumber', 'rdf:type/rdfs:label'
        ]).then(function(meters) {
            $scope.meter = meters[0];
        });
    });
    module.controller('MeterChartCtrl', function($scope, $routeParams, 
        ResourceManager, GraphFactory, utils, stomp, metersService) {
        var thisArg = this;
        var sub;
        $scope.vChartConfig = {
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
                { name: 'Фаза 1', data: [] },
                { name: 'Фаза 2', data: [] },
                { name: 'Фаза 3', data: [] }
            ],
            legend: { enabled: true },
            yAxis: { title: { text: 'Напряжение (В)' } },
            xAxis: { type: 'datetime', minRange: 15*60000 },
            loading: true
        };
        $scope.pChartConfig = {
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
                { name: 'Фаза 1', data: [] },
                { name: 'Фаза 2', data: [] },
                { name: 'Фаза 3', data: [] }
            ],
            legend: { enabled: true },
            yAxis: { title: { text: 'Мощность (Вт)' } },
            xAxis: { type: 'datetime', minRange: 15*60000 },
            loading: true
        };
        $scope.fromDate = new Date();
        $scope.untilDate = null;
        
        ResourceManager.findByURI($routeParams.meterUri, ['em:hasStream'])
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
        
        $scope._addObservation = function(chart, observation, length) {
            utils.shiftAndPush(chart.series[0].data, 
                observation[1], length);
            utils.shiftAndPush(chart.series[1].data, 
                        observation[2], length);
            utils.shiftAndPush(chart.series[2].data,
                        observation[3], length);
        };
        $scope._removeObservations = function(chart) {
            chart.series[0].data = [];
            chart.series[1].data = [];
            chart.series[2].data = [];
        };
        $scope.changedDateRange = function() {                        
            thisArg._loadObservations($scope.meter, 
                        toZeroTimeDate($scope.fromDate),
                        toZeroTimeDate($scope.untilDate));
        };
        $scope.$on('$destroy', function() {
            if(sub) {
                sub.then(function(subscription) {
                    subscription.unsubscribe();
                });
            }
        });
        
        this._onMessage = function(message) {
            utils.parseTTL(message.body)
            .then(GraphFactory.newFromTriples)
            .then(function(graph) {
                var points = [];
                var observation = graph.getByType('em:PolyphaseVoltageObservation')[0] ||
                        graph.getByType('em:PolyphasePowerObservation')[0];
                var output = graph.getByURI(observation.get('ssn:observationResult'));
                output['ssn:hasValue'].forEach(function(valueURI){
                    var value = graph.getByURI(valueURI);
                    var phaseNumber = value.get('em:hasPhaseNumber');
                    points[phaseNumber] = [
                        new Date(observation.get('ssn:observationResultTime')).getTime(),
                        parseFloat(value.get('em:hasQuantityValue'))
                    ];
                });
                
                if(observation.is('em:PolyphaseVoltageObservation')) {
                    $scope._addObservation($scope.vChartConfig, points);
                } else {
                    $scope._addObservation($scope.pChartConfig, points);
                }
            });
        };
        this._loadObservations = function(meter, from, till) {
            $scope._removeObservations($scope.vChartConfig);
            $scope._removeObservations($scope.pChartConfig);
            
            $scope.vChartConfig.loading = true;
            $scope.pChartConfig.loading = true;
            
            return metersService.fetchObservations(meter.uri, from, till, 
                ['em:PolyphaseVoltageObservation', 'em:PolyphasePowerObservation'])
                .then(function(points) {
                    Object.getOwnPropertyNames(points).forEach(function(type) {
                        if(type === 'em:PolyphaseVoltageObservation') {
                            utils.addPoints($scope.vChartConfig, points[type]);
                        } else {
                            utils.addPoints($scope.pChartConfig, points[type]);
                        }
                    });
                })
                .then(function() {
                    $scope.vChartConfig.loading = false;
                    $scope.pChartConfig.loading = false;
                });
        };
    });
    
    function toZeroTimeDate(date) {
        return date ? 
                new Date(date.getFullYear(), date.getMonth(), date.getDate())
                : null;
    };
    
})(window.angular);