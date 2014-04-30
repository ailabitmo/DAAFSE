package daafse.registry.client;

import java.io.IOException;

/**
 * Created by oscii on 29/04/14.
 */
public class RegistryFactory {
    static public ConsumerRegistry getConsumerRegistry() throws IOException {
        return ConsumerRegistryImpl.getInstance();
    }
    static public ProducerRegistry getProducerRegistry() throws IOException {
        return ProducerRegistryImpl.getInstance();
    }
}
