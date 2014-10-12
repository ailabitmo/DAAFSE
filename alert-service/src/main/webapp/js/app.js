(function(angular) {
    var app = angular.module('metersApp', [
        'ngRoute', 'ui.codemirror', 'metersApp.alerts', 'metersApp.meters', 
        'mgcrea.ngStrap', 'highcharts-ng'
    ]);

    app.config(function($routeProvider) {
        $routeProvider
            .when('/main', {
                templateUrl: 'partials/main.html' 
            })
            .when('/configure', {
                templateUrl: 'partials/configure.html',
                controller: 'AlertConfigureCtrl'
            })
            .when('/meters/:meterUri*\/info', {
                templateUrl: 'partials/meter-details.html',
                controller: 'MeterPageCtrl'
            })
            .otherwise({
                redirectTo: '/main'
            });
    });
    app.config(function($datepickerProvider){
        angular.extend($datepickerProvider.defaults, {
            autoclose: true,
            dateFormat: 'longDate'
        });
    });
    
    var generalConfig = angular.module('metersApp.config', []);
    generalConfig.constant('GENERAL_CONFIG', {
        ALERTS_STREAM: 'amqp://lpmstreams.tk?exchangeName=alert_exchange&routingKey=alerts'
    });
    
    var sparqlConfig = angular.module('ngSPARQL.config', []);
    sparqlConfig.constant(
        'SPARQL_CONFIG', {
            PREFIXES: {
                'rdf': 'http://www.w3.org/1999/02/22-rdf-syntax-ns#',
                'rdfs': 'http://www.w3.org/2000/01/rdf-schema#',
                'em' : 'http://purl.org/NET/ssnext/electricmeters#',
                'pne': 'http://data.press.net/ontology/event/',
                'ssn': 'http://purl.oclc.org/NET/ssnx/ssn#',
                'xsd': 'http://www.w3.org/2001/XMLSchema#',
                'dul': 'http://www.loa-cnr.it/ontologies/DUL.owl#'
            },
            ENDPOINTS: {
                ENDPOINT_1: "http://lpmanalytics.tk/sparql-cors",
                ENDPOINT_2: "http://lpmanalytics.tk:3030/sparql-cors"
            }
        }
    );
})(window.angular);