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
import static ru.ifmo.ailab.daafse.streampublisher.utils.ParserUtils.*;

public class LogReader implements Runnable {

    private final static Logger logger = LoggerFactory.getLogger(
            LogReader.class);
    private final Path file;
    private final ObservationListener listener;
    private long previous = 0;

    public LogReader(Path file, ObservationListener listener) {
        this.file = file;
        this.listener = listener;
    }

    @Override
    public void run() {
        BufferedReader reader = null;
        try {
            reader = Files.newBufferedReader(file);
            String line;
            while (true) {
                if ((line = reader.readLine()) == null) {
                    reader = Files.newBufferedReader(file);
                    line = reader.readLine();
                    logger.debug("Reloaded the source file.");
                }

                try (JsonReader jr = Json.createReader(new StringReader(line))) {
                    JsonObject obj = jr.readObject();

                    final String type = obj.getString("type");
                    final int serialNumber = obj.getInt("SerialNumber__");
                    final long historicalTime = toTimestamp(
                            obj.getString(DAY), obj.getInt(HOUR),
                            obj.getInt(MINUTE), obj.getInt(SECONDS));
                    final long currentTime = System.currentTimeMillis();
                    
                    VoltageObservation o = new VoltageObservation(
                            type, serialNumber, currentTime,
                            new double[]{
                                obj.getJsonNumber("Now_Voltage_Phase_1_value").doubleValue(),
                                obj.getJsonNumber("Now_Voltage_Phase_2_value").doubleValue(),
                                obj.getJsonNumber("Now_Voltage_Phase_3_value").doubleValue()
                            }
                    );
                    PowerObservation po = new PowerObservation(
                            type, serialNumber, currentTime, new double[]{
                                obj.getJsonNumber("Now_Power_P_Phase_1_value").doubleValue(),
                                obj.getJsonNumber("Now_Power_P_Phase_2_value").doubleValue(),
                                obj.getJsonNumber("Now_Power_P_Phase_3_value").doubleValue()
                            });
                    
                    if(previous != 0 && (historicalTime - previous > 100)) {
                        Thread.sleep(historicalTime - previous);
                    }
                    
                    previous = historicalTime;
                    
                    listener.newObservation(o);
                    listener.newObservation(po);
                }
            }
        } catch (IOException | InterruptedException ex) {
            logger.warn(ex.getMessage(), ex);
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException ex) {
                    logger.warn(ex.getMessage(), ex);
                }
            }
        }
    }

}
