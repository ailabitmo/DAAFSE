(function(angular, Highcharts){ 'use strict';
    var module = angular.module('metersApp.meters.services', [
        'ngSPARQL', 'ngSPARQL.config'
    ]);
    
    module.factory('metersService', function(sparql, SPARQL_CONFIG){
        function Service() {
        };
        
        Service.prototype = {
            _createQuery: function(meterUri, from, till, types) {
                var t = "";
                if(types.length > 1) {
                    t = "{ ?observation a " + types[0] + " . }\n\
                        UNION\n\
                        { ?observation a " + types[1] + " . }\n";
                } else {
                    t = "?observation a " + types[0] + " .\n";
                }
                var query = "DESCRIBE ?observation ?output ?value\n\
                            WHERE {\n";
                query += "GRAPH <" + meterUri + "> {\n";
                query += t;
                query += "?observation ssn:observationResultTime ?time ;\n\
                                    ssn:observationResult ?output ;\n\
                                    ssn:observedBy <" + meterUri + "> .\n\
                                ?output ssn:hasValue ?value .\n";
                if(from) {
                    query += "FILTER(?time >= '" + from.toISOString() 
                            + "'^^xsd:dateTime";
                    if(till) {
                        query += "&& ?time <= '" + till.toISOString() 
                                + "'^^xsd:dateTime";
                    }
                    query += ")\n";
                }
                query += "}} ORDER BY ?time";

                return query;
            },
            _sortObservations: function(array) {
                array.sort(function(a, b) {
                    var ta = a.get('ssn:observationResult');
                    var tb = b.get('ssn:observationResult');
                    if(ta < tb) {
                        return -1;
                    } else if(ta > tb) {
                        return 1;
                    }
                    return 0;
                });
                return array;
            }
        };
        
        Service.prototype.fetchObservations = function(
                meter, from, till, types) {
            var thisArg = this;
            
            var points  = {};
            types.forEach(function(element) {
                points[element] = [[], [], []];
            });
            
            var query = this._createQuery(meter, from, till, types);
            return sparql.describe(query, SPARQL_CONFIG.ENDPOINTS.ENDPOINT_2)
            .then(function(graph) {
                var observations = graph.getByType(types);
                if(observations.length > 0) {
                    var TZoffset = new Date(observations[0]
                            .get('ssn:observationResultTime')).getTimezoneOffset();
                    Highcharts.setOptions({
                        global : {
                            timezoneOffset: TZoffset
                        }
                    });
                }

                observations = thisArg._sortObservations(observations);

                observations.forEach(function(observation) {
                    var p = [];
                    var output = graph.getByURI(observation.get('ssn:observationResult'));
                    output['ssn:hasValue'].forEach(function(valueURI) {
                        var value = graph.getByURI(valueURI);
                        var phaseNumber = value.get('em:hasPhaseNumber');
                        p[phaseNumber - 1] = [
                            new Date(observation.get('ssn:observationResultTime'))
                                    .getTime(),
                            parseFloat(value.get('em:hasQuantityValue'))
                        ];
                    });
                    
                    var index = observation.is(types);
                    p.forEach(function(v, i) {
                        points[types[index]][i] = points[types[index]][i].concat([v]);
                    });
                }, thisArg);
                
                return points;
            });
        };
        
        return new Service();
    });
})(window.angular, window.Highcharts);


