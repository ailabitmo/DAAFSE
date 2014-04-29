package ru.ifmo.ailab.daafse.registry.client;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.MessageProperties;
import ru.ifmo.ailab.daafse.bus.MessageBusFactory;
import ru.ifmo.ailab.daafse.bus.StreamID;
import ru.ifmo.ailab.daafse.registry.protocol.RegistryProtocol;

import java.io.IOException;

/**
 * Created by oscii on 29/04/14.
 */
enum ProducerRegistryImpl implements ProducerRegistry {
    INSTANCE;

    private Channel channel;
    private String registryQueue = MessageBusFactory.getBus().getRegistryRoute();

    ProducerRegistryImpl() throws IOException {
        channel = MessageBusFactory.getBus().getChannel();
        channel.queueDeclare(registryQueue, true, false, false, null);

    }

    @Override
    public boolean publishStream(StreamID streamID) {
        String msg = RegistryProtocol.PUBLISH.toString() + " " + streamID.getRoute();
        try {
            channel.basicPublish("", registryQueue,
                    MessageProperties.PERSISTENT_TEXT_PLAIN,
                    msg.getBytes());
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public boolean unpublishStream(StreamID streamID) {
        String msg = RegistryProtocol.UNPUBLISH.toString() + " " + streamID.getRoute();
        try {
            channel.basicPublish("", registryQueue,
                    MessageProperties.PERSISTENT_TEXT_PLAIN,
                    msg.getBytes());
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }
}
