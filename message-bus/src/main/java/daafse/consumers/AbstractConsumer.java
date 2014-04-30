package daafse.consumers;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.QueueingConsumer;
import daafse.bus.MessageBusFactory;
import daafse.bus.StreamID;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by oscii on 29/04/14.
 */
public abstract class AbstractConsumer implements Runnable {
    final protected Set<StreamID> streams = new HashSet<>();
    final private Channel channel;
    final private String exchangeName = MessageBusFactory.getBus().getExchangeName();
    final private String queueName;
    final private QueueingConsumer consumer;

    protected AbstractConsumer() throws IOException {
        channel = MessageBusFactory.getBus().getChannel();
        channel.exchangeDeclare(exchangeName, "topic");
        queueName = channel.queueDeclare().getQueue();
        consumer = new QueueingConsumer(channel);
        channel.basicConsume(queueName, true, consumer);
    }


    public void listenStream(StreamID streamID) throws IOException {
        streams.add(streamID);
        channel.queueBind(queueName, exchangeName, streamID.getRoute());
    }

    public void unlistenStream(StreamID streamID) throws IOException {
        if (streams.contains(streamID)) {
            streams.remove(streamID);
            channel.queueUnbind(queueName, exchangeName, streamID.getRoute());
        }
    }

    @Override
    public void run() {
        boolean running = true;
        while (running && !Thread.interrupted()) {
            try {
                QueueingConsumer.Delivery delivery = consumer.nextDelivery();
                process(delivery.getBody());
            } catch (InterruptedException e) {
                running = false;
                e.printStackTrace();
            }
        }
    }

    abstract protected void process(byte[] data);
}
