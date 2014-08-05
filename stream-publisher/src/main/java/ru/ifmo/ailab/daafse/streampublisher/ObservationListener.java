package ru.ifmo.ailab.daafse.streampublisher;

import ru.ifmo.ailab.daafse.streampublisher.observations.Observation;


public interface ObservationListener {

    public void newObservation(Observation observation);
}
