package org.ovirt.engine.core.common.utils;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.enterprise.concurrent.ManagedExecutorService;
import javax.enterprise.concurrent.ManagedScheduledExecutorService;
import javax.enterprise.inject.Produces;
import javax.inject.Singleton;


import org.ovirt.engine.core.common.BackendService;
import org.ovirt.engine.core.utils.threadpool.ThreadPoolUtil;
import org.ovirt.engine.core.utils.threadpool.ThreadPools;

@Singleton
public class EngineThreadPools implements BackendService {
    public static final String COMMAND_COORDINATOR_POOL_NAME = "java:jboss/ee/concurrency/executor/commandCoordinator";
    public static final String HOST_UPDATES_CHECKER_POOL_NAME = "java:jboss/ee/concurrency/executor/hostUpdatesChecker";
    public static final String ENGINE_POOL_NAME = "java:jboss/ee/concurrency/executor/engineThreadPool";
    public static final String ENGINE_SCHEDULED_POOL_NAME =
            "java:jboss/ee/concurrency/scheduler/engineScheduledThreadPool";
    public static final String ENGINE_THREAD_MONITORING_POOL_NAME =
            "java:jboss/ee/concurrency/scheduler/engineThreadMonitoringThreadPool";

    @Resource(lookup = EngineThreadPools.COMMAND_COORDINATOR_POOL_NAME)
    private ManagedExecutorService cocoPool;

    @Resource(lookup = EngineThreadPools.HOST_UPDATES_CHECKER_POOL_NAME)
    private ManagedExecutorService hostUpdatesCheckerPool;

    @Resource(lookup = EngineThreadPools.ENGINE_POOL_NAME)
    private ManagedExecutorService engineThreadPool;

    @Resource(lookup = EngineThreadPools.ENGINE_SCHEDULED_POOL_NAME)
    private ManagedScheduledExecutorService engineScheduledThreadPool;

    @Resource(lookup = EngineThreadPools.ENGINE_THREAD_MONITORING_POOL_NAME)
    private ManagedScheduledExecutorService engineThreadMonitoringThreadPool;

    @PostConstruct
    private void init() {
        // initialize ThreadPoolUtil
        ThreadPoolUtil.setExecutorService(engineThreadPool);
    }

    @Produces
    @ThreadPools(ThreadPools.ThreadPoolType.CoCo)
    public ManagedExecutorService cocoPoolProducer() {
        return cocoPool;
    }

    @Produces
    @ThreadPools(ThreadPools.ThreadPoolType.HostUpdatesChecker)
    public ManagedExecutorService hostUpdatesCheckerPoolProducer() {
        return hostUpdatesCheckerPool;
    }

    @Produces
    @ThreadPools(ThreadPools.ThreadPoolType.EngineThreadPool)
    public ManagedExecutorService engineThreadPoolProducer() {
        return engineThreadPool;
    }

    @Produces
    @ThreadPools(ThreadPools.ThreadPoolType.EngineScheduledThreadPool)
    public ManagedScheduledExecutorService engineScheduledThreadPoolProducer() {
        return engineScheduledThreadPool;
    }

    @Produces
    @ThreadPools(ThreadPools.ThreadPoolType.EngineThreadMonitoringThreadPool)
    public ManagedScheduledExecutorService engineThreadMonitoringThreadPoolProducer() {
        return engineThreadMonitoringThreadPool;
    }
}
