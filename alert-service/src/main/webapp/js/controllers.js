(function(angular, console, Date, N3) {
    var app = angular.module('metersApp.controllers', [
        'ngSTOMP', 'ngSPARQL', 'rdflib.models', 'highcharts-ng', 
        'metersApp.config', 'metersApp.utils'
    ]);

    app.controller('MeterListCtrl', function(
        $scope, $state, stomp, Resource, ResourceManager, utils, 
        GENERAL_CONFIG) {
        var perPage = 10;
        $scope.selected = $state.params.meterUri;
        $scope.meters = [];
        $scope.alerts = [];
        $scope.indexes = [0, undefined];

        $scope.setSelected = function(setId) {
            $scope.selected = setId;
        };
        $scope.isSelected = function(checkId) {
            return $scope.selected === checkId;
        };
        $scope.hasPrev = function() {
            return $scope.indexes[0] >= perPage;
        };
        $scope.hasNext = function() {
            return $scope.indexes[1]?
                        $scope.indexes[1] < $scope.meters.length : true;
        };
        $scope.next = function() {
            $scope.indexes[0] += perPage;
            if($scope.indexes[1] + perPage <= $scope.meters.length) {
                $scope.indexes[1] += perPage;
            } else {
                $scope.indexes[1] = $scope.meters.length;
            }
        };
        $scope.prev = function() {
            var diff = $scope.indexes[1] - $scope.indexes[0];
            $scope.indexes[0] -= perPage;
            if(diff < perPage) {
                $scope.indexes[1] -= diff;
            } else {
                $scope.indexes[1] -= perPage;
            }
        };

        ResourceManager.findByType('em:Mercury230', ['em:hasSerialNumber'])
        .then(function(meters) {
            $scope.meters = meters;
            if($scope.meters.length < perPage) {
                $scope.indexes[1] = $scope.meters.length;
            } else {
                $scope.indexes[1] = perPage;
            }
        }, function(status){
            $scope.meters = [];
            $scope.indexes = [0, undefined];
            alert("[ERROR] HTTP Status: " + status);
        });
        
        $scope._onAlert = function(message) {
            utils.parseTTL(message.body)
            .then(Resource.fromTriples)
            .then(function(alert) {
                if(alert['dul:involvesAgent'] === $scope.selected) {
                    $scope.$apply(function() {
                        utils.shiftAndPush($scope.alerts, 8, alert);
                    });
                }
            });
        };
        stomp.subscribe(GENERAL_CONFIG.ALERTS_STREAM, $scope._onAlert);
    });

    app.controller('MeterInfoCtrl', 
    function($scope, $stateParams, stomp, ResourceManager) {
        
        var parser = N3.Parser();
        ResourceManager.findByURI($stateParams.meterUri, [
            'em:hasStream', 'em:hasSerialNumber'
        ]).then(function(meters) {
            var length = 10;
            $scope.meter = meters[0];
            $scope.meter.uri = $stateParams.meterUri;
            
            var VALUE = "http://purl.org/NET/ssnext/electricmeters#PolyphaseVoltageValue";
            var HASPHASENUMBER = "http://purl.org/NET/ssnext/electricmeters#hasPhaseNumber";
            var HASQUANTITYVALUE = "http://purl.org/NET/ssnext/electricmeters#hasQuantityValue";
            
            stomp.subscribe($scope.meter['em:hasStream'], function(message) {
                var observation = [];
                var time = Date.now();
                var store = N3.Store();
                var N3Util = N3.Util;
                parser.parse(message.body, function(error, triple, prefixes) {
                    if(triple) {
                        store.addTriple(triple.subject, triple.predicate, triple.object);
                    } else {
                        var values = store.find(null, null, VALUE);
                        values.forEach(function(triple) {
                            var phase = store.find(triple.subject, HASPHASENUMBER, null)[0];
                            var value = store.find(triple.subject, HASQUANTITYVALUE, null)[0];
                            observation[N3Util.getLiteralValue(phase.object)] = 
                                    [time, parseFloat(N3Util.getLiteralValue(value.object))];
                        });
                        shiftAndPush($scope.chartConfig.series[0].data, length, 
                                    observation[1]);
                        shiftAndPush($scope.chartConfig.series[1].data, length, 
                                    observation[2]);
                        shiftAndPush($scope.chartConfig.series[2].data, length, 
                                    observation[3]);

                        $scope.$apply();
                    }
                });
            });
        });
        $scope.events = [];
        $scope.chartConfig = {
            options : {
                chart: {
                    type: 'line'
                }
            },
            series: [
                {
                    name: 'Phase 1',
                    data: []
                },
                {
                    name: 'Phase 2',
                    data: []
                },
                {
                    name: 'Phase 3',
                    data: []
                }
            ],
            yAxis: {
                title: {
                    text: 'Voltage (V)'
                }
            },
            xAxis: {
                type: 'datetime'
            },
            title: {
                text: $scope.meter?$scope.meter.uri:""
            },
            tooltip: {
                shared: true,
                crosshairs: true
            }
        };
    });

})(window.angular, window.console, window.Date, window.N3);