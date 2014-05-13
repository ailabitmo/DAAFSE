package ru.ifmo.ailab.daafse.alertservice;

public interface QueryExecutorService {
    
    public void loadDataset(final String graph, final String uri);

    public int register(final String query);
    
    public void unregister(int queryId);
    
}
