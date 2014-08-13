(function(angular, N3, console, Object) {

    var module = angular.module('ngRDFResource', ['ngSPARQL', 'ngSPARQL.config']);
    
    module.factory('ResourceStore', function(ResourceUtils, SPARQL_CONFIG) {
        var ResourceStore = function () {
            this._store = N3.Store(null, SPARQL_CONFIG.PREFIXES);
        };
        
        ResourceStore.prototype.get = function (uri, path, index) {
            index = index || 0;
            
            var results = [];
            if(path.length - 1 > index) {
                var vs = this._store.find(uri, path[index], null);
                vs.forEach(function(v) {
                    results = results.concat(
                            this.get(v.object, path, index + 1));
                }, this);
                return results;
            } else {
                var vs = this._store.find(uri, path[index], null);
                if(vs.length) {
                    vs.forEach(function(v) {
                        results.push(v.object);
                    }, this);
                } else {
                    results.push(null);
                }
                return results;
            }
        };
        ResourceStore.prototype.genMissingTP = function(root, path, index) {
            if(!index) index = 0;

            var missingTriples = [];
            if(path.length > index) {
                var vs = this._store.find(root, path[index], null);
                if(!vs.length) {
                    var variable = ResourceUtils.pathToVar(path, index);

                    missingTriples = missingTriples.concat([[
                        root.indexOf('?') === 0 || root.indexOf('<') === 0? root : '<' + root + '>',
                        path[index], 
                        variable
                    ]]);
                    return missingTriples.concat(this.genMissingTP(
                            variable, path, index + 1));
                } else {
                    vs.forEach(function(v) {
                        missingTriples = missingTriples.concat(
                                this.genMissingTP(v.object, path, index + 1));
                    }, this);
                    return missingTriples;
                }
            } else {
                return [];
            } 
        };
        ResourceStore.prototype.getAndGenMissingTP = function(uri, type, props) {
                var resources = this._store.find(uri, 'rdf:type', type);
                var missingTriples = [];
                var map = {};
                
                uri = uri ? '<' + uri + '>' : '?uri';
                type = type ? type : '?rdf_type';
                
                if (resources.length > 0) {
                    resources.forEach(function(resource) {
                        var r = {
                            'rdf:type': [resource.object]
                        };
                        props.forEach(function(prop) {
                            var path = prop.split('/');
                            missingTriples = missingTriples.concat(
                                    this.genMissingTP(resource.subject, path));
                            
                            //fill with the root property
                            var vs = this._store.find(
                                    resource.subject, path[0], null);
                            if(vs.length) {
                                r[path[0]] = vs.map(function(v) {
                                    return v.object;
                                });
                            }
                        }, this);
                        
                        map[resource.subject] = r;
                    }, this);
                } else {
                    missingTriples.push([uri, 'rdf:type', type]);
                    props.forEach(function(prop) {
                        if(prop.indexOf('/') > -1) {
                            //`prop` is a path
                            var path = prop.split('/');
                            missingTriples = missingTriples.concat(
                                    this.genMissingTP(uri, path));
                        } else {
                            missingTriples.push(
                                    [uri, prop, ResourceUtils.propToVar(prop)]);
                        }
                    }, this);
                }
                
                return {
                    missing: missingTriples,
                    map: map
                };
            };
        
        return new ResourceStore();
    });
    module.factory('ResourceFactory', function(Resource, ResourceUtils) {
        var ResourceFactory = function() {
            
        };
        
        /**
         * Creates a new `Resource` from a bag of triples.
         * 
         * @param {Array} triples
         * @returns {Resource} a new resource
         */
        ResourceFactory.prototype.newFromTriples = function(triples) {
            if(triples.length) {
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
            } else {
                return null;
            }
        };
        
        return new ResourceFactory();
    });
    module.factory('GraphFactory', function(Graph){
        var GraphFactory = function() {};
        
        GraphFactory.prototype.newFromTriples = function(triples, prefixes) {
            return new Graph(triples, prefixes);
        };
        
        return new GraphFactory();
    });
    module.factory('Resource', function(ResourceStore, ResourceUtils) {
        var Resource = function(uri, type) {
            if (uri) {
                this.uri = uri;
            }
            if (type) {
                this['rdf:type'] = [type];
            }
        };
        
        Resource.prototype = {
            _getFirstValueOfProperty: function(property) {
                if(this[property]) {
                    if(ResourceUtils.utils().isLiteral(this[property][0])) {
                        return ResourceUtils.utils().getLiteralValue(
                                this[property][0]);
                    }
                    return this[property][0];
                }
                return null;
            }
        };
        
        Resource.prototype.get = function(prop) {
            if(prop.indexOf('/') > -1) {
                var path = prop.split('/');
                if(this[path[0]]) {
                    return ResourceStore.get(this[path[0]], path.slice(1))[0];
                } else {
                    //given root property doesn't exist
                    return null;
                }
            } else {
                return this._getFirstValueOfProperty(prop);
            }
        };
        Resource.prototype.is = function(type) {
            if(Array.isArray(type)) {
                var result = -1;
                type.some(function(element, index) {
                    var expandedType = ResourceUtils.expandQName(element);
                    result = index;
                    return this['rdf:type'].indexOf(expandedType) > -1;
                }, this);
                return result;
            } else {
                var expandedType = ResourceUtils.expandQName(type);
                return this['rdf:type'].indexOf(expandedType) > -1;
            }
        };
        
        return Resource;
    });
    module.factory('ResourceManager', function(
            $q, Resource, ResourceStore, ResourceUtils, sparql) {
        var ResourceManager = function() {
        };

        ResourceManager.prototype = {
            _mapToResources: function(type, map) {
                var t = type ? ResourceUtils.expandQName(type) : null;
                var resources = [];
                Object.keys(map).forEach(function(uri) {
                    var isThisType = t ? map[uri]['rdf:type'] 
                            && map[uri]['rdf:type'].indexOf(t) > -1 : true;
                    if(isThisType) {
                        var model = new Resource(uri, map[uri]['rdf:type']);
                        angular.extend(model, map[uri]);
                        resources.push(model);
                    }
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
            var thisArg = this;
            var results = ResourceStore.getAndGenMissingTP(null, type, props);
            if(results.missing.length > 0) {
                sparql.loadTriples(results.missing).then(function(bindings) {
                    bindings.results.bindings.forEach(function(triple) {
                        var s = triple.s.value;
                        var p = triple.p.value;
                        var o = triple.o.value;
                        ResourceStore._store.addTriple(s, p, o);
                        
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
            return deferred.promise.then(function(map) {
                return thisArg._mapToResources(type, map);
            });
        };
        
        ResourceManager.prototype.findByURI = function(uri, props) {
            var thisArg = this;
            var deferred = $q.defer();
            var results = ResourceStore.getAndGenMissingTP(uri, null, props);
            
            if(results.missing.length > 0) {
                sparql.loadTriples(results.missing).then(function(bindings) {
                    bindings.results.bindings.forEach(function(triple) {
                        var s = triple.s.value;
                        var p = triple.p.value;
                        var o = triple.o.value;
                        ResourceStore._store.addTriple(s, p, o);
                        
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
            return deferred.promise.then(function(map) {
                return thisArg._mapToResources(null, map);
            });
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
        ResourceUtils.prototype.expandQName = function(qname) {
            if(qname && this._n3utils.isQName(qname)) {
                return this._n3utils.expandQName(qname, SPARQL_CONFIG.PREFIXES);
            }
            return qname;
        };
        ResourceUtils.prototype.propToVar = function(prop) {
            return '?' + prop.replace(':', '_');
        };
        ResourceUtils.prototype.pathToVar = function(path, index) {
            if(index < 0) { return null; }
            
            var variable = "?" + path[0].replace(':', '_');
            for(var i = 1; i <= index; i++) {
                variable += '_' + path[i].replace(':', '_');
            }
            return variable;
        };
        ResourceUtils.prototype.varToProp = function(variable) {
            return variable.replace('_', ':');
        };
        
        return new ResourceUtils();
    });
    module.factory('Graph', function(ResourceFactory, SPARQL_CONFIG){
        var Graph = function (triples, prefixes) {
            if(!prefixes) {
                prefixes = SPARQL_CONFIG.PREFIXES;
            }
            this._store = N3.Store(triples, prefixes);
        };
        
        Graph.prototype.getByType = function(type) {
            var types = Array.isArray(type)? type: [type];
            
            var resources = [];
            types.forEach(function(type) {
                var subjects = this._store.find(null, 'rdf:type', type);
                subjects.forEach(function(subject) {
                    var triples = this._store.find(subject.subject, null, null);
                    resources.push(ResourceFactory.newFromTriples(triples));
                }, this);
            }, this);
            return resources;
        };
        
        Graph.prototype.getByURI = function(uri) {
            var triples = this._store.find(uri, null, null);
            return ResourceFactory.newFromTriples(triples);
        };
        
        return Graph;
    });
})(window.angular, window.N3, window.console, window.Object);