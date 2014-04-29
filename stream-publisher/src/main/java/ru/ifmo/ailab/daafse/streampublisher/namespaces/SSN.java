package ru.ifmo.ailab.daafse.streampublisher.namespaces;

import com.hp.hpl.jena.rdf.model.Property;

public class SSN extends Namespace {
    public static final String BASE = "http://purl.oclc.org/NET/ssnx/ssn#";
    
    public static final Property isProducedBy = property(BASE, "isProducedBy");
    public static final Property observedBy = property(BASE, "observerBy");
    public static final Property hasValue = property(BASE, "hasValue");
    public static final Property observationResultTime = property(BASE, 
            "observationResultTime");
    public static final Property observationResult = property(BASE, 
            "observationResult");
}
