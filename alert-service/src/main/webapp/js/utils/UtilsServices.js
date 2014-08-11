(function(angular, N3){ 'use strict';
    var module = angular.module('metersApp.utils', []);
    
    module.factory('utils', function($q){
        function Utils() {
            this._n3util = N3.Util;
        };
        
        /**
        * Adds a new element at the end and removes the first element 
        * if the array length is higher or equal to length.
        * 
        * @param {Array} array
        * @param {Integer} length
        * @param {Object} element
        * @returns {undefined}
        */
        Utils.prototype.shiftAndPush = function(array, element, length) {
            if(length && array.length >= length) {
                array.shift();
            }
            array.push(element);
        };
        
        Utils.prototype.parseTTL = function(data) {
            var deferred = $q.defer();
            var triples = [];
            var parser = N3.Parser();
            parser.parse(data, function(_, triple, __) {
                if(triple) {
                    triples.push(triple);
                } else {
                    deferred.resolve(triples);
                }
            });
            return deferred.promise;
        };
        
        Utils.prototype.addPoints = function(chart, points) {
            points.forEach(function(series, index) {
                chart.series[index].data = series;
            });
        };
        
        return new Utils();
    });
})(window.angular, window.N3);