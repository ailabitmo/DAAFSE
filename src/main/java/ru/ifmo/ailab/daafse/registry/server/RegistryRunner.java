package ru.ifmo.ailab.daafse.registry.server;

import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by oscii on 30/04/14.
 */
public class RegistryRunner {
    private ExecutorService executor = Executors.newSingleThreadExecutor();
    private Registry registry;

    public RegistryRunner() throws IOException {
        registry = new Registry();
    }

    public void launchRegistry() {
        executor.submit(registry);
    }

    public void stopRegistry() {
        executor.shutdownNow();
    }
}
