package ru.ifmo.ailab.daafse.streampublisher;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.aeonbits.owner.ConfigFactory;
import ru.ifmo.ailab.daafse.streampublisher.config.PublisherConfig;

public class Publisher implements ObservationListener {

    private static final PublisherConfig config = ConfigFactory
            .create(PublisherConfig.class);
    private static Connection connection;
    private static Channel channel;

    public static void main(String[] args) throws Exception {
        try {
            Path log = Paths.get(args[0]);
            LogReader lr = new LogReader(log, new Publisher());

            ConnectionFactory factory = new ConnectionFactory();
            factory.setUri(config.serverURI());
            connection = factory.newConnection();

            channel = connection.createChannel();
            channel.exchangeDeclare(config.exchangeName(), "topic");

            lr.run();

        } finally {
            connection.close();
        }
    }

    @Override
    public void newObservation(Observation observation) {
        try {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            observation.getModel().write(out);
            channel.basicPublish(config.exchangeName(),
                    config.routingKey(observation.getMeterId()),
                    null, out.toByteArray());
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

}
