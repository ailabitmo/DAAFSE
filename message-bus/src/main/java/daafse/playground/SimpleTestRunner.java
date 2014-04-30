package daafse.playground;

import daafse.bus.StreamID;
import daafse.registry.client.RegistryFactory;
import daafse.registry.server.Registry;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by oscii on 30/04/14.
 */
public class SimpleTestRunner {
    public static void main(String[] args) throws IOException, InterruptedException {
        ExecutorService executor = Executors.newFixedThreadPool(4);
        Registry registry = new Registry();

        StreamID testRoute = new StreamID("test.route");
        SimpleTestProducer producer = new SimpleTestProducer(testRoute);
        SimpleTestConsumer consumer = new SimpleTestConsumer();

        executor.submit(registry);
        executor.submit(consumer);
        executor.submit(producer);

        producer.publishStream();

        List<StreamID> availableStreams =
                RegistryFactory.getConsumerRegistry().getAvailableStreams();

        for (StreamID streamID : availableStreams) {
            consumer.listenStream(streamID);
        }

        boolean running = true;
        while (running) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                running = false;
                e.printStackTrace();
                break;
            }
        }
    }
}
