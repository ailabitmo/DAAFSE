package ru.ifmo.ailab.daafse.streampublisher;

import com.hp.hpl.jena.update.UpdateExecutionFactory;
import com.hp.hpl.jena.update.UpdateFactory;
import com.hp.hpl.jena.update.UpdateRequest;
import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;
import org.aeonbits.owner.ConfigFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.ifmo.ailab.daafse.streampublisher.config.PublisherConfig;

public class Store {

    private static final Logger logger = LoggerFactory.getLogger(Store.class);
    private static final PublisherConfig CONFIG = ConfigFactory
            .create(PublisherConfig.class);
    private final String endpoint;
    private final List<Observation> cache = new ArrayList<>(
            CONFIG.sparqlMaxBatchSize());
    private int batchSize = 0;

    public Store(final String endpoint) {
        this.endpoint = endpoint;
    }

    public void clearAll() {
        UpdateRequest request = UpdateFactory.create("CLEAR ALL");
        UpdateExecutionFactory.createRemote(request, endpoint).execute();
    }

    public void save(Observation o) {
        cache.add(o);
        batchSize++;
        
        if (batchSize >= CONFIG.sparqlMaxBatchSize()) {
            try {
                logger.debug("Flusing the cache...");
                UpdateRequest request = UpdateFactory.create(modelToQuery(cache));
                UpdateExecutionFactory.createRemote(request, endpoint).execute();
                batchSize = 0;
                cache.clear();
            } catch (IOException ex) {
                logger.warn(ex.getMessage(), ex);
            }
        }
    }

    private String modelToQuery(List<Observation> obs) throws IOException {
        String query = "INSERT DATA {\n";
        for (Observation o : obs) {
            query += "GRAPH <" + o.getMeterURI() + "> {\n";
            try (StringWriter writer = new StringWriter()) {
                o.getModel().write(writer, "N3");
                query += writer.toString();
            } catch (IOException ex) {
                throw ex;
            }
            query += "\n}";
        }
        query += "\n}";
        return query;
    }

}
