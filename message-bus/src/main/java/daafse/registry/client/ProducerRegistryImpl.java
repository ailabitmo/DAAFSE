package daafse.registry.client;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.MessageProperties;
import daafse.bus.MessageBusFactory;
import daafse.bus.StreamID;
import daafse.registry.protocol.RegistryProtocol;

import java.io.IOException;

/**
 * Created by oscii on 29/04/14.
 */
class ProducerRegistryImpl implements ProducerRegistry {
    private Channel channel;
    private String registryQueue = MessageBusFactory.getBus().getRegistryName();

    private ProducerRegistryImpl() throws IOException {
        channel = MessageBusFactory.getBus().getChannel();
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


    private static ProducerRegistry singleton = null;

    public static ProducerRegistry getInstance() throws IOException {
        if (singleton == null) {
            singleton = new ProducerRegistryImpl();
        }
        return singleton;
    }
}
