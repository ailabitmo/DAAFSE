(function(angular, console) {
    var metersApp = angular.module('metersApp-controllers', 
        ['metersApp-services', 'highcharts-ng']);

    metersApp.controller('MeterListCtrl', ['$http', 'cache', function($http, cache) {
            this.selected = 0;
            
            var store = this;
            store.meters = [];
            
            this.setSelected = function(setId) {
                this.selected = setId;
            };
            this.isSelected = function(checkId) {
                return this.selected === checkId;
            };

            var endpoint = "http://192.168.134.114:8890/sparql";
            var sparql = "PREFIX em:<http://purl.org/daafse/electricmeters#>\n\
                SELECT ?uri ?serialNumber ?streamURI WHERE {\
                    GRAPH <http://192.168.134.114/SmartMetersDB/> {\
                        ?uri a em:Mercury230 ;\
                            em:hasStream ?streamURI ;\
                            em:hasSerialNumber ?serialNumber .\
                    }\
                }";
            $http.get(endpoint,
                    {
                        params: {query: sparql, output: "json"},
                        headers: {Accept: "application/sparql-results+json"}
                    }).success(function(data) {
                data.results.bindings.forEach(function(element) {
                    var meter = {
                        uri: element.uri.value,
                        serialNumber: element.serialNumber.value,
                        streamURI: element.streamURI.value
                    };
                    store.meters.push(meter);
                    cache.write(meter.uri, meter);
                });
            }).error(function(data, status) {
                store.meters = [];
                alert("[ERROR] HTTP Status: " + status);
            });
        }]);

    metersApp.controller('MeterInfoCtrl', ['$scope', '$routeParams', 'rabbitmq',
        'cache',
    function($scope, $routeParams, rabbitmq, cache) {
        $scope.meter = cache.read($routeParams.meterUri);
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
                text: $scope.meter.uri
            }
        };
        
        var OBSERVATION = "http://purl.org/daafse/electricmeters#PolyphaseVoltageObservation";
        var OUTPUT = "http://purl.org/daafse/electricmeters#PolyphaseVoltageSensorOutput";
        var VALUE = "http://purl.org/daafse/electricmeters#PolyphaseVoltageValue";
        var HASPHASENUMBER = "http://purl.org/daafse/electricmeters#hasPhaseNumber";
        var HASQUANTITYVALUE = "http://purl.org/daafse/electricmeters#hasQuantityValue";
        
        rabbitmq.subscribe($scope.meter.streamURI, function(message) {
            var json = JSON.parse(message.body);
            var observation = [];
            var time = Date.now();
            
            json['@graph'].forEach(function(e) {
                if(e['@type'] === VALUE) {
                    observation[e[HASPHASENUMBER]] = [
                        time, e[HASQUANTITYVALUE]
                    ];
                }
            });
            var length = 10;
            
            addPoint($scope.chartConfig.series[0].data, length, observation[1]);
            addPoint($scope.chartConfig.series[1].data, length, observation[2]);
            addPoint($scope.chartConfig.series[2].data, length, observation[3]);
            
            $scope.$apply();
        });
        
        function addPoint(array, length, point) {
            if(array.length > length) {
                array.shift();
            }
            array.push(point);
        };
    }]);
})(window.angular, window.console);