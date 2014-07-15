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

    private static final PublisherConfig CONFIG = ConfigFactory
            .create(PublisherConfig.class);
    private static final Producer producer = new Producer(
            CONFIG.serverURI(), CONFIG.exchangeName());
    private static final Store store = new Store(CONFIG.sparqlUpdate());
    private static final Logger logger = LoggerFactory.getLogger(Publisher.class);
    private static String lang = "RDF/XML";

    public static void main(String[] args) throws Exception {
        try {
            Path log = Paths.get(args[0]);
            if (args.length > 1 && args[1] != null) {
                lang = args[1];
            }
            logger.info("Observations will be read from {} file.", log);
            LogReader lr = new LogReader(log, new Publisher());
            producer.init();
            if(CONFIG.sparqlUpdateEnabled()) {
                store.clearAll();
            }
            logger.debug("SPARQL endpoint [{}] has been cleared!",
                    CONFIG.sparqlUpdate());

            lr.run();

        } finally {
            producer.close();
        }
    }

    @Override
    public void newObservation(Observation observation) {
        try {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            observation.getModel().write(out, lang);
            producer.publish(CONFIG.routingKey(observation.getMeterId()),
                    out.toByteArray());
            logger.debug("Published to {}",
                    CONFIG.routingKey(observation.getMeterId()));
            if (CONFIG.sparqlUpdateEnabled()) {
                store.save(observation);
                logger.debug("Saved to {}", observation.getMeterURI());
            }
        } catch (IOException ex) {
            logger.debug(ex.getMessage(), ex);
        }
    }

}
