package ru.ifmo.ailab.daafse.bus;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

import java.io.IOException;

/**
 * Created by oscii on 29/04/14.
 */
enum MessageBusImpl implements MessageBus {
    INSTANCE;

    private final Connection connection;
    private final String REGISTRY_ROUTE = "registry";
    private final String HOST = "localhost";

    private MessageBusImpl() throws IOException {
        ConnectionFactory connectionFactory = new ConnectionFactory();
        connectionFactory.setHost(HOST);
        connection = connectionFactory.newConnection();
    }

    @Override
    public Channel getChannel() throws IOException {
        return connection.createChannel();
    }

    @Override
    public String getRegistryRoute() {
        return REGISTRY_ROUTE;
    }
}
