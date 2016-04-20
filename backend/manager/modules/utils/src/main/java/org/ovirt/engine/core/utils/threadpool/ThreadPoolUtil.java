package org.ovirt.engine.core.utils.threadpool;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.ovirt.engine.core.common.config.Config;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.utils.CorrelationIdTracker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ThreadPoolUtil {

    private static final Logger log = LoggerFactory.getLogger(ThreadPoolUtil.class);

    private static class InternalThreadExecutor extends ThreadPoolExecutor {

        RejectedExecutionHandler rejectedExecutionHandler = new ThreadPoolExecutor.CallerRunsPolicy();

        /**
         * The pool which will be created with corePoolSize equal to ConfigValues.DefaultMinThreadPoolSize
         * maximumPoolSize equal to DefaultMaxThreadPoolSize
         */
        public InternalThreadExecutor() {
            super(Config.<Integer> getValue(ConfigValues.DefaultMinThreadPoolSize),
                    Config.<Integer> getValue(ConfigValues.DefaultMaxThreadPoolSize),
                    60L,
                    TimeUnit.SECONDS,
                    new ArrayBlockingQueue<>(Config.<Integer>getValue(ConfigValues.DefaultMaxThreadWaitQueueSize)));

        }

        @Override
        protected void beforeExecute(Thread t, Runnable r) {
            super.beforeExecute(t, r);
            String threadName = t.getName();
            if (!threadName.startsWith("org.ovirt.thread.")) {
                t.setName("org.ovirt.thread." + threadName);
            }

            if (getQueue().size() > 5) {
                log.warn("Executing a command '{}', but note that there are {} tasks in the queue.",
                        r.getClass().getName(),
                        getQueue().size());
            }
        }

        @Override
        protected void afterExecute(Runnable r, Throwable t) {
            if (t != null) {
                log.error("Execution of task failed", t);
            }
            super.afterExecute(r, t);
            CorrelationIdTracker.clean();
        }

        @Override
        public RejectedExecutionHandler getRejectedExecutionHandler() {
            return rejectedExecutionHandler;
        }
    }

    private static class InternalWrapperRunnable implements Runnable {

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
            CorrelationIdTracker.setCorrelationId(correlationId);
            job.run();
        }

    }

    private static class InternalCallable<V> implements Callable<V> {

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

    private static final ExecutorService es = new InternalThreadExecutor();

    /**
     * Creates a completion service to allow launching of tasks (callable objects)
     * concurrently, and use a blocking queue like interface to get the tasks
     * execution results
     */
    public static <V> ExecutorCompletionService<V> createCompletionService() {
        return new ExecutorCompletionService<>(es);
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
            es.submit(new InternalWrapperRunnable(command,
                    CorrelationIdTracker.getCorrelationId()));
        } catch (RejectedExecutionException e) {
            log.warn("The thread pool is out of limit. A submitted task was rejected");
            throw e;
        }
    }

    @SuppressWarnings("unchecked")
    public static <V> Future<V> execute(FutureTask<V> command) {
        try {
            return (Future<V>) es.submit(command);
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
                List<Future<T>> resultFutureList = es.invokeAll(sessionedTask);
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
}
