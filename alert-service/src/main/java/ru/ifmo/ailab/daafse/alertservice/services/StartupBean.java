package ru.ifmo.ailab.daafse.alertservice.services;

import com.hp.hpl.jena.query.ResultSet;
import java.net.URISyntaxException;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.ifmo.ailab.daafse.alertservice.SPARQLRemoteService;
import ru.ifmo.ailab.daafse.alertservice.StreamReaderService;
import ru.ifmo.ailab.daafse.alertservice.StreamURI;

@Startup
@Singleton
public class StartupBean {

    private static final Logger logger = LoggerFactory.getLogger(
            StartupBean.class);

    @Inject
    private SPARQLRemoteService sparqlRs;
    @Inject
    private StreamReaderService streamRs;

    @PostConstruct
    void init() {
        System.out.println("initializing...");
        ResultSet results = sparqlRs.select("PREFIX em:<http://purl.org/daafse/electricmeters#>"
                + "SELECT ?streamUri {"
                + "	GRAPH <http://192.168.134.114/SmartMetersDB/> {"
                + "    	?x em:hasStream ?streamUri ."
                + "    }"
                + "}");
        while (results.hasNext()) {
            String uri = results.nextSolution().getResource("streamUri").getURI();
            try {
                System.out.println(uri);
                streamRs.startReadStream(new StreamURI(uri));
            } catch (URISyntaxException ex) {
                logger.warn(ex.getMessage(), ex);
            }
        }
        System.out.println("initialized.");
    }

    @PreDestroy
    public void destroy() {
        logger.debug("destroyed.");
    }

}
