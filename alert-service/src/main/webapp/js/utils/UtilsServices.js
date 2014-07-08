(function(angular, N3){ 'use strict';
    var module = angular.module('metersApp.utils', []);
    
    module.factory('utils', function($q){
        function Utils() {
            this._n3util = N3.Util;
            this._n3parser = N3.Parser();
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
        Utils.prototype.shiftAndPush = function(array, length, element) {
            if(array.length >= length) {
                array.shift();
            }
            array.push(element);
        };
        
        Utils.prototype.parseTTL = function(data) {
            var deferred = $q.defer();
            var triples = [];
            this._n3parser.parse(data, function(error, triple, prefixes) {
                if(triple) {
                    triples.push(triple);
                } else {
                    deferred.resolve(triples);
                }
            });
            return deferred.promise;
        };
        
        return new Utils();
    });
})(window.angular, window.N3);