package ru.ifmo.ailab.daafse.bus;

import com.rabbitmq.client.Channel;

import java.io.IOException;

/**
 * Created by oscii on 29/04/14.
 */
public interface MessageBus {
    public Channel getChannel() throws IOException;
    public String getRegistryName();
    public String getExchangeName();
}
