package org.ovirt.engine.core.utils.threadpool;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;
import java.util.concurrent.RejectedExecutionException;

import javax.enterprise.concurrent.ManagedExecutorService;
import javax.enterprise.concurrent.ManagedTask;
import javax.enterprise.concurrent.ManagedTaskListener;

import org.ovirt.engine.core.utils.CorrelationIdTracker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ThreadPoolUtil {

    private static final Logger log = LoggerFactory.getLogger(ThreadPoolUtil.class);

    private static ExecutorService executor;

    private static class EngineManagedTask implements ManagedTask, ManagedTaskListener {

        @Override
        public void taskAborted(Future<?> future,
                ManagedExecutorService managedExecutorService,
                Object o,
                Throwable throwable) {
            log.error("Execution of task aborted: {}", Thread.currentThread().getName());
            log.debug("Exception", throwable);
            CorrelationIdTracker.clean();
        }

        @Override
        public void taskDone(Future<?> future,
                ManagedExecutorService managedExecutorService,
                Object o,
                Throwable throwable) {
            log.debug("Execution of task completed: {}", Thread.currentThread().getName());
            CorrelationIdTracker.clean();
        }

        @Override
        public void taskStarting(Future<?> future, ManagedExecutorService managedExecutorService, Object o) {
            String threadName = Thread.currentThread().getName();
            if (!threadName.startsWith("org.ovirt.thread.")) {
                Thread.currentThread().setName("org.ovirt.thread." + threadName);
            }
            log.debug("Execution of task starting: {}", Thread.currentThread().getName());
        }

        @Override
        public void taskSubmitted(Future<?> future, ManagedExecutorService managedExecutorService, Object o) {
            log.debug("Task submitted: {}", Thread.currentThread().getName());
        }

        @Override
        public Map<String, String> getExecutionProperties() {
            return new HashMap<>();
        }

        @Override
        public ManagedTaskListener getManagedTaskListener() {
            return this;
        }

    }

    private static class InternalWrapperRunnable extends EngineManagedTask implements Runnable {

        private Runnable job;

        /**
         * Identifies the correlation-id associated with the thread invoker
         */
        private String correlationId;

        public InternalWrapperRunnable(Runnable job, String correlationId) {
            this.job = job;
            this.correlationId = correlationId;
        }

        @Override
        public void run() {
            String threadName = Thread.currentThread().getName();
            log.debug("Executing task: {}", threadName);
            CorrelationIdTracker.setCorrelationId(correlationId);
            job.run();
        }
    }

    private static class InternalCallable<V> extends EngineManagedTask implements Callable<V> {

        private Callable<V> job;

        /**
         * Identifies the correlation-id associated with the thread invoker
         */
        private String correlationId;

        public InternalCallable(Callable<V> job) {
            this.job = job;
            this.correlationId = CorrelationIdTracker.getCorrelationId();
        }

        @Override
        public V call() throws Exception {
            CorrelationIdTracker.setCorrelationId(correlationId);
            return job.call();
        }
    }

    /**
     * Creates a completion service to allow launching of tasks (callable objects)
     * concurrently, and use a blocking queue like interface to get the tasks
     * execution results
     */
    public static <V> ExecutorCompletionService<V> createCompletionService() {
        return new ExecutorCompletionService<>(getExecutorService());
    }

    private static <T> List<Callable<T>> buildSessionTasks(Collection<? extends Callable<T>> tasks) {
        List<Callable<T>> sessionedTask = new ArrayList<>();
        for (Callable<T> task : tasks) {
            sessionedTask.add(new InternalCallable<>(task));
        }
        return sessionedTask;
    }

    /**
     * Creates a completion service to allow launching of tasks (callable objects)
     * concurrently, and use a blocking queue like interface to get the tasks
     * @param tasks collection of tasks (callable objects) to submit
     */
    public static <V> ExecutorCompletionService<V> createCompletionService(Iterable<Callable<V>> tasks) {
        ExecutorCompletionService<V> ecs =  createCompletionService();
        submitTasks(ecs, tasks);
        return ecs;
    }

    public static <V> List<Future<V>> submitTasks(ExecutorCompletionService<V> ecs, Iterable<Callable<V>> tasks) {
        List<Future<V>> futures = new LinkedList<>();
        if (tasks != null) {
            for (Callable<V> callable : tasks) {
                futures.add(ecs.submit(callable));
            }
        }
        return futures;
    }

    public static void execute(Runnable command) {
        try {
            getExecutorService().submit(new InternalWrapperRunnable(command,
                    CorrelationIdTracker.getCorrelationId()));
        } catch (RejectedExecutionException e) {
            log.warn("The thread pool is out of limit. A submitted task was rejected");
            throw e;
        }
    }

    @SuppressWarnings("unchecked")
    public static <V> Future<V> execute(FutureTask<V> command) {
        try {
            return (Future<V>) getExecutorService().submit(command);
        } catch (RejectedExecutionException e) {
            log.warn("The thread pool is out of limit. The submitted event was rejected");
            throw e;
        }
    }

    /**
     * Executes the given tasks, returning a list of results
     * when all complete, in case of empty or null list a null will be return
     */
    public static <T> List<T> invokeAll(Collection<? extends Callable<T>> tasks) {
        if (tasks != null && !tasks.isEmpty()) {
            try {
                List<Callable<T>> sessionedTask = buildSessionTasks(tasks);
                List<Future<T>> resultFutureList = getExecutorService().invokeAll(sessionedTask);
                List<T> resultList = new ArrayList<>();
                for (Future<T> future : resultFutureList) {
                    resultList.add(future.get());
                }
                return resultList;
            } catch (Exception e) {
                log.warn("The thread pool failed to execute list of tasks: {}", e.getMessage());
                log.debug("Exception", e);
                throw new RuntimeException(e);
            }
        }
        return null;
    }

    public static void setExecutorService(ExecutorService managedExecutorService) {
        executor = managedExecutorService;
    }

    public static ExecutorService getExecutorService() {
        return executor;
    }
}
