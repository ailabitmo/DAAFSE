package ru.ifmo.ailab.daafse.streampublisher;

import com.hp.hpl.jena.query.DatasetAccessorFactory;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.update.UpdateExecutionFactory;
import com.hp.hpl.jena.update.UpdateFactory;
import com.hp.hpl.jena.update.UpdateRequest;
import com.hp.hpl.jena.util.FileManager;
import java.io.IOException;
import java.io.StringWriter;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import org.aeonbits.owner.ConfigFactory;
import org.apache.jena.atlas.web.auth.HttpAuthenticator;
import org.apache.jena.atlas.web.auth.SimpleAuthenticator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.ifmo.ailab.daafse.streampublisher.config.PublisherConfig;
import ru.ifmo.ailab.daafse.streampublisher.observations.Observation;

public class Store {

    private static final Logger logger = LoggerFactory.getLogger(Store.class);
    private static final PublisherConfig CONFIG = ConfigFactory
            .create(PublisherConfig.class);
    private final String endpoint;
    private final List<Observation> cache = new ArrayList<>(
            CONFIG.sparqlMaxBatchSize());
    private final HttpAuthenticator authenticator;
    private int batchSize = 0;

    public Store(final String endpoint) {
        this.endpoint = endpoint;
        this.authenticator = new SimpleAuthenticator(
                CONFIG.sparqlUpdateUsername(),
                CONFIG.sparqlUpdatePassword().toCharArray());
    }

    public void clearAll() {
        UpdateRequest request = null;
        if (CONFIG.sparqlVendor().equalsIgnoreCase(PublisherConfig.VIRTUOSO)) {
            request = UpdateFactory.create(
                    "DELETE{GRAPH ?g {?x ?y ?z}}WHERE{GRAPH ?g {?x ?y ?z}"
                    + "FILTER(strStarts(str(?g), \"" + Observation.METERS + "\"))}");
        } else {
            request = UpdateFactory.create("CLEAR ALL");
        }
        UpdateExecutionFactory.createRemote(
                request, endpoint, authenticator).execute();
    }

    public void uploadFile(Path path) {
        Model m = FileManager.get().loadModel(path.toString());
        DatasetAccessorFactory.createHTTP(
                CONFIG.sparqlUpload(), authenticator).add(m);
    }

    public void save(Observation o) {
        cache.add(o);
        batchSize++;

        if (batchSize >= CONFIG.sparqlMaxBatchSize()) {
            try {
                logger.debug("Flushing the cache...");
                UpdateRequest request = UpdateFactory.create(modelToQuery(cache));
                UpdateExecutionFactory.createRemote(
                        request, endpoint, authenticator).execute();
                batchSize = 0;
                cache.clear();
            } catch (IOException ex) {
                logger.warn(ex.getMessage(), ex);
            }
        }
    }

    private String modelToQuery(List<Observation> obs) throws IOException {
        String prefixes = "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n"
                + "PREFIX xsd: <http://www.w3.org/2001/XMLSchema#>";
        String query = prefixes + "\nINSERT DATA {\n";
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
