package org.ovirt.engine.core.bll.executor;

import javax.enterprise.inject.Produces;
import javax.inject.Inject;
import javax.inject.Singleton;

import org.ovirt.engine.core.utils.executor.CommandController;

@Singleton
public class BackendActionExecutorFactory {

    @Inject
    CommandController controller;

    @Produces
    public BackendActionExecutor commandExecutor() {
        if (controller.isMonitorActionsEnabled()) {
            return new HystrixBackendActionExecutor();
        } else {
            return new DefaultBackendActionExecutor();
        }
    }

}
