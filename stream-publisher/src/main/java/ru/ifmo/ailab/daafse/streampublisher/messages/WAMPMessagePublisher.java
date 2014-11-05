package ru.ifmo.ailab.daafse.streampublisher.messages;

import java.io.IOException;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ws.wamp.jawampa.ApplicationError;
import ws.wamp.jawampa.WampClient;
import ws.wamp.jawampa.WampClientBuilder;

public class WAMPMessagePublisher implements MessagePublisher {
    
    private static final Logger logger = LoggerFactory.getLogger(WAMPMessagePublisher.class);
    private final String serverURI;
    private final String realm;
    private WampClient client;
    
    WAMPMessagePublisher(final String serverURI, final String realm) {
        this.serverURI = serverURI;
        this.realm = realm;
    }

    @Override
    public void init() {
        try {
        WampClientBuilder builder = new WampClientBuilder();
            builder.witUri(serverURI)
                    .withRealm(realm)
                    .withInfiniteReconnects()
                    .withReconnectInterval(5, TimeUnit.SECONDS);
            client = builder.build();
            client.statusChanged().subscribe((WampClient.Status newStatus) -> {
                if(WampClient.Status.Connected == newStatus) {
                    logger.debug("Connected to WAMP router [{}]", serverURI);
                } else if(WampClient.Status.Connecting == newStatus) {
                    logger.debug("Connecting to WAMP router [{}]", serverURI);
                }
            });
            client.open();
        } catch(ApplicationError ex) {
            logger.error(ex.getMessage(), ex);
        }
    }

    @Override
    public void publish(String topic, String message) throws IOException {
        client.publish(topic, message);
    }

    @Override
    public void close() {
        client.close();
    }
    
}
