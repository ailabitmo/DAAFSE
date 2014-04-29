package ru.ifmo.ailab.daafse.registry.client;

/**
 * Created by oscii on 29/04/14.
 */
public class RegistryFactory {
    static public ConsumerRegistry getConsumerRegistry() {
        return ConsumerRegistryImpl.INSTANCE;
    }
    static public ProducerRegistry getProducerRegistry() {
        return ProducerRegistryImpl.INSTANCE;
    }
}
