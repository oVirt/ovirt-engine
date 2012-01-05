package org.ovirt.engine.core.utils.threadpool;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.ovirt.engine.core.common.config.Config;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.common.interfaces.IVdcUser;
import org.ovirt.engine.core.compat.LogCompat;
import org.ovirt.engine.core.compat.LogFactoryCompat;
import org.ovirt.engine.core.utils.ThreadLocalParamsContainer;

public class ThreadPoolUtil {

    private static LogCompat log = LogFactoryCompat.getLog(ThreadPoolUtil.class);

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

        public InternalWrapperRunnable(Runnable job, IVdcUser vdcUser, String httpSessionId) {
            this.job = job;
            this.vdcUser = vdcUser;
            this.httpSessionId = httpSessionId;
        }

        @Override
        public void run() {
            ThreadLocalParamsContainer.setVdcUser(vdcUser);
            ThreadLocalParamsContainer.setHttpSessionId(httpSessionId);
            job.run();
        }

    }

    private static final ExecutorService es = new InternalThreadExecutor();

    public static void execute(Runnable command) {
        try {
            es.submit(new InternalWrapperRunnable(command,
                    ThreadLocalParamsContainer.getVdcUser(),
                    ThreadLocalParamsContainer.getHttpSessionId()));
        } catch (RejectedExecutionException e) {
            log.warn("The thread pool is out of limit. A submitted task was rejected");
            throw e;
        }
    }
}
