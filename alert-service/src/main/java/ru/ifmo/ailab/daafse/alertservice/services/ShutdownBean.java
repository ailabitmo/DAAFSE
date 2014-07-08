package ru.ifmo.ailab.daafse.alertservice.services;

import javax.inject.Inject;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;
import ru.ifmo.ailab.daafse.alertservice.CQELSEngine;

@WebListener
public class ShutdownBean implements ServletContextListener {

    @Inject
    private CQELSEngine ce;
    
    @Override
    public void contextInitialized(ServletContextEvent sce) {
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
    }
    
}
