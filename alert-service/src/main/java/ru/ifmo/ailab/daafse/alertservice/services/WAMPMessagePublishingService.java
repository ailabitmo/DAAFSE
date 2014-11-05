package ru.ifmo.ailab.daafse.alertservice.services;

import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import java.io.StringReader;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import org.deri.cqels.engine.RDFStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.ifmo.ailab.daafse.alertservice.CQELSEngine;
import ru.ifmo.ailab.daafse.alertservice.MessagePublishingService;
import ru.ifmo.ailab.daafse.alertservice.StreamURI;
import rx.Observer;
import rx.Subscription;
import ws.wamp.jawampa.ApplicationError;
import ws.wamp.jawampa.WampClient;
import ws.wamp.jawampa.WampClientBuilder;

public class WAMPMessagePublishingService implements MessagePublishingService {

    private static final Logger logger = LoggerFactory.getLogger(
            WAMPMessagePublishingService.class);
    private static final String DEFAULT_REALM = "realm1";
    private final CQELSEngine cqelsEngine;
    private final Map<URI, WampClient> clients = new HashMap<>();
    private final Map<StreamURI, Subscription> subscriptions = new HashMap<>();
    
    public WAMPMessagePublishingService(CQELSEngine cqelsEngine) {
        this.cqelsEngine = cqelsEngine;
    }

    @Override
    public void destroy() {
        subscriptions.forEach((__, sub) -> {
            sub.unsubscribe();
        });
        clients.forEach((__, client) -> {
            client.close();
        });
        logger.debug("service has been destroyed");
    }

    @Override
    public void publish(final StreamURI uri, final String body) {
        try {
            WampClient client = getOrCreateClient(uri).join();
            client.publish(uri.getTopic(), body);
        } catch (ApplicationError ex) {
            logger.error(ex.getMessage(), ex);
        }
    }

    @Override
    public void register(final StreamURI uri) {
        if(subscriptions.containsKey(uri)) {
            logger.debug("Stream [{}] is already being read!", uri);
            return;
        }
        try {
            WampClient client = getOrCreateClient(uri).join();
            Subscription sub = client.makeSubscription(uri.getTopic(), String.class)
                    .subscribe(new ObservationConsumer(uri));
            subscriptions.put(uri, sub);
        } catch (ApplicationError ex) {
            logger.error(ex.getMessage(), ex);
        }
    }

    @Override
    public void unregister(final StreamURI uri) {
        if(subscriptions.containsKey(uri)) {
            Subscription sub = subscriptions.get(uri);
            sub.unsubscribe();
        }
    }

    private CompletableFuture<WampClient> getOrCreateClient(final StreamURI streamURI)
            throws ApplicationError {
        CompletableFuture<WampClient> result = new CompletableFuture<>();
        if (!clients.containsKey(streamURI.getServerURI())) {
            WampClientBuilder builder = new WampClientBuilder();
            builder.witUri(streamURI.getServerURI().toASCIIString())
                    .withRealm(DEFAULT_REALM)
                    .withInfiniteReconnects()
                    .withReconnectInterval(5, TimeUnit.SECONDS);
            WampClient client = builder.build();
            client.statusChanged().subscribe((WampClient.Status newStatus) -> {
                logger.debug("WAMP router [{}] status: {}", 
                        streamURI.getServerURI(), newStatus);
                if(newStatus == WampClient.Status.Connected) {
                    result.complete(client);
                }
            });
            client.open();
            clients.put(streamURI.getServerURI(), client);
        } else {
            result.complete(clients.get(streamURI.getServerURI()));
        }
        return result;
    }

    private class ObservationConsumer implements Observer<String> {

        private final StreamURI streamURI;
        private final RDFStream stream;

        public ObservationConsumer(final StreamURI streamURI) {
            this.streamURI = streamURI;
            this.stream = new RDFStream(cqelsEngine.getContext(),
                    streamURI.toString()) {
                        @Override
                        public void stop() {
                            logger.debug("RDFStream.stop() has been called!");
                        }
                    };
        }

        @Override
        public void onCompleted() {
        }

        @Override
        public void onError(Throwable e) {
            logger.error(e.getMessage(), e);
        }

        @Override
        public void onNext(String message) {
            Model model = ModelFactory.createDefaultModel().read(
                    new StringReader(message), null, "TTL");
            StmtIterator iter = model.listStatements();
            while (iter.hasNext()) {
                Triple triple = iter.nextStatement().asTriple();
                stream.stream(triple);
            }
        }

    }

}
