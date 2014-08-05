package ru.ifmo.ailab.daafse.streampublisher;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.nio.file.Files;
import java.nio.file.Path;
import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.ifmo.ailab.daafse.streampublisher.observations.PowerObservation;
import ru.ifmo.ailab.daafse.streampublisher.observations.VoltageObservation;

public class LogReader implements Runnable {

    private final static Logger logger = LoggerFactory.getLogger(
            LogReader.class);
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
                        final long time = System.currentTimeMillis();
                        final String type = obj.getString("type");
                        final int serialNumber = obj.getInt("SerialNumber__");
                        VoltageObservation o = new VoltageObservation(
                                type, serialNumber, time,
                                new double[] {
                                    obj.getJsonNumber("Now_Voltage_Phase_1_value").doubleValue(),
                                    obj.getJsonNumber("Now_Voltage_Phase_2_value").doubleValue(),
                                    obj.getJsonNumber("Now_Voltage_Phase_3_value").doubleValue()
                                }
                        );
                        PowerObservation po = new PowerObservation(
                                type, serialNumber, time, new double[] {
                                    obj.getJsonNumber("Now_Power_Phase_1_value").doubleValue(),
                                    obj.getJsonNumber("Now_Power_Phase_2_value").doubleValue(),
                                    obj.getJsonNumber("Now_Power_Phase_3_value").doubleValue()
                                });
                        listener.newObservation(o);
                        listener.newObservation(po);
                    }
                }
                Thread.sleep(SLEEP);
            }
        } catch (IOException ex) {
            logger.warn(ex.getMessage(), ex);
        } catch (InterruptedException ex) {
            logger.warn(ex.getMessage(), ex);
        }
    }

}
