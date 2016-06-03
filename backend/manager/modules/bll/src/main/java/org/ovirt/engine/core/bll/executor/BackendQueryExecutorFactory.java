package org.ovirt.engine.core.bll.executor;

import javax.enterprise.inject.Produces;
import javax.inject.Inject;
import javax.inject.Singleton;

import org.ovirt.engine.core.utils.executor.CommandController;

@Singleton
public class BackendQueryExecutorFactory {

    @Inject
    CommandController controller;

    @Produces
    public BackendQueryExecutor commandExecutor() {
        if (controller.isMonitorQueriesEnabled()) {
            return new HystrixBackendQueryExecutor();
        } else {
            return new DefaultBackendQueryExecutor();
        }
    }

}
