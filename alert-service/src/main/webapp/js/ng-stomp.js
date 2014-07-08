(function(angular, console, Stomp) {
    var services = angular.module('ngSTOMP', []);

    services.factory('stomp', function($q) {
        var globalDeferred;
            
        function StreamClient() {
            this.clients = {};
        };
        
        StreamClient.prototype = {
            _newStompClient: function (serverUrl) {
                var client = Stomp.client(serverUrl);
                client.debug = function(){};
                client.heartbeat.outgoing = 0;
                client.heartbeat.incoming = 0;
                return client;
            },
            _connect: function (client, uri) {
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
        };
        
        StreamClient.prototype.subscribe = function(streamUri, callback) {
            var uri = parseUri(streamUri);
            var serverUri = 'ws://' + uri.host + ':15674/stomp/websocket';
            if(!this.clients.hasOwnProperty(serverUri)) {
                this.clients[serverUri] = this._newStompClient(serverUri);
            }
            var client = this.clients[serverUri];

            /**
             * The connect should not be called while the previous one 
             * has not finished yet.
             */
            var thisArg = this;
            if(!globalDeferred) {
                globalDeferred = $q.defer();
            }
            globalDeferred = $q.when(globalDeferred)
            .then(function() {
                return thisArg._connect(client, uri); 
            })
            .then(function(client) {
                var dest = '/exchange/' + uri.queryKey['exchangeName'] 
                        + '/' + uri.queryKey['routingKey'];

                client.subscribe(dest, callback);
            });
        };
        
        return new StreamClient();
    });
    
})(window.angular, window.console, window.Stomp);