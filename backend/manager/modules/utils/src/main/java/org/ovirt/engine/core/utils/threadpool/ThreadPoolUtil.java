package org.ovirt.engine.core.utils.threadpool;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.ovirt.engine.core.common.config.Config;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.common.interfaces.IVdcUser;
import org.ovirt.engine.core.utils.ThreadLocalParamsContainer;
import org.ovirt.engine.core.utils.log.Log;
import org.ovirt.engine.core.utils.log.LogFactory;

public class ThreadPoolUtil {

    private static Log log = LogFactory.getLog(ThreadPoolUtil.class);

    private static class InternalThreadExecutor extends ThreadPoolExecutor {

        /**
         * The pool which will be created with corePoolSize equal to ConfigValues.DefaultMinThreadPoolSize
         * maximumPoolSize equal to DefaultMaxThreadPoolSize
         */
        public InternalThreadExecutor() {
            super(Config.<Integer> GetValue(ConfigValues.DefaultMinThreadPoolSize),
                    Config.<Integer> GetValue(ConfigValues.DefaultMaxThreadPoolSize),
                    60L,
                    TimeUnit.SECONDS,
                    new SynchronousQueue<Runnable>());
        }

        @Override
        protected void afterExecute(Runnable r, Throwable t) {
            super.afterExecute(r, t);
            ThreadLocalParamsContainer.clean();
        }
    }

    private static class InternalWrapperRunnable implements Runnable {

        private Runnable job;
        private IVdcUser vdcUser;
        private String httpSessionId;

        /**
         * Identifies the correlation-id associated with the thread invoker
         */
        private String correlationId;

        public InternalWrapperRunnable(Runnable job, IVdcUser vdcUser, String httpSessionId, String correlationId) {
            this.job = job;
            this.vdcUser = vdcUser;
            this.httpSessionId = httpSessionId;
            this.correlationId = correlationId;
        }

        @Override
        public void run() {
            ThreadLocalParamsContainer.setVdcUser(vdcUser);
            ThreadLocalParamsContainer.setHttpSessionId(httpSessionId);
            ThreadLocalParamsContainer.setCorrelationId(correlationId);
            job.run();
        }

    }

    private static class InternalCallable<V> implements Callable<V> {

        private Callable<V> job;
        private IVdcUser vdcUser;
        private String httpSessionId;

        /**
         * Identifies the correlation-id associated with the thread invoker
         */
        private String correlationId;

        public InternalCallable(Callable<V> job) {
            this.job = job;
            this.vdcUser = ThreadLocalParamsContainer.getVdcUser();
            this.httpSessionId = ThreadLocalParamsContainer.getHttpSessionId();
            this.correlationId = ThreadLocalParamsContainer.getCorrelationId();
        }

        @Override
        public V call() throws Exception {
            ThreadLocalParamsContainer.setVdcUser(vdcUser);
            ThreadLocalParamsContainer.setHttpSessionId(httpSessionId);
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
                    ThreadLocalParamsContainer.getVdcUser(),
                    ThreadLocalParamsContainer.getHttpSessionId(),
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
