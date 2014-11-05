package ru.ifmo.ailab.daafse.alertservice;

import java.net.URI;
import java.net.URISyntaxException;

public class StreamURI {

    private final URI serverURI;
    private final String topic;
    
    public StreamURI(final String uri) throws URISyntaxException {
        this(new URI(uri));
    }

    public StreamURI(final URI uri) {
        URI tempServerURI = null;
        try {
            tempServerURI = new URI(
                    uri.getScheme() + "://" + uri.getAuthority() + uri.getPath());
        } catch (URISyntaxException ex) {
            throw new IllegalArgumentException(ex);
        }
        this.serverURI = tempServerURI;

        String tempTopic = null;
        for (String kv : uri.getQuery().split("&")) {
            String[] parts = kv.split("=");
            if (parts[0].equalsIgnoreCase("topic")) {
                tempTopic = parts[1];
            }
        }
        this.topic = tempTopic;
    }

    public URI getServerURI() {
        return serverURI;
    }

    public String getTopic() {
        return topic;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        return builder.append(serverURI.toASCIIString()).append("?topic=")
                .append(topic).toString();
    }

}
