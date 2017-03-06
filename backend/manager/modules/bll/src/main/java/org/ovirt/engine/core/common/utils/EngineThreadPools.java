package org.ovirt.engine.core.common.utils;

import javax.annotation.Resource;
import javax.enterprise.concurrent.ManagedExecutorService;
import javax.enterprise.inject.Produces;
import javax.inject.Singleton;


import org.ovirt.engine.core.common.BackendService;

@Singleton
public class EngineThreadPools implements BackendService {
    public static final String COMMAND_COORDINATOR_POOL_NAME = "java:jboss/ee/concurrency/executor/commandCoordinator";
    public static final String HOST_UPDATES_CHECKER_POOL_NAME = "java:jboss/ee/concurrency/executor/hostUpdatesChecker";

    @Resource(lookup = EngineThreadPools.COMMAND_COORDINATOR_POOL_NAME)
    private ManagedExecutorService cocoPool;

    @Resource(lookup = EngineThreadPools.HOST_UPDATES_CHECKER_POOL_NAME)
    private ManagedExecutorService hostUpdatesCheckerPool;

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
}
