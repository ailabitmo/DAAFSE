(function(angular) {
    var app = angular.module('metersApp', ['ngRoute', 'metersApp-controllers']);

    app.config(['$routeProvider', function($routeProvider) {
        $routeProvider
            .when('/meters/:meterUri*/\info', {
                templateUrl: 'partials/meter-info.html',
                controller: 'MeterInfoCtrl'
            })
            .otherwise({
                redirectTo: '/'
            });
    }]);
})(window.angular);