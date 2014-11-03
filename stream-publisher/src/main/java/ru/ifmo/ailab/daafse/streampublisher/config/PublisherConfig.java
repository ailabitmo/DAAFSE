package ru.ifmo.ailab.daafse.streampublisher.config;

import java.net.URI;
import org.aeonbits.owner.Config;
import org.aeonbits.owner.Config.Sources;

@Sources({
    "file:${DAAFSE_SP_HOME}/config.properties",
    "classpath:ru/ifmo/ailab/daafse/streampublisher/config/config.properties"
})
public interface PublisherConfig extends Config {
    
    public static final String VIRTUOSO = "virtuoso";
    public static final String AMQP = "amqp";
    public static final String WAMP = "wamp";
    
    @Key("mbus.type")
    String mbusType();
    
    @Key("mbus.uri")
    @DefaultValue("amqp://localhost")
    URI serverURI();
    
    @Key("mbus.wamp.realm")
    @DefaultValue("realm1")
    String wampRealmName();
    
    @Key("mbus.amqp.exchangeName")
    @DefaultValue("meter_exchange")
    String amqpExchangeName();
    
    @Key("mbus.topic.prefix")
    @DefaultValue("meter.%s")
    String mbusTopicPrefix(String meterId);
    
    @Key("sparql.vendor")
    String sparqlVendor();
    
    @Key("sparql.update")
    String sparqlUpdate();
    
    @Key("sparql.upload")
    String sparqlUpload();
    
    @Key("sparql.update.enabled")
    @DefaultValue("true")
    boolean sparqlUpdateEnabled();
    
    @Key("sparql.update.username")
    @DefaultValue("")
    String sparqlUpdateUsername();
    
    @Key("sparql.update.password")
    @DefaultValue("")
    String sparqlUpdatePassword();
    
    @Key("sparql.update.maxBatchSize")
    @DefaultValue("5")
    int sparqlMaxBatchSize();
    
}
