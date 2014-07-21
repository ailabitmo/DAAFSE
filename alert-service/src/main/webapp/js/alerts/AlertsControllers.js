(function(angular){ 'use strict';
    var module = angular.module('metersApp.alerts', [
        'metersApp.config', 'metersApp.utils', 'ngRDFResource'
    ]);
    
    module.controller('AlertConfigureCtrl', function($scope, sparql, $http) {
        $scope.queries = [];
        $scope.query = sparql.compilePrefixesString();        
        $scope.editorOptions = {
            lineNumbers: true,
            lineWrapping: true,
            mode: 'application/x-sparql-query'
        };
        $scope.setSelected = function(selected) {
          $scope.selected = selected;
          $scope.name = selected.name;
          $scope.query = selected.query;
        };
        $scope.isSelected = function(query) {
          return $scope.selected === query;
        };
        $scope.register = function(isValid) {
            if(isValid) {
                $scope.selected = null;
                sparql.register($scope.name, $scope.query);
            }
        };
        $http.get('rest/query').success(function(data){
            $scope.queries = data;
        });
    });
    
    module.controller('AlertListCtrl', function($scope, stomp, GENERAL_CONFIG, 
        utils, ResourceFactory, ResourceManager) {
        var sub;
        var alertsMap = {};
        $scope.alerts = [];
        $scope.getTypeClass = function(alert) {
            switch(alert.get('rdf:type/dul:isClassifiedBy')) {
                case "http://purl.org/daafse/alerts/types#Info":
                    return 'alert-info';
                case "http://purl.org/daafse/alerts/types#Warning":
                    return 'alert-warning';
                case "http://purl.org/daafse/alerts/types#Danger":
                    return 'alert-danger';
                default:
                    return 'alert-success';
            };
        };
        $scope._onAlert = function(message) {
            utils.parseTTL(message.body)
            .then(ResourceFactory.newFromTriples)
            .then(function(alert) {
                var meterUri = alert.get('dul:involvesAgent');
                if(!alertsMap.hasOwnProperty(meterUri)) {
                    alertsMap[meterUri] = {};
                }
                var type = alert.get('rdf:type');
                var card = alertsMap[meterUri][type] || { index: -1 };
                if(card.index > 0) {
                    $scope.alerts[card.index].count++;
                } else {
                    alert.count = alert.count ? alert.count++ : 1;
                    var index = $scope.alerts.push(alert) - 1;
                    alertsMap[meterUri][type] = {
                        index: index
                    };
                }
            });
        };
        //Pre-cache alert types
        ResourceManager.findByType('dul:Event', [
            'rdfs:label', 'rdfs:comment', 'dul:isClassifiedBy'
        ]).then(function() {
            return ResourceManager.findByType('dul:EventType', ['rdfs:label']);
        }).then(function() {
            sub = stomp.subscribe(GENERAL_CONFIG.ALERTS_STREAM, $scope._onAlert);
        });
        
        $scope.$on('$destroy', function () {
            sub.then(function(subscription) {
                subscription.unsubscribe();
            });
        });
    });
})(window.angular);