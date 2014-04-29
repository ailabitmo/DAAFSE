package ru.ifmo.ailab.daafse.registry.client;

import ru.ifmo.ailab.daafse.bus.StreamID;

import java.io.IOException;
import java.util.List;

/**
 * Created by oscii on 29/04/14.
 */
public interface ConsumerRegistry {
    public List<StreamID> getAvailableStreams() throws IOException, InterruptedException;
}
