package ru.ifmo.ailab.daafse.alertservice.services;

import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.Model;
import javax.inject.Singleton;
import ru.ifmo.ailab.daafse.alertservice.SPARQLRemoteService;

@Singleton
public class SPARQLRemoteServiceImpl implements SPARQLRemoteService {

    private static final String SPARQL_ENDPOINT = 
            "http://192.168.134.114:8890/sparql";

    @Override
    public Model construct(String query) {
        Query q = QueryFactory.create(query);
        Model model = QueryExecutionFactory
                .createServiceRequest(SPARQL_ENDPOINT, q).execConstruct();
        return model;
    }
    
    @Override
    public ResultSet select(String query) {
        Query q = QueryFactory.create(query);
        ResultSet result = QueryExecutionFactory
                .createServiceRequest(SPARQL_ENDPOINT, q).execSelect();
        return result;
    }

}
