package ru.ifmo.ailab.daafse.streampublisher.utils;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import static ru.ifmo.ailab.daafse.streampublisher.utils.ParserUtils.*;

public class ObservationSorter {

    private static final Logger log
            = LoggerFactory.getLogger(ObservationSorter.class);
    private static final List<JsonObject> observations = new ArrayList<>();

    public static void main(String[] args) {
        if (args.length > 1) {
            Path source = new File(args[0]).toPath();
            Path dest = new File(args[1]).toPath();

            try (
                    BufferedWriter writer = Files.newBufferedWriter(dest,
                            StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
                    BufferedReader reader = Files.newBufferedReader(source)) {
                String line;
                while ((line = reader.readLine()) != null) {
                    try (JsonReader jr = Json.createReader(new StringReader(line))) {
                        JsonObject obj = jr.readObject();
                        if (obj.containsKey("type") && obj.containsKey("SerialNumber__")) {
                            observations.add(obj);
                        }
                    }
                }

                observations.sort((JsonObject o1, JsonObject o2) -> {
                    final long t1 = toTimestamp(
                            o1.getString(DAY), o1.getInt(HOUR),
                            o1.getInt(MINUTE), o1.getInt(SECONDS));
                    final long t2 = toTimestamp(
                            o2.getString(DAY), o2.getInt(HOUR),
                            o2.getInt(MINUTE), o2.getInt(SECONDS));

                    return Long.compare(t1, t2);
                });
                
                for (JsonObject o : observations) {
                    writer.write(o.toString());
                    writer.newLine();
                }
            } catch (IOException ex) {
                log.error(ex.getMessage(), ex);
            }

        } else {
            log.error("Source file is not set!");
        }
    }

}
