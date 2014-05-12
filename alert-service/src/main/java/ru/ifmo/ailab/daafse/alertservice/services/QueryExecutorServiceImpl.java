package ru.ifmo.ailab.daafse.alertservice.services;

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
    public int register(final String query) {
        ContinuousSelect select = cqelsEngine.getContext().registerSelect(query);
        select.register((Mapping mapping) -> {
            logger.debug("{}", 
                    mapping.getCtx().engine().decode(mapping.get(mapping.vars().next())));
        });
        return 0;
    }

    @Override
    public void unregister(int queryId) {
        logger.debug("The unregister method has been called!");
    }

}
