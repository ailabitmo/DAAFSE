package ru.ifmo.ailab.daafse.messagebus.registry.client;

import com.rabbitmq.client.AMQP.BasicProperties;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.QueueingConsumer;
import ru.ifmo.ailab.daafse.messagebus.MessageBusFactory;
import ru.ifmo.ailab.daafse.messagebus.StreamID;
import ru.ifmo.ailab.daafse.messagebus.registry.protocol.RegistryProtocol;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Created by oscii on 29/04/14.
 */
class ConsumerRegistryImpl implements ConsumerRegistry {
    private Channel channel;
    private String requestQueueName = MessageBusFactory.getBus().getRegistryName();
    private String replyQueueName;
    private QueueingConsumer consumer;

    private ConsumerRegistryImpl() throws IOException {
        channel = MessageBusFactory.getBus().getChannel();

        replyQueueName = channel.queueDeclare().getQueue();
        consumer = new QueueingConsumer(channel);
        channel.basicConsume(replyQueueName, true, consumer);
    }

    @Override
    synchronized public List<StreamID> getAvailableStreams() throws IOException, InterruptedException {
        String response = null;
        String corrID = UUID.randomUUID().toString();

        BasicProperties props = new BasicProperties
                                    .Builder()
                                    .correlationId(corrID)
                                    .replyTo(replyQueueName)
                                    .build();

        channel.basicPublish("", requestQueueName, props,
                RegistryProtocol.GET_STREAMS.toString().getBytes());

        List<StreamID> result = new ArrayList<>();
        while (true) {
            QueueingConsumer.Delivery delivery = consumer.nextDelivery();

            if (delivery.getProperties().getCorrelationId().equals(corrID)) {
                String msg = new String(delivery.getBody());

                if (msg.startsWith(RegistryProtocol.STREAMS.toString())) {
                    String[] streamStrings = msg.split(" ")[1].split(",");

                    for (String s : streamStrings) {
                        result.add(new StreamID(s));
                    }
                    break;
                }
            }

        }

        return result;
    }


    private static ConsumerRegistry singleton = null;

    static ConsumerRegistry getInstance() throws IOException {
        if (singleton == null) {
            singleton = new ConsumerRegistryImpl();
        }
        return singleton;
    }
}
