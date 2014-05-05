package ru.ifmo.ailab.daafse.messagebus;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

import java.io.IOException;

/**
 * Created by oscii on 29/04/14.
 */
class MessageBusImpl implements MessageBus {
    private final Connection connection;
    private final String REGISTRY_ROUTE = "registry";
    private final String EXCHANGE_NAME = "streams_exchange";
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
    public String getRegistryName() {
        return REGISTRY_ROUTE;
    }

    @Override
    public String getExchangeName() {
        return EXCHANGE_NAME;
    }


    static private MessageBus singleton = null;

    static MessageBus getInstance() throws IOException {
        if (singleton == null) {
            singleton = new MessageBusImpl();
        }
        return singleton;
    }
}
