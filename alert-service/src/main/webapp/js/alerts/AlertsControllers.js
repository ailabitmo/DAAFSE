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
        utils, Resource) {
        $scope.alerts = [];
        $scope._onAlert = function(message) {
            utils.parseTTL(message.body)
            .then(Resource.prototype.fromTriples)
            .then(function(alert) {
                $scope.alerts.push(alert);
            });
        };
        var sub = stomp.subscribe(GENERAL_CONFIG.ALERTS_STREAM, $scope._onAlert);
        
        $scope.$on('$destroy', function () {
            sub.then(function(subscription) {
                subscription.unsubscribe();
            });
        });
    });
})(window.angular);