package org.ovirt.engine.core.utils.timer;

import static org.apache.commons.lang.StringUtils.isEmpty;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.quartz.SchedulerConfigException;
import org.quartz.spi.ThreadPool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SchedulerThreadPool implements ThreadPool {

    private final Logger log = LoggerFactory.getLogger(getClass());
    private ExecutorService executorService = null;
    private String schedulerName;
    private int count = -1;
    private int threadPriority = Thread.NORM_PRIORITY;
    private boolean inheritLoader;

    public SchedulerThreadPool() {
    }

    @Override
    public boolean runInThread(Runnable runnable) {
        try {
            executorService.submit(runnable);
            return true;
        } catch (RejectedExecutionException e) {
            return false;
        }
    }

    @Override
    public int blockForAvailableThreads() {
        return 1;
    }

    @Override
    public void initialize() throws SchedulerConfigException {
        executorService = new ThreadPoolExecutor(10,
                count,
                60L,
                TimeUnit.SECONDS,
                new ArrayBlockingQueue<Runnable>(50),
                new SchedulerThreadFactory(this));
    }

    public void setThreadCount(int count) {
        this.count = count;
    }

    public int getThreadCount() {
        return count;
    }

    public void setThreadPriority(int prio) {
        this.threadPriority = prio;
    }

    public int getThreadPriority() {
        return threadPriority;
    }

    public boolean isThreadsInheritContextClassLoaderOfInitializingThread() {
        return inheritLoader;
    }

    public void setThreadsInheritContextClassLoaderOfInitializingThread(
            boolean inheritLoader) {
        this.inheritLoader = inheritLoader;
    }

    @Override
    public void shutdown(boolean waitForJobsToComplete) {
        if (waitForJobsToComplete) {
            try {
                executorService.awaitTermination(3600, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                log.error("there is a problem with cleanly shutdown the pool due to: {}", e.getMessage());
            }
        } else {
            executorService.shutdownNow();
        }
    }

    @Override
    public int getPoolSize() {
        return count;
    }

    @Override
    public void setInstanceId(String schedInstId) {
    }

    @Override
    public void setInstanceName(String schedName) {
        this.schedulerName = schedName;
    }

    public String getInstanceName() {
        if (isEmpty(this.schedulerName)) {
            return "SchedulerThreadPool";
        }
        return this.schedulerName;
    }

    static class SchedulerThreadFactory implements ThreadFactory {
        private final AtomicInteger threadNumber = new AtomicInteger(1);
        private final ThreadGroup group;
        private SchedulerThreadPool pool;

        SchedulerThreadFactory(SchedulerThreadPool pool) {
            this.pool = pool;
            SecurityManager s = System.getSecurityManager();
            group = (s != null) ? s.getThreadGroup() : Thread.currentThread().getThreadGroup();
        }

        @Override
        public Thread newThread(Runnable r) {
            Thread t = new Thread(group,
                    r,
                    pool.getInstanceName() + threadNumber.getAndIncrement(),
                    0);
            t.setDaemon(false);
            t.setPriority(pool.getThreadPriority());
            return t;
        }

    }

}
