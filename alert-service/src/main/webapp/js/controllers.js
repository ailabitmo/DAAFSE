(function(angular, console, Date) {
    var metersApp = angular.module('metersApp-controllers', 
        ['metersApp-services', 'highcharts-ng']);

    metersApp.controller('MeterListCtrl', ['$scope', 'sparql', function($scope, sparql) {
            $scope.meters = [];
            
            this.setSelected = function(setId) {
                $scope.selected = setId;
            };
            this.isSelected = function(checkId) {
                return $scope.selected === checkId;
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
                }, function(status){
                    $scope.meters = [];
                    alert("[ERROR] HTTP Status: " + status);
                });
        }]);

    metersApp.controller('MeterInfoCtrl', ['$scope', '$stateParams', 'rabbitmq',
        'sparql',
    function($scope, $stateParams, rabbitmq, sparql) {
        sparql.select(
        "PREFIX em:<http://purl.org/daafse/electricmeters#>\
        SELECT ?serialNumber ?streamUri WHERE {\
            GRAPH <http://192.168.134.114/SmartMetersDB/> {\
                <http://purl.org/daafse/meters/mercury230_16824038> em:hasSerialNumber ?serialNumber ;\
                    em:hasStream ?streamUri .\
            }\
        }").then(function(meters){
            $scope.meter = meters[0];
            $scope.meter.uri = $stateParams.meterUri;
            
            var VALUE = "http://purl.org/daafse/electricmeters#PolyphaseVoltageValue";
            var HASPHASENUMBER = "http://purl.org/daafse/electricmeters#hasPhaseNumber";
            var HASQUANTITYVALUE = "http://purl.org/daafse/electricmeters#hasQuantityValue";
            
            rabbitmq.subscribe($scope.meter.streamUri, function(message) {
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
})(window.angular, window.console, window.Date);