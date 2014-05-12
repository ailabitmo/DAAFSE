package ru.ifmo.ailab.daafse.alertservice;

import java.net.URI;
import java.net.URISyntaxException;

public class StreamURI {

    private final URI serverURI;
    private final String exchangeName;
    private final String routingKey;
    
    public StreamURI(final String uri) throws URISyntaxException {
        this(new URI(uri));
    }

    public StreamURI(final URI uri) {
        URI tempServerURI = null;
        try {
            tempServerURI = new URI(
                    uri.getScheme() + "://" + uri.getAuthority() + uri.getPath());
        } catch (URISyntaxException ex) {
            ex.printStackTrace();
        }
        this.serverURI = tempServerURI;

        String tempExchangeName = null;
        String tempRoutingKey = null;
        for (String kv : uri.getQuery().split("&")) {
            String[] parts = kv.split("=");
            if (parts[0].equalsIgnoreCase("exchangeName")) {
                tempExchangeName = parts[1];
            } else if (parts[0].equalsIgnoreCase("routingKey")) {
                tempRoutingKey = parts[1];
            }
        }
        this.exchangeName = tempExchangeName;
        this.routingKey = tempRoutingKey;
    }

    public URI getServerURI() {
        return serverURI;
    }

    public String getExchangeName() {
        return exchangeName;
    }

    public String getRoutingKey() {
        return routingKey;
    }

    @Override
    public String toString() {
        return serverURI.toString() + "?exchangeName=" + exchangeName
                + "&routingKey=" + routingKey;
    }

}
