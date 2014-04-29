package ru.ifmo.ailab.daafse.streampublisher;

import java.nio.file.Path;
import java.nio.file.Paths;


public class Publisher implements ObservationListener {

    public static void main(String[] args) {
        Path log = Paths.get(args[0]);
        LogReader lr = new LogReader(log, new Publisher());
        lr.run();
    }

    @Override
    public void newObservation(Observation observation) {
        observation.getModel().write(System.out, "TTL");
    }

}
