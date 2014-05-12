package ru.ifmo.ailab.daafse.alertservice;

import javax.inject.Singleton;
import org.deri.cqels.engine.ExecContext;

@Singleton
public class CQELSEngineImpl implements CQELSEngine {

    private static final ExecContext CONTEXT
            = new ExecContext("cqels_home", true);

    @Override
    public ExecContext getContext() {
        synchronized (CQELSEngineImpl.class) {
            return CONTEXT;
        }
    }

}
