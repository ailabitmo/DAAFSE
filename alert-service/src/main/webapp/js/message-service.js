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

    services.factory('cache', function() {
        function Cache() {
            this.map = {};
        };
        
        Cache.prototype.write = function(key, value) {
            this.map[key] = value;
        };
        
        Cache.prototype.read = function(key) {
            return this.map[key];
        };
        
        return new Cache();
    });
    
})(window.angular, window.console, window.Stomp);