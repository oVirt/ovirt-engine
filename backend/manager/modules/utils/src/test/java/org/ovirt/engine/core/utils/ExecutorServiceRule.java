package org.ovirt.engine.core.utils;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.junit.rules.TestWatcher;
import org.junit.runner.Description;
import org.ovirt.engine.core.utils.threadpool.ThreadPoolUtil;

/**
 * This rule is set up the {@link ThreadPoolUtil}'s executor service for unit tests of classes that rely on it.
 *
 * To use it, simple add a {@link ExecutorServiceRule} member to your test, with the {@link org.junit.Rule} annotation.
 */
public class ExecutorServiceRule extends TestWatcher {
    private ExecutorService origExecutorService;

    @Override
    public void starting(Description description) {
        origExecutorService = ThreadPoolUtil.getExecutorService();
        ThreadPoolUtil.setExecutorService(Executors.newFixedThreadPool(1));
    }

    @Override
    public void finished(Description description) {
        ThreadPoolUtil.setExecutorService(origExecutorService);
    }
}
