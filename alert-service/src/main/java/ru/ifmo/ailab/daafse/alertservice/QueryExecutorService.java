package ru.ifmo.ailab.daafse.alertservice;

public interface QueryExecutorService {

    public int register(final String query);
    
    public void unregister(int queryId);
    
}
