(function(angular, console, Date, N3) {
    var app = angular.module('metersApp-controllers', 
        ['metersApp-services', 'highcharts-ng']);

    app.controller('MeterListCtrl', ['$scope', 'sparql', '$state',
        function($scope, sparql, $state) {
            var perPage = 10;
            $scope.selected = $state.params.meterUri;
            $scope.meters = [];
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

            sparql.select("PREFIX em:<http://purl.org/daafse/electricmeters#>\n\
                SELECT ?uri ?serialNumber WHERE {\
                    GRAPH <http://192.168.134.114/SmartMetersDB/> {\
                        ?uri a em:Mercury230 ;\
                            em:hasSerialNumber ?serialNumber .\
                    }\
                }")
                .then(function(meters){
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
        }]);

    app.controller('MeterInfoCtrl', ['$scope', '$stateParams', 'rabbitmq',
        'sparql', '$q',
    function($scope, $stateParams, rabbitmq, sparql, $q) {
        var parser = N3.Parser();
        sparql.select(
        "PREFIX em:<http://purl.org/daafse/electricmeters#>\
        SELECT ?serialNumber ?streamUri WHERE {\
            GRAPH <http://192.168.134.114/SmartMetersDB/> {\
                <http://purl.org/daafse/meters/mercury230_16824038> em:hasSerialNumber ?serialNumber ;\
                    em:hasStream ?streamUri .\
            }\
        }").then(function(meters){
            var length = 10;
            $scope.meter = meters[0];
            $scope.meter.uri = $stateParams.meterUri;
            
            var VALUE = "http://purl.org/daafse/electricmeters#PolyphaseVoltageValue";
            var HASPHASENUMBER = "http://purl.org/daafse/electricmeters#hasPhaseNumber";
            var HASQUANTITYVALUE = "http://purl.org/daafse/electricmeters#hasQuantityValue";
            
            rabbitmq.subscribe($scope.meter.streamUri, function(message) {
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
                        addPoint($scope.chartConfig.series[0].data, length, 
                                    observation[1]);
                        addPoint($scope.chartConfig.series[1].data, length, 
                                    observation[2]);
                        addPoint($scope.chartConfig.series[2].data, length, 
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
                    text: 'V (volt)'
                }
            },
            xAxis: {
                type: 'datetime'
            },
            title: {
                text: $scope.meter?$scope.meter.uri:""
            }
        };
        
        function addPoint(array, length, point) {
            if(array.length > length) {
                array.shift();
            }
            array.push(point);
        };
    }]);

    app.controller('AlertCtrl', ['$scope', 'settings', 'sparql',
        function($scope, settings, sparql) {
        $scope.query = "";
        settings.prefixes.forEach(
        function(e){
            $scope.query += "PREFIX " + e.prefix + ": <" + e.uri + ">\n";
        });
            
        $scope.editorOptions = {
            lineNumbers: true,
            lineWrapping: true,
            mode: 'application/x-sparql-query'
        };
        
        $scope.register = function() {
            sparql.register($scope.query).catch(function(status) {
                alert('[ERROR][AlertCtrl] HTTP status: ' + status);
            });
        };
    }]);
})(window.angular, window.console, window.Date, window.N3);