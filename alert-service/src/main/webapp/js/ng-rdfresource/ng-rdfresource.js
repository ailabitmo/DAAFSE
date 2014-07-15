(function(angular, N3, console, Object) {

    var module = angular.module('ngRDFResource', ['ngSPARQL', 'ngSPARQL.config']);

    module.factory('ResourceManager', function(
            $q, Resource, ResourceUtils, sparql, SPARQL_CONFIG) {
        var RDF_TYPE = 'http://www.w3.org/1999/02/22-rdf-syntax-ns#type';
        
        var ResourceManager = function() {
            this._n3utils = N3.Util;
            this._store = N3.Store(null, SPARQL_CONFIG.PREFIXES);
        };

        ResourceManager.prototype = {
            _expandQName: function(qname) {
                if(qname && this._n3utils.isQName(qname)) {
                    return this._n3utils.expandQName(qname, SPARQL_CONFIG.PREFIXES);
                }
                return qname;
            },
            _propToVar: function(prop) {
                return '?' + prop.replace(':', '_');
            },
            _varToProp: function(variable) {
                return variable.replace('_', ':');
            },
            _findInStore: function(uri, type, props) {
                var resources = this._store.find(uri, 'rdf:type', type);
                var missingTriples = [];
                var map = {};
                
                uri = uri ? '<' + uri + '>' : '?uri';
                type = type ? type : '?rdf_type';
                
                if (resources.length > 0) {
                    resources.forEach(function(resource) {
                        var r = {};
                        
                        props.forEach(function(prop) {
                            var values = this._store.find(
                                    resource.subject, 
                                    this._expandQName(prop), 
                                    null);
                            if (values.length > 0) {
                                r[prop] = values.map(function(v){
                                    return v.object;
                                });
                            } else {
                                //There is no such triple in the store
                                missingTriples.push([
                                    '<' + resource.subject + '>', 
                                    prop, 
                                    this._propToVar(prop)]);
                            }
                        }, this);
                        
                        map[resource.subject] = r;
                    }, this);
                } else {
                    missingTriples.push([uri, 'rdf:type', type]);
                    props.forEach(function(prop) {
                        missingTriples.push([uri, prop,
                            this._propToVar(prop)]);
                    }, this);
                }
                
                return {
                    missing: missingTriples,
                    map: map
                };
            },
            _mapToResources: function(map) {
                var resources = [];
                Object.keys(map).forEach(function(uri) {
                    var model = new Resource(uri, map[uri][RDF_TYPE]);
                    angular.extend(model, map[uri]);
                    resources.push(model);
                });
                return resources;
            }
        };
        /**
         * 
         * @param {String} type - owl:Class
         * @param {Array} props - owl:Property
         * @returns {promise} - list of {Resource}s
         */
        ResourceManager.prototype.findByType = function(type, props) {
            var deferred = $q.defer();
            var results = this._findInStore(null, type, props);
            if(results.missing.length > 0) {
                var thisArg = this;
                sparql.loadTriples(results.missing).then(function(bindings) {
                    bindings.results.bindings.forEach(function(triple) {
                        var s = triple.s.value;
                        var p = triple.p.value;
                        var o = triple.o.value;
                        this._store.addTriple(s, p, o);
                        
                        results.map[s] = results.map[s] || {};
                        var pq = ResourceUtils.toQName(p);
                        if(results.map[s].hasOwnProperty(pq)) {
                            results.map[s][pq].push(o);
                        } else {
                            results.map[s][ResourceUtils.toQName(p)] = [o];
                        }
                    }, thisArg);

                    deferred.resolve(results.map);
                });
            } else {
                deferred.resolve(results.map);
            }
            return deferred.promise.then(this._mapToResources);
        };
        
        ResourceManager.prototype.findByURI = function(uri, props) {
            var deferred = $q.defer();
            var results = this._findInStore(uri, null, props);
            
            if(results.missing.length > 0) {
                var thisArg = this;
                sparql.loadTriples(results.missing).then(function(bindings) {
                    bindings.results.bindings.forEach(function(triple) {
                        var s = triple.s.value;
                        var p = triple.p.value;
                        var o = triple.o.value;
                        this._store.addTriple(s, p, o);
                        
                        var pq = ResourceUtils.toQName(p);
                        results.map[s] = results.map[s] || {};
                        if(!results.map[s].hasOwnProperty(pq)) {
                            results.map[s][pq] = [];
                        }
                        results.map[s][ResourceUtils.toQName(p)].push(o);
                    }, thisArg);

                    deferred.resolve(results.map);
                });
            } else {
                deferred.resolve(results.map);
            }
            return deferred.promise.then(this._mapToResources);
        };

        return new ResourceManager();
    });
    
    module.factory('ResourceUtils', function(SPARQL_CONFIG) {
        function ResourceUtils() {
            this._n3utils = N3.Util;
            
            this.utils = function() {
                return this._n3utils;
            };
        };
        
        ResourceUtils.prototype.toQName = function(uri) {
            if(!this._n3utils.isQName(uri)) {
                Object.keys(SPARQL_CONFIG.PREFIXES).every(function(prefix) {
                    var index = uri.indexOf(SPARQL_CONFIG.PREFIXES[prefix]);
                    if(index > -1) {
                        uri = prefix + ":" + uri.substring(
                                SPARQL_CONFIG.PREFIXES[prefix].length);
                        return false;
                    }
                    return true;
                }, this); 
            }
            return uri;
        };
        
        return new ResourceUtils();
    });
    module.factory('Resource', function(ResourceUtils) {
        var Resource = function(uri, type) {
            if (uri) {
                this.uri = uri;
            }
            if (type) {
                this['rdf:type'] = [type];
            }
        };
        
        /**
         * Creates a new resource from a bag of triples.
         * 
         * @param {type} triples
         * @returns {undefined}
         */
        Resource.prototype.fromTriples = function(triples) {
            var newResource = new Resource();
            triples.forEach(function(triple) {
                var p = ResourceUtils.toQName(triple.predicate);
                if(p === 'rdf:type') {
                    newResource.uri = triple.subject;
                }
                if(!newResource.hasOwnProperty(p)) {
                    newResource[p] = [];
                }
                newResource[p].push(triple.object);
            });
            return newResource;
        };
        
        Resource.prototype.get = function(property) {
            if(ResourceUtils.utils().isLiteral(this[property][0])) {
                return ResourceUtils.utils().getLiteralValue(this[property][0]);
            }
            return this[property][0];
        };

        return Resource;
    });
    module.factory('Graph', function(Resource, SPARQL_CONFIG){
        var Graph = function (triples, prefixes) {
            if(!prefixes) {
                prefixes = SPARQL_CONFIG.PREFIXES;
            }
            this._store = N3.Store(triples, prefixes);
        };
        
        Graph.prototype.fromTriples = function(triples, prefixes) {
            return new Graph(triples, prefixes);
        };
        
        Graph.prototype.getByType = function(type) {
            var resources = [];
            var subjects = this._store.find(null, 'rdf:type', type);
            subjects.forEach(function(subject) {
                var triples = this._store.find(subject.subject, null, null);
                resources.push(Resource.prototype.fromTriples(triples));
            }, this);
            return resources;
        };
        
        Graph.prototype.getByURI = function(uri) {
            var triples = this._store.find(uri, null, null);
            return Resource.prototype.fromTriples(triples);
        };
        
        return Graph;
    });
})(window.angular, window.N3, window.console, window.Object);