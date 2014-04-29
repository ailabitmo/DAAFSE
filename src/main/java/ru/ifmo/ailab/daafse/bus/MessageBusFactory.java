package ru.ifmo.ailab.daafse.bus;

/**
 * Created by oscii on 29/04/14.
 */
public class MessageBusFactory {
    private MessageBusFactory() { }

    public static MessageBus getBus() {
        return MessageBusImpl.INSTANCE;
    }
}
