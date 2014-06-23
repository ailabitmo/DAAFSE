package ru.ifmo.ailab.daafse.streampublisher.utils;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.ResourceFactory;
import com.hp.hpl.jena.vocabulary.RDFS;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonReader;
import ru.ifmo.ailab.daafse.streampublisher.namespaces.DAAFSE;

public class ExtractMeters {

    private static final Map<String, Meter> meters = new HashMap<>();
    private static final Model model = ModelFactory.createDefaultModel();

    public static void main(String[] args) {
        if (args.length > 0 && args[0] != null) {
            Path file = new File(args[0]).toPath();

            try (BufferedReader reader = Files.newBufferedReader(file)) {
                String line;
                while ((line = reader.readLine()) != null) {
                    try (JsonReader jr = Json.createReader(new StringReader(line))) {
                        JsonObject obj = jr.readObject();
                        if (obj.containsKey("type")
                                && obj.containsKey("SerialNumber__")) {
                            String type = obj.getString("type");
                            String serial = obj.getJsonNumber("SerialNumber__").toString();
                            if (type.equalsIgnoreCase("mercury230")
                                    && !meters.containsKey(serial)) {
                                meters.put(serial, new Meter(serial));
                            }
                        }
                    }
                }
            } catch (IOException ex) {
                ex.printStackTrace();
            }

            for (String key : meters.keySet()) {
                Meter meter = meters.get(key);
                model.setNsPrefix("em", DAAFSE.BASE);
                Resource r = model.createResource(meter.uri, DAAFSE.Mercury230);
                r.addLiteral(DAAFSE.hasSerialNumber,
                        ResourceFactory.createPlainLiteral(meter.serialNumber));
                r.addProperty(DAAFSE.hasStream, meter.stream);
            }
            
            try(FileWriter writer = new FileWriter(new File("meters.ttl"))){
                model.write(writer, "TTL");
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        } else {
            System.out.println("Source file is not set!");
        }
    }

    private static class Meter {

        private static final String METERS = "http://purl.org/daafse/meters/";

        String uri;
        String serialNumber;
        Resource stream;

        Meter(String serialNumber) {
            this.uri = METERS + "mercury230_" + serialNumber;
            this.serialNumber = serialNumber;
            this.stream = ResourceFactory.createResource(
                    "amqp://192.168.134.114?exchangeName=meter_exchange&routingKey=meter.location.mercury230_" 
                            + serialNumber);
        }

    }
}
