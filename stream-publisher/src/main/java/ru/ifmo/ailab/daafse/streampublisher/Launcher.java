package ru.ifmo.ailab.daafse.streampublisher;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.aeonbits.owner.ConfigFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.ifmo.ailab.daafse.streampublisher.config.PublisherConfig;
import ru.ifmo.ailab.daafse.streampublisher.messages.MessagePublisher;
import ru.ifmo.ailab.daafse.streampublisher.messages.MessagePublisherFactory;
import ru.ifmo.ailab.daafse.streampublisher.observations.Observation;

public class Launcher implements ObservationListener {

    private static final PublisherConfig CONFIG = ConfigFactory
            .create(PublisherConfig.class);
    private static final MessagePublisher producer = 
            new MessagePublisherFactory().create(CONFIG.mbusType());
    private static final Store store = new Store(CONFIG.sparqlUpdate());
    private static final Logger logger = LoggerFactory.getLogger(Launcher.class);
    private static String lang = "RDF/XML";
    private static boolean verbose = false;

    public static void main(String[] args) throws Exception {
        try {
            Path log = Paths.get(args[0]);
//            Path ontology = Paths.get(args[1]);
            if (args.length > 1 && args[1] != null) {
                lang = args[1];
                if(args.length > 2 && args[2] != null) {
                    verbose = Boolean.parseBoolean(args[2]);
                }
            }
            logger.info("Observations will be read from {} file.", log);
            LogReader lr = new LogReader(log, new Launcher());
            producer.init();
            if(CONFIG.sparqlUpdateEnabled()) {
                store.clearAll();
//                store.uploadFile(ontology);
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
            producer.publish(CONFIG.mbusTopicPrefix(observation.getMeterId()),
                    out.toString());
            if(verbose) {
                logger.info(out.toString());
            }
            logger.debug("Published to {}", 
                    CONFIG.mbusTopicPrefix(observation.getMeterId()));
            if (CONFIG.sparqlUpdateEnabled()) {
                store.save(observation);
                logger.debug("Saved to {}", observation.getMeterURI());
            }
        } catch (IOException ex) {
            logger.debug(ex.getMessage(), ex);
        }
    }

}
