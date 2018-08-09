package org.ovirt.engine.core.bll;

import javax.inject.Inject;

import org.ovirt.engine.core.bll.context.EngineContext;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.utils.lock.EngineLock;
import org.ovirt.engine.core.utils.lock.LockManager;

public class IsHostLockedOnNetworkOperationQuery<P extends IdQueryParameters> extends QueriesCommandBase<P> {
    @Inject
    private LockManager lockManager;
    @Inject
    private HostLocking hostLocking;

    public IsHostLockedOnNetworkOperationQuery(P parameters, EngineContext engineContext) {
        super(parameters, engineContext);
    }

    @Override
    protected void executeQueryCommand() {
        getQueryReturnValue().setReturnValue(
            lockManager.isExclusiveLockPresent(new EngineLock(hostLocking.getSetupNetworksLock(getParameters().getId())))
        );
    }
}
