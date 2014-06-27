package ru.ifmo.ailab.daafse.alertservice;

import com.rabbitmq.client.Channel;

public interface StreamService {

    public void publish(final StreamURI uri, final byte[] body);
    
    public void register(final StreamURI uri);

    public void unregister(final StreamURI uri);
    
    public Channel getOrCreateChannel(final StreamURI uri) throws Exception;

}
