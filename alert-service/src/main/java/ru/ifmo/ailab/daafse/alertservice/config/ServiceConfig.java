package ru.ifmo.ailab.daafse.alertservice.config;

import java.net.URI;
import org.aeonbits.owner.Config;

public interface ServiceConfig extends Config {
    
    public static final String WAMP = "wamp";
    
    @Key("mbus.type")
    @DefaultValue(WAMP)
    String mbusType();
    
    @Key("alerts.streamURI")
    @DefaultValue("ws://lpmstreams.tk/ws?topic=alerts.all")
    URI alertsStreamURI();
    
    @Key("sparql.endpointURL")
    @DefaultValue("http://lpmanalytics.tk/sparql")
    String sparqlEndpointURL();
    
    @Key("cqels.home")
    @DefaultValue("/opt/wildfly/cqels_home")
    String cqelsHome();
    
}
