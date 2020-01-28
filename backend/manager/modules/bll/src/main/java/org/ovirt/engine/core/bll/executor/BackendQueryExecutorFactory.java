package org.ovirt.engine.core.bll.executor;

import javax.enterprise.inject.Produces;
import javax.inject.Singleton;

@Singleton
public class BackendQueryExecutorFactory {
    @Produces
    public BackendQueryExecutor commandExecutor() {
        return new DefaultBackendQueryExecutor();
    }

}
