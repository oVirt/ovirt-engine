package org.ovirt.engine.core.bll.executor;

import javax.enterprise.inject.Produces;
import javax.inject.Singleton;

@Singleton
public class BackendActionExecutorFactory {

    @Produces
    public BackendActionExecutor commandExecutor() {
        return new DefaultBackendActionExecutor();
    }

}
