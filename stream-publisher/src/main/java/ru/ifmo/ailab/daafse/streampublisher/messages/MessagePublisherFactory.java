package ru.ifmo.ailab.daafse.streampublisher.messages;

import org.aeonbits.owner.ConfigFactory;
import ru.ifmo.ailab.daafse.streampublisher.config.PublisherConfig;

public class MessagePublisherFactory {
    
    private static final PublisherConfig CONFIG = ConfigFactory
            .create(PublisherConfig.class);

    public MessagePublisher create(final String name){
        if(name.equalsIgnoreCase(PublisherConfig.WAMP)) {
            return new WAMPMessagePublisher(
                    CONFIG.serverURI().toASCIIString(), CONFIG.wampRealmName());
        } else if(name.equalsIgnoreCase(PublisherConfig.AMQP)) {
            return new AMQPMessagePublisher(
                    CONFIG.serverURI(), CONFIG.amqpExchangeName());
        } else {
            throw new IllegalArgumentException();
        }
    }
    
}
