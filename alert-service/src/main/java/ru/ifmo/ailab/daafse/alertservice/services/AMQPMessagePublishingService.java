package ru.ifmo.ailab.daafse.alertservice.services;

import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.DefaultConsumer;
import com.rabbitmq.client.Envelope;
import java.io.IOException;
import java.io.StringReader;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import org.deri.cqels.engine.RDFStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.ifmo.ailab.daafse.alertservice.CQELSEngine;
import ru.ifmo.ailab.daafse.alertservice.MessagePublishingService;
import ru.ifmo.ailab.daafse.alertservice.StreamURI;

public class AMQPMessagePublishingService implements MessagePublishingService {

    private static final Logger logger = LoggerFactory.getLogger(AMQPMessagePublishingService.class);
    private static final String EXCHANGE_TYPE = "topic";
    private final Map<URI, ConnectionFactory> factories = new HashMap<>();
    private final Map<URI, Connection> connections = new HashMap<>();
    private final Map<StreamURI, Channel> channels = new HashMap<>();
    private final CQELSEngine cqelsEngine;

    public AMQPMessagePublishingService(CQELSEngine cqelsEngine) {
        this.cqelsEngine = cqelsEngine;
    }

    @Override
    public void destroy() {
        channels.forEach((k, v) -> {
            try {
                if (v.isOpen()) {
                    v.close();
                } else {
                    v.abort();
                }
            } catch (IOException ex) {
                logger.debug(ex.getMessage(), ex);
            }
        });
        connections.forEach((k, v) -> {
            try {
                if (v.isOpen()) {
                    v.close();
                } else {
                    v.abort();
                }
            } catch (IOException ex) {
                logger.debug(ex.getMessage(), ex);
            }
        });
        logger.debug("service has been destroyed");
    }

    @Override
    public void publish(final StreamURI uri, final String message) {
        try {
            final Channel channel = getOrCreateChannel(uri);
            final String[] topic = uri.getTopic().split(":");
            channel.basicPublish(topic[0], topic[1],
                    null, message.getBytes());
        } catch (Exception ex) {
            logger.warn(ex.getMessage(), ex);
        }
    }

    @Override
    public void register(final StreamURI uri) {
        if (channels.containsKey(uri) && channels.get(uri).isOpen()) {
            logger.debug("Stream [{}] is already being read!", uri);
            return;
        }
        try {
            final Channel channel = getOrCreateChannel(uri);
            channel.exchangeDeclare(uri.getTopic(), EXCHANGE_TYPE);
            final String queueName = channel.queueDeclare().getQueue();
            final String[] topic = uri.getTopic().split(":");
            channel.queueBind(queueName, topic[0], topic[1]);
            channel.basicConsume(queueName,
                    new ObservationConsumer(channel, uri));
            channels.put(uri, channel);
            logger.debug("Started reading [{}] stream", uri);
        } catch (Exception ex) {
            logger.error(ex.getMessage(), ex);
        }
    }

    @Override
    public void unregister(final StreamURI uri) {
        if (channels.containsKey(uri)) {
            try {
                final Channel c = channels.get(uri);
                if (c.isOpen()) {
                    c.close();
                } else {
                    c.abort();
                }
                logger.debug("Stopped reading [{}] stream", uri);
            } catch (IOException ex) {
                logger.warn(ex.getMessage(), ex);
            }
        } else {
            logger.debug("Stream [{}] is not being read!", uri);
        }
    }

    public Channel getOrCreateChannel(final StreamURI uri)
            throws Exception {
        ConnectionFactory cf;
        Connection connection;
        if (channels.containsKey(uri) && channels.get(uri).isOpen()) {
            return channels.get(uri);
        } else {
            if (!factories.containsKey(uri.getServerURI())) {
                cf = new ConnectionFactory();
                cf.setUri(uri.getServerURI());
                factories.put(uri.getServerURI(), cf);

                connection = cf.newConnection();
                connections.put(uri.getServerURI(), connection);
            } else {
                cf = factories.get(uri.getServerURI());
                if (!connections.containsKey(uri.getServerURI())) {
                    connection = cf.newConnection();
                    connections.put(uri.getServerURI(), connection);
                } else {
                    connection = connections.get(uri.getServerURI());
                }
            }
            Channel channel = connection.createChannel();
            channel.exchangeDeclare(uri.getTopic(), EXCHANGE_TYPE);
            channels.put(uri, channel);
            return channel;
        }
    }

    private class ObservationConsumer extends DefaultConsumer {

        private final RDFStream stream;
        private final StreamURI uri;

        public ObservationConsumer(final Channel channel, final StreamURI uri) {
            super(channel);
            this.uri = uri;
            this.stream = new RDFStream(cqelsEngine.getContext(),
                    uri.toString()) {

                        @Override
                        public void stop() {
                            logger.debug("RDFStream.stop() has been called!");
                        }
                    };
        }

        @Override
        public void handleDelivery(String consumerTag, Envelope envelope,
                AMQP.BasicProperties properties, byte[] body)
                throws IOException {
            Model model = ModelFactory.createDefaultModel().read(
                    new StringReader(new String(body)), null, "TTL");
            StmtIterator i = model.listStatements();
            while (i.hasNext()) {
                Triple triple = i.nextStatement().asTriple();
//                logger.debug("{}", triple);
                stream.stream(triple);
            }
        }

    }

}
