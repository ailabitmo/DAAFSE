package ru.ifmo.ailab.daafse.playground;

import ru.ifmo.ailab.daafse.messagebus.consumers.AbstractConsumer;

import java.io.IOException;

/**
 * Created by oscii on 01/05/14.
 */
public class ObservationConsumer extends AbstractConsumer {
    public ObservationConsumer() throws IOException { }

    @Override
    protected void process(byte[] data) {
        System.out.println("[ObservationConsumer]");
        System.out.println(new String(data));
    }
}
