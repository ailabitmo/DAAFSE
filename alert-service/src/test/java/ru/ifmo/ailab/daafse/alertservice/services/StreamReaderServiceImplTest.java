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
        qes.loadDataset(null, "../datasets/meters.ttl");
        stream.startReadStream(uri);
        final int queryId = qes.registerSelect(
                "PREFIX ssn: <http://purl.oclc.org/NET/ssnx/ssn#>\n"
                + "PREFIX em: <http://purl.org/daafse/electricmeters#>\n"
                + "SELECT ?meter ?stream\n"
                + "WHERE {"
                + "     STREAM ?stream [NOW]"
                + "     {?o ssn:observedBy ?meter}"
                + "     ?x em:hasStream ?stream ."
                + "}");
        Thread.sleep(20000);
        qes.unregister(queryId);
        stream.stopReadStream(uri);
    }
}
