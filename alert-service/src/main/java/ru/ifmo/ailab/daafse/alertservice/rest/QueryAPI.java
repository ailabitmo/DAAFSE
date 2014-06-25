package ru.ifmo.ailab.daafse.alertservice.rest;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.ifmo.ailab.daafse.alertservice.QueryExecutorService;

@Path("query")
public class QueryAPI {
    private static final Logger logger = LoggerFactory.getLogger(
            QueryAPI.class);

    @Inject
    private QueryExecutorService qes;

    @POST
    @Path("register")
    public void register(@QueryParam("query") String query) {
        logger.debug(query);
        if (query != null) {
            qes.registerConstruct(query);
        }
    }

    @GET
    @Path("select")
    public void select() {
        System.out.println("select");
    }

}
