package ru.ifmo.ailab.daafse.alertservice.config;

import java.net.URI;
import org.aeonbits.owner.Config;

public interface ServiceConfig extends Config {
    
    @Key("alerts.streamURI")
    @DefaultValue("amqp://lpmstreams.tk?exchangeName=alert_exchange&routingKey=alerts")
    URI alertsStreamURI();
    
    @Key("sparql.endpointURL")
    @DefaultValue("http://lpmanalytics.tk/sparql")
    String sparqlEndpointURL();
    
    @Key("cqels.home")
    @DefaultValue("/opt/wildfly/cqels_home")
    String cqelsHome();
    
}
