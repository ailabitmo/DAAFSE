package ru.ifmo.ailab.daafse.messagebus.registry.server;

import com.rabbitmq.client.AMQP.BasicProperties;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.QueueingConsumer;
import ru.ifmo.ailab.daafse.messagebus.MessageBusFactory;
import ru.ifmo.ailab.daafse.messagebus.StreamID;
import ru.ifmo.ailab.daafse.messagebus.registry.protocol.RegistryProtocol;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by oscii on 30/04/14.
 */
public class Registry implements Runnable {
    private Set<StreamID> streams = new HashSet<>();
    private Channel channel;
    private QueueingConsumer consumer;

    public Registry() throws IOException {
        String registryName = MessageBusFactory.getBus().getRegistryName();
        channel = MessageBusFactory.getBus().getChannel();
        channel.queueDeclare(registryName, false, false, false, null);
//        channel.basicQos(1);
        consumer = new QueueingConsumer(channel);
        channel.basicConsume(registryName, false, consumer);
    }

    public void publishStream(StreamID streamID) {
        streams.add(streamID);
    }

    public void unpublishStream(StreamID streamID) {
        streams.remove(streamID);
    }

    public List<StreamID> getAvailableStreams() {
        return new ArrayList<>(streams);
    }


    @Override
    public void run() {
        System.out.println("[Registry starting]");
        if (!channel.isOpen()) {
            try {
                channel = MessageBusFactory.getBus().getChannel();
            } catch (IOException e) {
                e.printStackTrace();
                return;
            }
        }
        try {
            boolean running = true;
            while (running) {
                try {
                    QueueingConsumer.Delivery delivery = consumer.nextDelivery();
                    BasicProperties props = delivery.getProperties();
                    BasicProperties replyProps = new BasicProperties
                            .Builder()
                            .correlationId(props.getCorrelationId())
                            .build();

                    String msg = new String(delivery.getBody());

                    System.out.println("[Registry received] " + msg);

                    try {
                        boolean r = processReceivedData(delivery, msg, props, replyProps);
                    } catch (IOException e) {
                        e.printStackTrace();
                        break;
                    }

                } catch (InterruptedException e) {
                    e.printStackTrace();
                    running = false;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            System.out.println("[Registry stopping]");
            try {
                channel.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private boolean processReceivedData(QueueingConsumer.Delivery delivery,
            String msg, BasicProperties props, BasicProperties replyProps) throws IOException {

        String[] wordAndParams = msg.split(" ");

        String word = wordAndParams[0];
        if (word.equals(RegistryProtocol.PUBLISH.toString())) {
            publishStream(new StreamID(wordAndParams[1]));

        } else if (word.equals(RegistryProtocol.UNPUBLISH.toString())) {
            unpublishStream(new StreamID(wordAndParams[1]));

        } else if (word.equals(RegistryProtocol.GET_STREAMS.toString())) {
            List<StreamID> availableStreams = getAvailableStreams();
            StringBuilder response = new StringBuilder()
                    .append(RegistryProtocol.STREAMS.toString())
                    .append(" ");

            boolean first = true;
            for (StreamID stream : availableStreams) {
                if (first) {
                    first = false;
                } else {
                    response.append(",");
                }
                response.append(stream.getRoute());
            }

            String result = response.toString();
            System.out.println("[Registry sent] " + result);

            channel.basicPublish("", props.getReplyTo(), replyProps,
                    result.getBytes());
            channel.basicAck(delivery.getEnvelope().getDeliveryTag(), false);
        }

        return true;
    }
}
