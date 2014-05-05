package ru.ifmo.ailab.daafse.messagebus.registry.protocol;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by oscii on 29/04/14.
 */
public enum RegistryProtocol {
    PUBLISH, UNPUBLISH, GET_STREAMS, STREAMS;

    private static Map<String, RegistryProtocol> stringToWords = new HashMap<>();

    static {
        stringToWords.put("PUBLISH", RegistryProtocol.PUBLISH);
        stringToWords.put("UNPUBLISH", RegistryProtocol.UNPUBLISH);
        stringToWords.put("GET_STREAMS", RegistryProtocol.GET_STREAMS);
        stringToWords.put("STREAMS", RegistryProtocol.STREAMS);
    }

    public static RegistryProtocol fromString(String input) {
        if (stringToWords.containsKey(input)) {
            return stringToWords.get(input);
        } else {
            return null;
        }
    }
}
