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
    
    module.controller('AlertListCtrl', function(
            $scope, stomp, GENERAL_CONFIG, $modal, 
            utils, ResourceFactory, ResourceManager) {
        var sub;
        var modal = $modal({
            scope: $scope, placement: 'center', 
            template: 'partials/alert-info.html', show: false
        });
        $scope.alerts = [];
        $scope._onAlert = function(message) {
            utils.parseTTL(message.body)
            .then(ResourceFactory.newFromTriples)
            .then(function(alert) {
                var index = -1;
                $scope.alerts.every(function(v, i) {
                    if(v['dul:involvesAgent'][0] === alert['dul:involvesAgent'][0] 
                            && v['rdf:type'][0] === alert['rdf:type'][0]) {
                        index = i;
                        return false;
                    }
                    return true;
                });
                if(index > -1) {
                    //The similar alert has been found
                } else {
                    $scope.alerts.push(alert);
                }
            });
        };
        $scope.remove = function(index) {
            $scope.alerts.splice(index, 1);
        };
        $scope.onClick = function(index) {
            $scope.selected = $scope.alerts[index];
            modal.$promise.then(modal.show);
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