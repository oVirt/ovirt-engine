package org.ovirt.engine.core.vdsbroker.vdsbroker;

import javax.enterprise.inject.Produces;
import javax.inject.Singleton;

@Singleton
public class VdsCommandExecutorFactory {

    @Produces
    public VdsCommandExecutor commandExecutor() {
        return new DefaultVdsCommandExecutor();
    }
}
