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
    public static final Resource Mercury230 = resource(BASE, "Mercury230");
    public static final Property hasPhaseNumber = property(BASE, "hasPhaseNumber");
    public static final Property hasQuantityValue = property(BASE, "hasQuantityValue");
    public static final Property hasSerialNumber = property(BASE, "hasSerialNumber");
    public static final Property hasStream = property(BASE, "hasStream");
    
}
