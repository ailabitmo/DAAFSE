package ru.ifmo.ailab.daafse.alertservice;

import java.io.File;
import javax.annotation.PostConstruct;
import javax.inject.Singleton;
import org.deri.cqels.engine.ExecContext;

@Singleton
public class CQELSEngineImpl implements CQELSEngine {

    private static final String CQELS_HOME = "cqels_home";
    private static ExecContext context;
    
    @PostConstruct
    public void postConstruct() {
        File home = new File(CQELS_HOME);
        if(!home.exists()) {
            home.mkdir();
        }
        context = new ExecContext(CQELS_HOME, true);
    }

    @Override
    public ExecContext getContext() {
        synchronized (CQELSEngineImpl.class) {
            return context;
        }
    }

}
