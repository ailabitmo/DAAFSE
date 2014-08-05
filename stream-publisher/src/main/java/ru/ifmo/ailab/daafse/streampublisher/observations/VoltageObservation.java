package ru.ifmo.ailab.daafse.streampublisher.observations;

import com.hp.hpl.jena.datatypes.xsd.XSDDatatype;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.ResourceFactory;
import java.time.Instant;
import ru.ifmo.ailab.daafse.streampublisher.namespaces.DAAFSE;
import ru.ifmo.ailab.daafse.streampublisher.namespaces.SSN;

public class VoltageObservation extends Observation {

    private final Resource observationResult;
    private final Resource meter;
    private final Resource firstPhaseValue;
    private final Resource secondPhaseValue;
    private final Resource thirdPhaseValue;

    public VoltageObservation(final String meterType,
            final int meterSerialNumber, final long timestamp,
            final double[] volateValues) {
        super(meterType, meterSerialNumber, timestamp, "#voltage-");
        
        
        this.meter = getModel().createResource(createMeterURI());

        this.firstPhaseValue = getModel().createResource(
                createObservationValueURI(1),
                DAAFSE.PolyphaseVoltageValue)
                .addLiteral(DAAFSE.hasPhaseNumber, ResourceFactory
                        .createTypedLiteral("1", XSDDatatype.XSDinteger))
                .addLiteral(DAAFSE.hasQuantityValue, volateValues[0]);
        this.secondPhaseValue = getModel().createResource(
                createObservationValueURI(2),
                DAAFSE.PolyphaseVoltageValue)
                .addLiteral(DAAFSE.hasPhaseNumber, ResourceFactory
                        .createTypedLiteral("2", XSDDatatype.XSDinteger))
                .addLiteral(DAAFSE.hasQuantityValue, volateValues[1]);
        this.thirdPhaseValue = getModel().createResource(
                createObservationValueURI(3),
                DAAFSE.PolyphaseVoltageValue)
                .addLiteral(DAAFSE.hasPhaseNumber, ResourceFactory
                        .createTypedLiteral("3", XSDDatatype.XSDinteger))
                .addLiteral(DAAFSE.hasQuantityValue, volateValues[2]);

        this.observationResult = getModel().createResource(
                createObservationResultURI(),
                DAAFSE.PolyphaseVoltageSensorOutput)
                .addProperty(SSN.isProducedBy, this.meter)
                .addProperty(SSN.hasValue, this.firstPhaseValue)
                .addProperty(SSN.hasValue, this.secondPhaseValue)
                .addProperty(SSN.hasValue, this.thirdPhaseValue);

        getModel().createResource(createObservationURI(),
                DAAFSE.PolyphaseVoltageObservation)
                .addProperty(SSN.observedBy, this.meter)
                .addProperty(SSN.observationResult, this.observationResult)
                .addLiteral(SSN.observationResultTime, 
                        ResourceFactory.createTypedLiteral(
                                Instant.ofEpochMilli(timestamp).toString(), 
                                XSDDatatype.XSDdateTime));
    }

}
