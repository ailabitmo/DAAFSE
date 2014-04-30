package daafse.registry.client;

import daafse.bus.StreamID;

/**
 * Created by oscii on 29/04/14.
 */
public interface ProducerRegistry {
    public boolean publishStream(StreamID streamID);
    public boolean unpublishStream(StreamID streamID);

}
