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
        
        $scope.register = function() {
            $scope.selected = null;
            sparql.register($scope.name, $scope.query).catch(function(status) {
                alert('[ERROR][AlertCtrl] HTTP status: ' + status);
            });
        };
        
        $http.get('rest/query').success(function(data){
            $scope.queries = data;
        });
    });
    
    module.controller('AlertListCtrl', function($scope, stomp, GENERAL_CONFIG, 
        utils, Resource) {
        $scope.alerts = [];
        
        this._onAlert = function(message) {
            utils.parseTTL(message.body)
            .then(Resource.fromTriples)
            .then(function(alert) {
                alert['time'] = Date.now();
                $scope.$apply(function() {
                    utils.shiftAndPush($scope.alerts, 20, alert);
                });
            });
        };
        
        stomp.subscribe(GENERAL_CONFIG.ALERTS_STREAM, this._onAlert);
    });
})(window.angular);