package ru.ifmo.ailab.daafse.alertservice;

import javax.enterprise.inject.Default;
import javax.enterprise.inject.Disposes;
import javax.enterprise.inject.Produces;
import javax.inject.Singleton;
import org.aeonbits.owner.ConfigFactory;
import ru.ifmo.ailab.daafse.alertservice.config.ServiceConfig;
import ru.ifmo.ailab.daafse.alertservice.services.WAMPMessagePublishingService;

@Singleton
public class MessagePublishingServiceFactory {
    
    private static final ServiceConfig CONFIG = ConfigFactory.create(
            ServiceConfig.class);
    
    @Produces
    @Default
    @Singleton
    public MessagePublishingService getMessagePublishingService(
            CQELSEngine cqelsEngine) {
        if(CONFIG.mbusType().equalsIgnoreCase(ServiceConfig.WAMP)) {
            return new WAMPMessagePublishingService(cqelsEngine);
        }
        return null;
    }
    
    public void destroy(@Disposes MessagePublishingService service) {
        service.destroy();
    }
    
}
