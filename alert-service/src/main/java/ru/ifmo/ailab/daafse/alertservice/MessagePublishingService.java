package ru.ifmo.ailab.daafse.alertservice;

public interface MessagePublishingService {

    public void publish(final StreamURI uri, final String body);
    
    public void register(final StreamURI uri);

    public void unregister(final StreamURI uri);

    public void destroy();
    
}
