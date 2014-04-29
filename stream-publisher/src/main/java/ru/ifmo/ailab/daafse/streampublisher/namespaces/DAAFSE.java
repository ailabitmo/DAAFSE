package ru.ifmo.ailab.daafse.streampublisher.namespaces;

import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.Resource;

public class DAAFSE extends Namespace{
    
    public static final String BASE = "http://purl.org/daafse/electricmeters#";
    
    public static final Resource PolyphaseVoltageObservation = resource(BASE, 
            "PolyphaseVoltageObservation");
    public static final Resource PolyphaseVoltageSensorOutput = resource(BASE, 
            "PolyphaseVoltageSensorOutput");
    public static final Resource PolyphaseVoltageValue = resource(BASE, 
            "PolyphaseVoltageValue");
    public static final Property hasPhaseNumber = property(BASE, "hasPhaseNumber");
    public static final Property hasQuantityValue = property(BASE, "hasQuantityValue");
    
}
