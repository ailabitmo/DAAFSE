(function(angular, console, Stomp) {
    var services = angular.module('metersApp-services', []);

    services.factory('rabbitmq', ['$q', function($q) {
        var globalDeferred;
            
        function StreamClient(){
            this.clients = {};
        };
        
        function newStompClient (serverUrl) {
            var client = Stomp.client(serverUrl);
            client.debug = function(){};
            client.heartbeat.outgoing = 0;
            client.heartbeat.incoming = 0;
            return client;
        };
        
        function connect (client, uri) {
            var deferred = $q.defer();
            if(!client.connected) {
                client.connect(
                    uri.user?uri.user:"guest", 
                    uri.password?uri.password:"guest",
                    function () {
                        console.log("[SUCCESS] Connected to " + uri.host);
                        deferred.resolve(client);
                    },
                    function (error) {
                        console.log("[ERROR] Failed to connect to " 
                                + uri.host + " : " + error);
                        deferred.reject(error);
                    }
                );
            } else {
                deferred.resolve(client);
            }
            return deferred.promise;
        }
        
        StreamClient.prototype.subscribe = function(streamUri, callback) {
            var uri = parseUri(streamUri);
            var serverUri = 'ws://' + uri.host + ':15674/stomp/websocket';
            if(!this.clients.hasOwnProperty(serverUri)) {
                this.clients[serverUri] = newStompClient(serverUri);
            }
            var client = this.clients[serverUri];

            /**
             * The connect should not be called while the previous one 
             * has not finished yet.
             */
            if(!globalDeferred) {
                globalDeferred = $q.defer();
            }
            globalDeferred = $q.when(globalDeferred)
                .then(function() {
                    return connect(client, uri); 
                })
                .then(function(client) {
                    var dest = '/exchange/' + uri.queryKey['exchangeName'] 
                            + '/' + uri.queryKey['routingKey'];

                    client.subscribe(dest, callback);
                });
        };
        
        return new StreamClient();
    }]);

    services.factory('sparql', ['settings', '$http', '$q', function(settings, $http, $q) {
        function Client() {
        };
        
        Client.prototype.select = function(query) {
            var deferred = $q.defer();
            $http.get(settings.sparqlEndpoint, {
                params: {query: query, output: 'json'},
                headers: {Accept: "application/sparql-results+json"}
            }).success(function(data) {
                var results = bindingsToJson(data);
                deferred.resolve(results);
            }).error(function(_, status){
                deferred.reject(status);
            });
            return deferred.promise;
        };
        
        Client.prototype.register = function(query) {
            var deferred = $q.defer();
            $http.post('rest/query/register/', {}, {
                params: {query: query}
            }).success(function(){
                deferred.resolve();
            }).error(function(_, status){
                deferred.reject(status);
            });
            return deferred.promise;
        };
        
        function bindingsToJson(bindings) {
            var results = [];
            bindings.results.bindings.forEach(function(element){
                var r = {};
                Object.getOwnPropertyNames(element).forEach(function(name){
                    r[name] = element[name].value;
                });
                results.push(r);
            });
            return results;
        };
        
        return new Client();
    }]);
    
    services.factory('settings', function(){
        function Settings(){
            this.sparqlEndpoint = "http://192.168.134.114:8890/sparql-cors";
            this.prefixes = [
                {
                    prefix: "em",
                    uri:"http://purl.org/daafse/electricmeters#"
                },
                {
                    prefix: "pne", 
                    uri: "http://data.press.net/ontology/event/"
                },
                {   
                    prefix: "ssn", 
                    uri: "http://purl.oclc.org/NET/ssnx/ssn#"
                }
            ];
        };
        
        return new Settings();
    });
    
})(window.angular, window.console, window.Stomp);