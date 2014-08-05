package ru.ifmo.ailab.daafse.streampublisher.observations;

import com.hp.hpl.jena.datatypes.xsd.XSDDatatype;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.ResourceFactory;
import java.time.Instant;
import ru.ifmo.ailab.daafse.streampublisher.namespaces.DAAFSE;
import ru.ifmo.ailab.daafse.streampublisher.namespaces.SSN;

public class PowerObservation extends Observation {
    
    private final Resource observationResult;
    private final Resource meter;
    private final Resource firstPhaseValue;
    private final Resource secondPhaseValue;
    private final Resource thirdPhaseValue;

    public PowerObservation(String meterType, int meterSerialNumber, 
            long timestamp, double[] powerValues) {
        super(meterType, meterSerialNumber, timestamp, "#power-");
        
        
        this.meter = getModel().createResource(createMeterURI());

        this.firstPhaseValue = getModel().createResource(
                createObservationValueURI(1),
                DAAFSE.PolyphasePowerValue)
                .addLiteral(DAAFSE.hasPhaseNumber, ResourceFactory
                        .createTypedLiteral("1", XSDDatatype.XSDinteger))
                .addLiteral(DAAFSE.hasQuantityValue, powerValues[0]);
        this.secondPhaseValue = getModel().createResource(
                createObservationValueURI(2),
                DAAFSE.PolyphasePowerValue)
                .addLiteral(DAAFSE.hasPhaseNumber, ResourceFactory
                        .createTypedLiteral("2", XSDDatatype.XSDinteger))
                .addLiteral(DAAFSE.hasQuantityValue, powerValues[1]);
        this.thirdPhaseValue = getModel().createResource(
                createObservationValueURI(3),
                DAAFSE.PolyphasePowerValue)
                .addLiteral(DAAFSE.hasPhaseNumber, ResourceFactory
                        .createTypedLiteral("3", XSDDatatype.XSDinteger))
                .addLiteral(DAAFSE.hasQuantityValue, powerValues[2]);

        this.observationResult = getModel().createResource(
                createObservationResultURI(),
                DAAFSE.PolyphasePowerSensorOutput)
                .addProperty(SSN.isProducedBy, this.meter)
                .addProperty(SSN.hasValue, this.firstPhaseValue)
                .addProperty(SSN.hasValue, this.secondPhaseValue)
                .addProperty(SSN.hasValue, this.thirdPhaseValue);

        getModel().createResource(createObservationURI(),
                DAAFSE.PolyphasePowerObservation)
                .addProperty(SSN.observedBy, this.meter)
                .addProperty(SSN.observationResult, this.observationResult)
                .addLiteral(SSN.observationResultTime, 
                        ResourceFactory.createTypedLiteral(
                                Instant.ofEpochMilli(timestamp).toString(), 
                                XSDDatatype.XSDdateTime));
    }
    
}
