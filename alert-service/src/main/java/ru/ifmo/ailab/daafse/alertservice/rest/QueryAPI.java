package ru.ifmo.ailab.daafse.alertservice.rest;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.ifmo.ailab.daafse.alertservice.QueryExecutorService;
import ru.ifmo.ailab.daafse.alertservice.models.Query;

@Path("query")
public class QueryAPI {

    private static final Logger logger = LoggerFactory.getLogger(
            QueryAPI.class);
    private static final List<Query> queries
            = Collections.synchronizedList(new ArrayList<>());

    @Inject
    private QueryExecutorService qes;

    @POST
    @Path("register")
    public void register(@QueryParam("query") String queryStr,
            @QueryParam("name") String name) {
        if (queryStr != null && name != null) {
            try {
                Query query = new Query(name, queryStr);
                
                logger.debug("Number of registered queries: {}", queries.size());
                logger.debug("{}", query);
                
                qes.registerConstruct(query.getQuery());
                
                queries.add(query);
            } catch (Exception ex) {
                throw new WebApplicationException(ex.getMessage(), ex,
                        Response.Status.BAD_REQUEST);
            }
        } else {
            throw new WebApplicationException(Response.Status.BAD_REQUEST);
        }
    }

    @GET
    @Path("select")
    public void select() {
        System.out.println("select");
    }

    @GET
    @Produces("application/json")
    public List<Query> getAllRegisteredQueries() {
        logger.debug("Number of registered queries: {}", queries.size());
        return queries;
    }

}
