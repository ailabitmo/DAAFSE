package ru.ifmo.ailab.daafse.messagebus.registry.client;

import ru.ifmo.ailab.daafse.messagebus.StreamID;

/**
 * Created by oscii on 29/04/14.
 */
public interface ProducerRegistry {
    public boolean publishStream(StreamID streamID);
    public boolean unpublishStream(StreamID streamID);

}
