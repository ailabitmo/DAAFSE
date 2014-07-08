package ru.ifmo.ailab.daafse.alertservice.models;

import java.util.concurrent.atomic.AtomicInteger;

public class Query {

    private static final AtomicInteger incID = new AtomicInteger(0);
    
    private final long id;
    private final String query;
    private final String name;

    public Query(String name, String query) {
        this.name = name;
        this.query = query;
        this.id = incID.incrementAndGet();
    }
    
    public long getId() {
        return id;
    }

    public String getQuery() {
        return query;
    }

    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        return "QUERY [" + name + "]:\n" + query;
    }

}
