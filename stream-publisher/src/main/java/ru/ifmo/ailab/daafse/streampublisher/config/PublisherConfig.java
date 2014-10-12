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
    
    @Key("amqp.exchangeName")
    @DefaultValue("meter_exchange")
    String exchangeName();
    
    @Key("amqp.routingKey.prefix")
    @DefaultValue("meter.location.%s")
    String routingKey(String meterId);
    
    @Key("amqp.uri")
    @DefaultValue("amqp://localhost")
    URI serverURI();
    
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
