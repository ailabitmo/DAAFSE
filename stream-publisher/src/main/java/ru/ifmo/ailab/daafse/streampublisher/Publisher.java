package ru.ifmo.ailab.daafse.streampublisher;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.aeonbits.owner.ConfigFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.ifmo.ailab.daafse.streampublisher.config.PublisherConfig;

public class Publisher implements ObservationListener {

    private static final PublisherConfig config = ConfigFactory
            .create(PublisherConfig.class);
    private static final Producer producer = new Producer(
            config.serverURI(), config.exchangeName());
    private static final Logger logger = LoggerFactory.getLogger(Publisher.class);

    public static void main(String[] args) throws Exception {
        try {
            Path log = Paths.get(args[0]);
            logger.info("Observations will be read from {} file.", log);
            LogReader lr = new LogReader(log, new Publisher());
            producer.init();

            lr.run();

        } finally {
            producer.close();
        }
    }

    @Override
    public void newObservation(Observation observation) {
        try {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            observation.getModel().write(out, "JSON-LD");
            producer.publish(config.routingKey(observation.getMeterId()),
                    out.toByteArray());
            logger.debug("Published to {}", 
                    config.routingKey(observation.getMeterId()));
        } catch (IOException ex) {
            logger.debug(ex.getMessage(), ex);
        }
    }

}
