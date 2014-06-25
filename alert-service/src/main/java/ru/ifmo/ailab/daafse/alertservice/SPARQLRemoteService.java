package ru.ifmo.ailab.daafse.alertservice;

import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.Model;

public interface SPARQLRemoteService {
    
    public ResultSet select(final String query);
    
    public Model construct(final String query);
    
}
