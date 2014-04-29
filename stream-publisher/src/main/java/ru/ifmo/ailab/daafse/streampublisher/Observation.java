package ru.ifmo.ailab.daafse.streampublisher;

import com.hp.hpl.jena.datatypes.xsd.XSDDatatype;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.ResourceFactory;
import com.hp.hpl.jena.vocabulary.XSD;
import ru.ifmo.ailab.daafse.streampublisher.namespaces.DAAFSE;
import ru.ifmo.ailab.daafse.streampublisher.namespaces.SSN;

public class Observation {

    private static final String OBSERVATIONS = "http://purl.org/daafse/observations/";
    private static final String METERS = "http://purl.org/daafse/meters/";
    private static final String OBSERVATION_RESULTS = "http://purl.org/daafse/observations/results/";
    private static final String OBSERVATION_VALUES = "http://purl.org/daafse/observations/results/values/";
    private final Model model = ModelFactory.createDefaultModel();
    private final Resource observation;
    private final Resource observationResult;
    private final Resource meter;
    private final Resource firstPhaseValue;
    private final Resource secondPhaseValue;
    private final Resource thirdPhaseValue;

    public Observation(final String meterType,
            final int meterSerialNumber, final long timestamp,
            final double[] phaseValues) {
        String meterId = meterType + "_" + meterSerialNumber;
        this.meter = model.createResource(createMeterURI(meterId));

        this.firstPhaseValue = model.createResource(
                createObservationValueURI(meterId, timestamp, 1),
                DAAFSE.PolyphaseVoltageValue)
                .addLiteral(DAAFSE.hasPhaseNumber, ResourceFactory
                        .createTypedLiteral("1", XSDDatatype.XSDinteger))
                .addLiteral(DAAFSE.hasQuantityValue, phaseValues[0]);
        this.secondPhaseValue = model.createResource(
                createObservationValueURI(meterId, timestamp, 2),
                DAAFSE.PolyphaseVoltageValue)
                .addLiteral(DAAFSE.hasPhaseNumber, ResourceFactory
                        .createTypedLiteral("2", XSDDatatype.XSDinteger))
                .addLiteral(DAAFSE.hasQuantityValue, phaseValues[1]);
        this.thirdPhaseValue = model.createResource(
                createObservationValueURI(meterId, timestamp, 3),
                DAAFSE.PolyphaseVoltageValue)
                .addLiteral(DAAFSE.hasPhaseNumber, ResourceFactory
                        .createTypedLiteral("3", XSDDatatype.XSDinteger))
                .addLiteral(DAAFSE.hasQuantityValue, phaseValues[2]);

        this.observationResult = model.createResource(
                createObservationResultURI(meterId, timestamp),
                DAAFSE.PolyphaseVoltageSensorOutput)
                .addProperty(SSN.isProducedBy, this.meter)
                .addProperty(SSN.hasValue, this.firstPhaseValue)
                .addProperty(SSN.hasValue, this.secondPhaseValue)
                .addProperty(SSN.hasValue, this.thirdPhaseValue);

        this.observation = model.createResource(createObservationURI(meterId, timestamp),
                DAAFSE.PolyphaseVoltageObservation)
                .addProperty(SSN.observedBy, this.meter)
                .addProperty(SSN.observationResult, this.observationResult)
                .addLiteral(SSN.observationResultTime, timestamp);
    }

    private String createObservationURI(String meterId, long timestamp) {
        return OBSERVATIONS + "#voltage-" + meterId + "-" + timestamp;
    }

    private String createMeterURI(String meterId) {
        return METERS + meterId;
    }

    private String createObservationResultURI(String meterId, long timestamp) {
        return OBSERVATION_RESULTS + "#voltage-" + meterId + "-" + timestamp;
    }

    private String createObservationValueURI(String meterId, long timestamp, int phase) {
        return OBSERVATION_VALUES + "#voltage-" + meterId + "-" + timestamp + "-phase-" + phase;
    }

    public Model getModel() {
        return model;
    }

}
