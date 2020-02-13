package org.ovirt.engine.core.vdsbroker.monitoring.kubevirt;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import javax.annotation.PostConstruct;
import javax.enterprise.concurrent.ManagedScheduledExecutorService;
import javax.enterprise.concurrent.Trigger;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.ovirt.engine.core.utils.threadpool.ThreadPools;

import io.kubernetes.client.ApiClient;
import io.kubernetes.client.informer.SharedInformerFactory;

@ApplicationScoped
public class SharedInformerFactoryProducer {

    @Inject
    @ThreadPools(ThreadPools.ThreadPoolType.EngineScheduledThreadPool)
    private ManagedScheduledExecutorService executor;

    private ExecutorService wrappedExecutor;

    public SharedInformerFactory newInstance(ApiClient client) {
        return new SharedInformerFactory(client, wrappedExecutor);
    }

    @PostConstruct
    public void init() {
        /**
         * A wrapper of engine's ManagedScheduledExecutorService, specifically for ignoring a call to {@code shutdown}
         * method, so the executor service will remain active after the SharedInformerFactory stops.
         */
        wrappedExecutor = new ExecutorService() {
            public <V> ScheduledFuture<V> schedule(Callable<V> callable,
                    Trigger trigger) {
                return executor.schedule(callable, trigger);
            }

            public ScheduledFuture<?> schedule(Runnable command, Trigger trigger) {
                return executor.schedule(command, trigger);
            }

            public void shutdown() {
                // do nothing
            }

            public List<Runnable> shutdownNow() {
                return executor.shutdownNow();
            }

            public boolean isShutdown() {
                return executor.isShutdown();
            }

            public boolean isTerminated() {
                return executor.isTerminated();
            }

            public boolean awaitTermination(long l, TimeUnit timeUnit) throws InterruptedException {
                return executor.awaitTermination(l, timeUnit);
            }

            public <T> Future<T> submit(Callable<T> callable) {
                return executor.submit(callable);
            }

            public <T> Future<T> submit(Runnable runnable, T t) {
                return executor.submit(runnable, t);
            }

            public Future<?> submit(Runnable runnable) {
                return executor.submit(runnable);
            }

            public <T> List<Future<T>> invokeAll(Collection<? extends Callable<T>> collection) throws
                    InterruptedException {
                return executor.invokeAll(collection);
            }

            public <T> List<Future<T>> invokeAll(Collection<? extends Callable<T>> collection,
                    long l, TimeUnit timeUnit) throws InterruptedException {
                return executor.invokeAll(collection, l, timeUnit);
            }

            public <T> T invokeAny(Collection<? extends Callable<T>> collection)
                    throws InterruptedException, ExecutionException {
                return executor.invokeAny(collection);
            }

            public <T> T invokeAny(Collection<? extends Callable<T>> collection,
                    long l,
                    TimeUnit timeUnit) throws InterruptedException, ExecutionException, TimeoutException {
                return executor.invokeAny(collection, l, timeUnit);
            }

            public void execute(Runnable runnable) {
                executor.execute(runnable);
            }

            public ScheduledFuture<?> schedule(Runnable runnable,
                    long l,
                    TimeUnit timeUnit) {
                return executor.schedule(runnable, l, timeUnit);
            }

            public <V> ScheduledFuture<V> schedule(Callable<V> callable,
                    long l,
                    TimeUnit timeUnit) {
                return executor.schedule(callable, l, timeUnit);
            }

            public ScheduledFuture<?> scheduleAtFixedRate(Runnable runnable,
                    long l,
                    long l1,
                    TimeUnit timeUnit) {
                return executor.scheduleAtFixedRate(runnable, l, l1, timeUnit);
            }

            public ScheduledFuture<?> scheduleWithFixedDelay(Runnable runnable,
                    long l,
                    long l1,
                    TimeUnit timeUnit) {
                return executor.scheduleWithFixedDelay(runnable, l, l1, timeUnit);
            }
        };
    }
}
