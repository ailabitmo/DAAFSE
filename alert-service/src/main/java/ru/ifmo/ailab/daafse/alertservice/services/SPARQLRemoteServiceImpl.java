package ru.ifmo.ailab.daafse.alertservice.services;

import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.Model;
import javax.inject.Singleton;
import org.aeonbits.owner.ConfigFactory;
import ru.ifmo.ailab.daafse.alertservice.SPARQLRemoteService;
import ru.ifmo.ailab.daafse.alertservice.config.ServiceConfig;

@Singleton
public class SPARQLRemoteServiceImpl implements SPARQLRemoteService {

    private static final ServiceConfig CONFIG = ConfigFactory.create(
            ServiceConfig.class);

    @Override
    public Model construct(String query) {
        Query q = QueryFactory.create(query);
        Model model = QueryExecutionFactory
                .createServiceRequest(CONFIG.sparqlEndpointURL(), q).execConstruct();
        return model;
    }
    
    @Override
    public ResultSet select(String query) {
        Query q = QueryFactory.create(query);
        ResultSet result = QueryExecutionFactory
                .createServiceRequest(CONFIG.sparqlEndpointURL(), q).execSelect();
        return result;
    }

}
