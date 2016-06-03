package org.ovirt.engine.core.vdsbroker.vdsbroker;

import javax.enterprise.inject.Produces;
import javax.inject.Inject;
import javax.inject.Singleton;

import org.ovirt.engine.core.utils.executor.CommandController;

@Singleton
public class VdsCommandExecutorFactory {

    @Inject
    CommandController controller;

    @Produces
    public VdsCommandExecutor commandExecutor() {
        if (controller.isMonitorVdsBrokerEnabled()) {
            return new HystrixVdsCommandExecutor();
        } else {
            return new DefaultVdsCommandExecutor();
        }
    }
}
