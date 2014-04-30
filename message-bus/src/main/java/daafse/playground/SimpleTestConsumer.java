package daafse.playground;

import daafse.consumers.AbstractConsumer;

import java.io.IOException;

/**
 * Created by oscii on 30/04/14.
 */
class SimpleTestConsumer extends AbstractConsumer {

    public SimpleTestConsumer() throws IOException { }

    @Override
    protected void process(byte[] data) {
        System.out.println("[SimpleTestConsumer received] " + new String(data));
    }
}
