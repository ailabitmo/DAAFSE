package ru.ifmo.ailab.daafse.alertservice.services;

import java.net.URISyntaxException;
import javax.inject.Inject;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.Test;
import org.junit.runner.RunWith;
import ru.ifmo.ailab.daafse.alertservice.QueryExecutorService;
import ru.ifmo.ailab.daafse.alertservice.StreamReaderService;
import ru.ifmo.ailab.daafse.alertservice.StreamURI;

@RunWith(Arquillian.class)
public class StreamReaderServiceImplTest {

    @Deployment
    public static JavaArchive createDeployment() {
        return ShrinkWrap.create(JavaArchive.class)
                .addPackages(true, "ru.ifmo.ailab.daafse.alertservice")
                .addAsManifestResource(EmptyAsset.INSTANCE, "beans.xml");
    }

    @Inject
    StreamReaderService stream;
    @Inject
    QueryExecutorService qes;

    @Test
    public void simple() throws URISyntaxException, InterruptedException {
        StreamURI uri = new StreamURI(
                "amqp://192.168.134.114?exchangeName=meter_exchange&routingKey=meter.location.*");
        stream.startReadStream(uri);
        final int queryId = qes.register(
                "PREFIX ssn: <http://purl.oclc.org/NET/ssnx/ssn#>\n"
                + "PREFIX em: <http://purl.org/daafse/electricmeters#>\n"
                + "SELECT ?meter \n"
                + "WHERE {\n"
                + "STREAM <" + uri.toString() + "> [NOW] \n"
                + "{?o ssn:observedBy ?meter} \n"
                + "}");
        Thread.sleep(15000);
        qes.unregister(queryId);
        stream.stopReadStream(uri);
    }
}
