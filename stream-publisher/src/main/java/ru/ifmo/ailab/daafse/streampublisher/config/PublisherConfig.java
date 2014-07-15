package ru.ifmo.ailab.daafse.streampublisher.config;

import java.net.URI;
import org.aeonbits.owner.Config;

public interface PublisherConfig extends Config {
    
    @Key("amqp.exchangeName")
    @DefaultValue("meter_exchange")
    String exchangeName();
    
    @Key("amqp.routingKey.prefix")
    @DefaultValue("meter.location.%s")
    String routingKey(String meterId);
    
    @Key("amqp.uri")
    @DefaultValue("amqp://localhost")
    URI serverURI();
    
    @Key("sparql.update")
    String sparqlUpdate();
    
    @Key("sparql.update.enabled")
    @DefaultValue("true")
    boolean sparqlUpdateEnabled();
    
    @Key("sparql.update.maxBatchSize")
    @DefaultValue("5")
    int sparqlMaxBatchSize();
    
}
