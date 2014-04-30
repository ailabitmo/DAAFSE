package ru.ifmo.ailab.daafse.bus;

import java.io.IOException;

/**
 * Created by oscii on 29/04/14.
 */
public class MessageBusFactory {
    private MessageBusFactory() { }

    public static MessageBus getBus() throws IOException {
        return MessageBusImpl.getInstance();
    }
}
