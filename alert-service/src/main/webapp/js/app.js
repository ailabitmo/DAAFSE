(function(angular) {
    var app = angular.module('metersApp', ['ui.router', 'metersApp-controllers']);

    app.config(function($stateProvider, $urlRouterProvider) {
        $urlRouterProvider.otherwise("/");

        $stateProvider
                .state('index', {
                    url: "/"
                })
                .state('meters', {
                    url: '/meters',
                    templateUrl: 'partials/meters.html',
                    controller: 'MeterListCtrl'
                })
                .state('meters.info', {
                    url: '/{meterUri:.*}/info',
                    templateUrl: 'partials/meters.info.html',
                    controller: 'MeterInfoCtrl'
                })
                .state('alerts', {
                    url: "/alerts"
                });
    });
})(window.angular);