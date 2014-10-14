(function(angular){ 'use strict';
    var module = angular.module('metersApp.alerts', [
        'metersApp.config', 'metersApp.utils', 'metersApp.meters.services', 
        'ngRDFResource'
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
                sparql.register($scope.name, $scope.query)
                .then(function() {
                    alert('Query sucessfully registered!');
                }, function() {
                    alert('Registration of query failed! Take a look at the logs.');
                }).then(function() {
                    return $http.get('rest/query').success(function(data) {
                        $scope.queries = data;
                    });
                });
            }
        };
        $http.get('rest/query').success(function(data){
            $scope.queries = data;
        });
    });
    
    module.controller('AlertListCtrl', function(
            $scope, stomp, GENERAL_CONFIG, $modal, $window,
            utils, ResourceFactory, ResourceManager, metersService) {
        var sub;
        var modal = $modal({
            scope: $scope, placement: 'center', 
            template: 'partials/alert-info.html', show: false
        });
        $scope.chart = {
            options: {
                chart: { type: 'line', zoomType: 'x'},
                rangeSelector: { enabled: false }
            },
            useHighStocks: true,
            series: [
                { name: 'Фаза 1', data: [] },
                { name: 'Фаза 2', data: [] },
                { name: 'Фаза 3', data: [] }
            ],
            legend: { enabled: true },
            yAxis: { title: { text: 'Напряжение (В)' } },
            xAxis: { type: 'datetime', minRange: 15*60000 },
            loading: true
        };
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
                    $scope.alerts[index].lastTime = alert.get('dul:hasEventDate');
                } else {
                    alert.lastTime = alert.get('dul:hasEventDate');
                    $scope.alerts.push(alert);
                }
            });
        };
        $scope.remove = function(index) {
            $scope.alerts.splice(index, 1);
        };
        $scope.onClick = function(index) {
            $scope.selected = $scope.alerts[index];
            modal.$promise
                .then(modal.show)
                .then(function() {
                    $window.dispatchEvent(new Event('resize'));
                    $scope.chart.loading = true;
                    $scope.from = new Date(new Date(
                            $scope.selected.get('dul:hasEventDate')).getTime() - 10*60000);
                    $scope.till = new Date(new Date(
                            $scope.selected.lastTime).getTime() + 10*60000);
                    return metersService.fetchObservation(
                            $scope.selected.get('dul:involvesAgent'),
                            $scope.selected.get('rdf:type/dul:includesEvent'),
                            $scope.from,
                            $scope.till)
                    .then(function(points) {
                        utils.addPoints($scope.chart, points);
                    });
                })
                .then(function() {
                    $scope.chart.loading = false;
                    $window.dispatchEvent(new Event('resize'));
                });
        };
        //Pre-cache alert types
        ResourceManager.findByType('dul:Event', [
            'rdfs:label', 'rdfs:comment', 'dul:isClassifiedBy', 'dul:includesEvent'
        ]).then(function() {
            return ResourceManager.findByType('dul:EventType', ['rdfs:label']);
        }).then(function() {
            sub = stomp.subscribe(GENERAL_CONFIG.ALERTS_STREAM, $scope._onAlert);
        });
        
        $scope.$on('$routeChangeStart', function() {
            if(modal.$scope.$isShown) {
                modal.hide();
            }
        });
        $scope.$on('$destroy', function () {
            sub.then(function(subscription) {
                subscription.unsubscribe();
            });
        });
    });
})(window.angular);