package ru.ifmo.ailab.daafse.streampublisher.observations;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import java.time.format.DateTimeFormatter;

public abstract class Observation {
    
    public static final String OBSERVATIONS = "http://purl.org/daafse/observations/";
    public static final String METERS = "http://purl.org/daafse/meters/";
    public static final String OBSERVATION_RESULTS = "http://purl.org/daafse/observations/results/";
    public static final String OBSERVATION_VALUES = "http://purl.org/daafse/observations/results/values/";
    public static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ISO_DATE_TIME;

    private final Model model = ModelFactory.createDefaultModel();
    private final String meterId;
    private final long timestamp;
    private final String uriPrefix;

    public Observation(final String meterType, final int meterSerialNumber, 
            final long timestamp, final String uriPrefix) {
        this.meterId = meterType + "_" + meterSerialNumber;
        this.timestamp = timestamp;
        this.uriPrefix = uriPrefix;
    }
    
    public long getTimestamp() {
        return timestamp;
    }
    
    public Model getModel() {
        return model;
    }
    
    public String getMeterId() {
        return meterId;
    }
    
    public String getMeterURI() {
        return createMeterURI();
    }
    
    protected String createMeterURI() {
        return METERS + meterId;
    }
    
    public String createObservationURI() {
        return OBSERVATIONS + uriPrefix + meterId + "-" + timestamp;
    }
    
    protected String createObservationResultURI() {
        return OBSERVATION_RESULTS + uriPrefix + meterId + "-" + timestamp;
    }

    protected String createObservationValueURI(int phase) {
        return OBSERVATION_VALUES + uriPrefix + meterId + "-" + timestamp 
                + "-phase-" + phase;
    }
    
}
