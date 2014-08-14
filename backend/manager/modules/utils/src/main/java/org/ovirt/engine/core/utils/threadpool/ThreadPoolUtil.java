package org.ovirt.engine.core.utils.threadpool;

import java.util.ArrayList;
import java.util.Collection;
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
import org.ovirt.engine.core.utils.ThreadLocalParamsContainer;
import org.ovirt.engine.core.utils.log.Log;
import org.ovirt.engine.core.utils.log.LogFactory;

public class ThreadPoolUtil {

    private static final Log log = LogFactory.getLog(ThreadPoolUtil.class);

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
                    new ArrayBlockingQueue<Runnable>(Config.<Integer> getValue(ConfigValues.DefaultMaxThreadWaitQueueSize)));

        }

        @Override
        protected void beforeExecute(Thread t, Runnable r) {
            super.beforeExecute(t, r);
            String threadName = t.getName();
            if (!threadName.startsWith("org.ovirt.thread.")) {
                t.setName("org.ovirt.thread." + threadName);
            }
            if (log.isDebugEnabled()) {
                log.debug("About to run task " + r.getClass().getName() + " from ", new Exception());
            }

            if (getQueue().size() > 5) {
                log.warn("Executing a command: " + r.getClass().getName() + " , but note that there are "
                        + getQueue().size() + " tasks in the queue.");
            }
        }

        @Override
        protected void afterExecute(Runnable r, Throwable t) {
            super.afterExecute(r, t);
            ThreadLocalParamsContainer.clean();
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
            ThreadLocalParamsContainer.setCorrelationId(correlationId);
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
            this.correlationId = ThreadLocalParamsContainer.getCorrelationId();
        }

        @Override
        public V call() throws Exception {
            ThreadLocalParamsContainer.setCorrelationId(correlationId);
            return job.call();
        }
    }

    private static final ExecutorService es = new InternalThreadExecutor();

    /**
     * Creates a completion service to allow launching of tasks (callable objects)
     * concurrently, and use a blocking queue like interface to get the tasks
     * execution results
     * @return
     */
    private static <V> ExecutorCompletionService<V> createCompletionService() {
        return new ExecutorCompletionService<V>(es);
    }

    private static <T> List<Callable<T>> buildSessionTasks(Collection<? extends Callable<T>> tasks) {
        List<Callable<T>> sessionedTask = new ArrayList<Callable<T>>();
        for (Callable<T> task : tasks) {
            sessionedTask.add(new InternalCallable<T>(task));
        }
        return sessionedTask;
    }

    /**
     * Creates a completion service to allow launching of tasks (callable objects)
     * concurrently, and use a blocking queue like interface to get the tasks
     * @param tasks collection of tasks (callable objects) to submit
     * @return
     */
    public static <V> ExecutorCompletionService<V> createCompletionService(Iterable<Callable<V>> tasks) {
         ExecutorCompletionService<V> ecs =  createCompletionService();
         if (tasks != null) {
             for (Callable<V> callable : tasks) {
                 ecs.submit(callable);
             }
         }
         return ecs;
    }

    public static void execute(Runnable command) {
        try {
            es.submit(new InternalWrapperRunnable(command,
                    ThreadLocalParamsContainer.getCorrelationId()));
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
     * @param tasks
     * @return
     */
    public static <T> List<T> invokeAll(Collection<? extends Callable<T>> tasks) {
        if (tasks != null && !tasks.isEmpty()) {
            try {
                List<Callable<T>> sessionedTask = buildSessionTasks(tasks);
                List<Future<T>> resultFutureList = es.invokeAll(sessionedTask);
                List<T> resultList = new ArrayList<T>();
                for (Future<T> future : resultFutureList) {
                    resultList.add(future.get());
                }
                return resultList;
            } catch (Exception e) {
                log.warnFormat("The thread pool failed to execute list of tasks");
                throw new RuntimeException(e);
            }
        }
        return null;
    }
}
