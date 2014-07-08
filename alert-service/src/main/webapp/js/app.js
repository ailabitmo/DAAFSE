(function(angular) {
    var app = angular.module('metersApp', 
        ['ui.router', 'ui.codemirror', 'metersApp.controllers', 'metersApp.alerts']);

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
                .state('meterPage', {
                    url: '/meters/{meterUri:.*}/info',
                    templateUrl: 'partials/meterPage.html',
                    controller: 'MeterInfoCtrl'
                })
                .state('alerts', {
                    url: '/alerts',
                    templateUrl: 'partials/alerts.html',
                    controller: 'AlertListCtrl'
                })
                .state('configure', {
                    url: '/configure',
                    templateUrl: 'partials/configure.html',
                    controller: 'AlertConfigureCtrl'
                });
    });
    
    var generalConfig = angular.module('metersApp.config', []);
    generalConfig.constant('GENERAL_CONFIG', {
        ALERTS_STREAM: 'amqp://192.168.134.114?exchangeName=alert_exchange&routingKey=alerts'
    });
    
    var sparqlConfig = angular.module('ngSPARQL.config', []);
    sparqlConfig.constant(
        'SPARQL_CONFIG', {
            PREFIXES: {
                'rdf': 'http://www.w3.org/1999/02/22-rdf-syntax-ns#',
                'em' : 'http://purl.org/NET/ssnext/electricmeters#',
                'pne': 'http://data.press.net/ontology/event/',
                'ssn': 'http://purl.oclc.org/NET/ssnx/ssn#'
            },
            ENDPOINTS: {
                ENDPOINT_1: "http://192.168.134.114:8890/sparql-cors"
            }
        }
    );
})(window.angular);