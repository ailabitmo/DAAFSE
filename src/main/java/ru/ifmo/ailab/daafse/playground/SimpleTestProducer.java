package ru.ifmo.ailab.daafse.playground;

import ru.ifmo.ailab.daafse.bus.StreamID;
import ru.ifmo.ailab.daafse.producers.AbstractProducer;

import java.io.IOException;

/**
 * Created by oscii on 30/04/14.
 */
class SimpleTestProducer extends AbstractProducer implements Runnable {
    public SimpleTestProducer(StreamID streamID)
            throws IOException, IllegalArgumentException {
        super(streamID);
    }

    @Override
    public void run() {
        boolean running = true;
        while (running && !Thread.interrupted()) {
            String testString =
                    Math.random() + " " + Math.random() + " " + Math.random();
            publish(testString.getBytes());
            System.out.println("[SimpleTestProducer sent] " + testString);
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                running = false;
                e.printStackTrace();
            }
        }

    }
}
