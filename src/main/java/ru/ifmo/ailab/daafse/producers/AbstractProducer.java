package ru.ifmo.ailab.daafse.producers;

import com.rabbitmq.client.Channel;
import ru.ifmo.ailab.daafse.bus.MessageBusFactory;
import ru.ifmo.ailab.daafse.bus.StreamID;
import ru.ifmo.ailab.daafse.registry.client.ProducerRegistry;
import ru.ifmo.ailab.daafse.registry.client.RegistryFactory;

import java.io.IOException;

/**
 * Created by oscii on 29/04/14.
 */
public abstract class AbstractProducer {
    final protected StreamID streamID;
    final protected ProducerRegistry registry;
    final private String exchangeName = MessageBusFactory.getBus().getExchangeName();
    private Channel channel;

    protected AbstractProducer(StreamID streamID)
            throws IOException, IllegalArgumentException {
        if (streamID == null) {
            throw new IllegalArgumentException("Stream route can't be null");
        }
        this.streamID = new StreamID(streamID.getRoute());

        channel = MessageBusFactory.getBus().getChannel();
        channel.exchangeDeclare(exchangeName, "topic");
        registry = RegistryFactory.getProducerRegistry();
    }

    public void publishStream() {
        registry.publishStream(streamID);
    }

    public void unpublishStream() {
        registry.unpublishStream(streamID);
    }

    public void initChannel() throws IOException {
        if (channel == null || !channel.isOpen()) {
            return;
        }

        channel = MessageBusFactory.getBus().getChannel();
        channel.exchangeDeclare(exchangeName, "topic");
    }

    public void closeChannel() throws IOException {
        channel.close();
    }


    public boolean publish(byte[] data) {
        try {
            channel.basicPublish(exchangeName, streamID.getRoute(), null, data);
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }



}
