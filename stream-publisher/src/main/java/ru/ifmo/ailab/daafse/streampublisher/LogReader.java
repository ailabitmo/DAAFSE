package ru.ifmo.ailab.daafse.streampublisher;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonReader;

public class LogReader implements Runnable {

    private final static int SLEEP = 500;
    private final Path file;
    private final ObservationListener listener;

    public LogReader(Path file, ObservationListener listener) {
        this.file = file;
        this.listener = listener;
    }

    @Override
    public void run() {
        try (BufferedReader reader = Files.newBufferedReader(file)) {
            String line;
            while ((line = reader.readLine()) != null) {
                try (JsonReader jr = Json.createReader(new StringReader(line))) {
                    JsonObject obj = jr.readObject();
                    if (obj.containsKey("type")
                            && obj.containsKey("SerialNumber__")) {
                        Observation o = new Observation(
                                obj.getString("type"),
                                obj.getInt("SerialNumber__"),
                                System.currentTimeMillis(),
                                new double[]{
                                    obj.getJsonNumber("Now_Voltage_Phase_1_value").doubleValue(),
                                    obj.getJsonNumber("Now_Voltage_Phase_2_value").doubleValue(),
                                    obj.getJsonNumber("Now_Voltage_Phase_3_value").doubleValue()
                                }
                        );
                        listener.newObservation(o);
                    }
                }
                Thread.sleep(SLEEP);
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        } catch (InterruptedException ex) {
            ex.printStackTrace();
        }
    }

}
