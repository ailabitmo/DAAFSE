package ru.ifmo.ailab.daafse.streampublisher;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.update.UpdateExecutionFactory;
import com.hp.hpl.jena.update.UpdateFactory;
import com.hp.hpl.jena.update.UpdateRequest;
import java.io.IOException;
import java.io.StringWriter;

public class Store {

    private final String endpoint;

    public Store(final String endpoint) {
        this.endpoint = endpoint;
    }
    
    public void clearAll() {
        UpdateRequest request = UpdateFactory.create("CLEAR ALL");
        UpdateExecutionFactory.createRemote(request, endpoint).execute();
    }

    public void save(Observation o) {
        UpdateRequest request = UpdateFactory.create(
                modelToQuery(o.getMeterURI(), o.getModel()));
        UpdateExecutionFactory.createRemote(request, endpoint).execute();
    }
    
    private String modelToQuery(String graphUri, Model model) {
        String query = "INSERT DATA { GRAPH <" + graphUri + "> {\n";
        
        try (StringWriter writer = new StringWriter()) {
            model.write(writer, "N3");
            query += writer.toString();
        } catch (IOException ex) {
        }
        query += "\n}}";
        return query;
    }

}
