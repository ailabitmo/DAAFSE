package ru.ifmo.ailab.daafse.playground;

import ru.ifmo.ailab.daafse.messagebus.StreamID;
import ru.ifmo.ailab.daafse.streampublisher.LogReader;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by oscii on 01/05/14.
 */
public class Runner {
    public static void main(String[] args) throws IOException {
        ExecutorService executor = Executors.newFixedThreadPool(4);

        StreamID streamID = new StreamID("observation.route");
        ObservationConsumer consumer = new ObservationConsumer();
        ObservationProducer producer = new ObservationProducer(streamID);
        consumer.listenStream(streamID);

        Path log = Paths.get(args[0]);
        LogReader logReader = new LogReader(log, producer);

        executor.submit(consumer);
        executor.submit(logReader);

        boolean running = true;
        while (running) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                running = false;
                e.printStackTrace();
            }
        }
    }





}
