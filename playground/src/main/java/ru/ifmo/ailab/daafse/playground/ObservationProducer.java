package ru.ifmo.ailab.daafse.playground;

import ru.ifmo.ailab.daafse.messagebus.StreamID;
import ru.ifmo.ailab.daafse.messagebus.producers.AbstractProducer;
import ru.ifmo.ailab.daafse.streampublisher.Observation;
import ru.ifmo.ailab.daafse.streampublisher.ObservationListener;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 * Created by oscii on 01/05/14.
 */
public class ObservationProducer extends AbstractProducer
        implements ObservationListener {

    private ByteArrayOutputStream os = new ByteArrayOutputStream();

    public ObservationProducer(StreamID streamID)
            throws IOException, IllegalArgumentException {
        super(streamID);
    }

    @Override
    synchronized public void newObservation(Observation observation) {
        observation.getModel().write(os, "TTL");
        publish(os.toByteArray());
        os.reset();
    }
}
