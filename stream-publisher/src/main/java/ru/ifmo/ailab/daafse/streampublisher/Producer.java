package ru.ifmo.ailab.daafse.streampublisher;

import com.rabbitmq.client.AlreadyClosedException;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.ShutdownListener;
import com.rabbitmq.client.ShutdownSignalException;
import java.io.IOException;
import java.net.URI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Producer implements ShutdownListener {

    private static final Logger logger = LoggerFactory.getLogger(Producer.class);
    private final URI serverUri;
    private final String exchangeName;
    private volatile boolean connected = false;
    private Connection connection;
    private Channel channel;

    public Producer(final URI serverUri, final String exchangeName) {
        this.serverUri = serverUri;
        this.exchangeName = exchangeName;
    }

    public void init() {
        logger.debug("Connecting to {}", serverUri);
        ConnectionFactory factory = new ConnectionFactory();
        try {
            factory.setUri(this.serverUri);
        } catch (Exception ex) {
            logger.debug(ex.getMessage(), ex);
        }

        while (!connected) {
            try {
                connection = factory.newConnection();
                connection.addShutdownListener(this);

                channel = connection.createChannel();
                channel.exchangeDeclare(this.exchangeName, "topic");

                connected = true;
                logger.debug("Connected to AMQP broker!");
            } catch (IOException ex) {
                logger.warn(
                        "Connection failed, will try to connect in a 5 secs!");
                connected = false;
                try {
                    Thread.sleep(5000);
                } catch (InterruptedException ie) {
                    logger.debug(
                            "The thread was interrupted while reconnection timeout!");
                }
            }
        }
    }

    public void publish(final String routingKey, final byte[] data) throws IOException {
        try {
            if(connected) {
                channel.basicPublish(exchangeName, routingKey, null, data);
            } else {
                logger.warn("Not connected! Ignoring message...");
            }
        } catch (AlreadyClosedException ex) {
            logger.debug(ex.getMessage(), ex);
        }
    }

    public void close() {
        connected = false;
        try {
            if (channel != null && channel.isOpen()) {
                channel.close();
                channel = null;
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        try {
            if (connection != null && connection.isOpen()) {
                connection.close();
                connection = null;
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public void shutdownCompleted(ShutdownSignalException cause) {
        logger.debug("Received ShutDownSignalException. Reconnecting...");
        close();
        init();
    }

}
