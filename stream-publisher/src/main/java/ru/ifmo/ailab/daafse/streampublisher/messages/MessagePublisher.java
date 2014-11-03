package ru.ifmo.ailab.daafse.streampublisher.messages;

import java.io.IOException;

public interface MessagePublisher {

    public void init();
    
    public void publish(final String topic, final String message) throws IOException;
    
    public void close();
    
}
