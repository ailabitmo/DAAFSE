package ru.ifmo.ailab.daafse.alertservice.services;

import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.sparql.core.Var;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import javax.inject.Inject;
import javax.inject.Singleton;
import org.deri.cqels.data.Mapping;
import org.deri.cqels.engine.ConstructListener;
import org.deri.cqels.engine.ContinuousConstruct;
import org.deri.cqels.engine.ContinuousSelect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.ifmo.ailab.daafse.alertservice.CQELSEngine;
import ru.ifmo.ailab.daafse.alertservice.QueryExecutorService;

@Singleton
public class QueryExecutorServiceImpl implements QueryExecutorService {

    private static final Logger logger = LoggerFactory.getLogger(
            QueryExecutorServiceImpl.class);

    @Inject
    private CQELSEngine cqelsEngine;

    @Override
    public void loadDataset(final String graph, final String uri) {
        if (graph != null && !graph.isEmpty()) {
            cqelsEngine.getContext().loadDataset(graph, uri);
        } else {
            cqelsEngine.getContext().loadDefaultDataset(uri);
        }
    }

    @Override
    public int registerSelect(final String query) {
        ContinuousSelect select = cqelsEngine.getContext().registerSelect(query);
        select.register((Mapping mapping) -> {
            String result = "";
            for (Iterator<Var> vars = mapping.vars(); vars.hasNext();) {
                final long t = mapping.get(vars.next());
                if (t > 0) {
                    result += " " + mapping.getCtx().engine().decode(t);
                }
            }
            logger.debug("{}", result);
        });
        return 0;
    }
    
    @Override
    public int registerConstruct(final String query) {
        ContinuousConstruct construct = cqelsEngine.getContext()
                .registerConstruct(query);
        construct.register(new ConstructListener(cqelsEngine.getContext()) {
            
            @Override
            public void update(List<Triple> graph) {
                System.out.println(Arrays.toString(graph.toArray()));
            }
        });
        return 0;
    }

    @Override
    public void unregister(int queryId) {
        logger.debug("The unregister method has been called!");
    }

}
