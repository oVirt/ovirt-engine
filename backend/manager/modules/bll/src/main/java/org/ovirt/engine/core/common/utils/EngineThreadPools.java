package org.ovirt.engine.core.common.utils;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.enterprise.concurrent.ManagedExecutorService;
import javax.enterprise.inject.Produces;
import javax.inject.Singleton;


import org.ovirt.engine.core.common.BackendService;
import org.ovirt.engine.core.utils.threadpool.ThreadPoolUtil;

@Singleton
public class EngineThreadPools implements BackendService {
    public static final String COMMAND_COORDINATOR_POOL_NAME = "java:jboss/ee/concurrency/executor/commandCoordinator";
    public static final String HOST_UPDATES_CHECKER_POOL_NAME = "java:jboss/ee/concurrency/executor/hostUpdatesChecker";
    public static final String ENGINE_POOL_NAME = "java:jboss/ee/concurrency/executor/engineThreadPool";

    @Resource(lookup = EngineThreadPools.COMMAND_COORDINATOR_POOL_NAME)
    private ManagedExecutorService cocoPool;

    @Resource(lookup = EngineThreadPools.HOST_UPDATES_CHECKER_POOL_NAME)
    private ManagedExecutorService hostUpdatesCheckerPool;

    @Resource(lookup = EngineThreadPools.ENGINE_POOL_NAME)
    private ManagedExecutorService engineThreadPool;

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
}
