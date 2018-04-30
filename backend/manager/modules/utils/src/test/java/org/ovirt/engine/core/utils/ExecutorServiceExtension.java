package org.ovirt.engine.core.utils;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.ovirt.engine.core.utils.threadpool.ThreadPoolUtil;

/**
 * This extension sets up the {@link ThreadPoolUtil}'s executor service for unit tests of classes that rely on it.
 *
 * To use it, simple add a {@code @ExtendWith(ExecutorServiceExtension.class)} annotation.
 */
public class ExecutorServiceExtension implements BeforeEachCallback, AfterEachCallback {

    private ExecutorService origExecutorService;

    @Override
    public void beforeEach(ExtensionContext extensionContext) {
        origExecutorService = ThreadPoolUtil.getExecutorService();
        ThreadPoolUtil.setExecutorService(Executors.newFixedThreadPool(1));
    }

    @Override
    public void afterEach(ExtensionContext extensionContext) {
        ThreadPoolUtil.setExecutorService(origExecutorService);
    }
}
