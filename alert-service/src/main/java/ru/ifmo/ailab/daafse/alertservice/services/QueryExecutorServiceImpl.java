package ru.ifmo.ailab.daafse.alertservice.services;

import com.hp.hpl.jena.sparql.core.Var;
import java.util.Iterator;
import javax.inject.Inject;
import javax.inject.Singleton;
import org.deri.cqels.data.Mapping;
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
        cqelsEngine.getContext().loadDataset(graph, uri);
    }

    @Override
    public int register(final String query) {
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
    public void unregister(int queryId) {
        logger.debug("The unregister method has been called!");
    }

}
