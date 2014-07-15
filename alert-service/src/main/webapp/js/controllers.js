(function(angular, console, Date, N3) {
    var app = angular.module('metersApp.controllers', [
        'ngSTOMP', 'ngSPARQL', 'ngRDFResource', 'highcharts-ng', 
        'metersApp.config', 'metersApp.utils'
    ]);

    app.controller('MeterListCtrl', function(
        $scope, $state, ResourceManager) {
        $scope.selected = $state.params.meterUri;
        $scope.meters = [];

        $scope.setSelected = function(setId) {
            $scope.selected = setId;
        };
        $scope.isSelected = function(checkId) {
            return $scope.selected === checkId;
        };

        ResourceManager.findByType('em:Mercury230', ['em:hasSerialNumber'])
        .then(function(meters) {
            $scope.meters = meters;
        });
    });

    app.controller('MeterPageCtrl', 
    function($scope, $stateParams, ResourceManager) {
        ResourceManager.findByURI($stateParams.meterUri, [
            'em:hasStream', 'em:hasSerialNumber'
        ]).then(function(meters) {
            $scope.meter = meters[0];
        });
    });

})(window.angular, window.console, window.Date, window.N3);