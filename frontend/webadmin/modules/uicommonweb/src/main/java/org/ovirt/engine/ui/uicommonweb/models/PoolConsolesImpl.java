package org.ovirt.engine.ui.uicommonweb.models;

import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.ui.uicommonweb.ConsoleOptionsFrontendPersister;

public class PoolConsolesImpl extends ConsolesBase {

    public PoolConsolesImpl(VM vm, Model parentModel, ConsoleOptionsFrontendPersister.ConsoleContext consoleContext) {
        super(vm, parentModel, consoleContext);
    }

    @Override
    public void connect() throws ConsoleConnectException {
        throw new ConsoleConnectException(constants.connectToPoolNotSupported());
    }

    /**
     * @return id of underlying pool
     */
    @Override
    public Guid getEntityId() {
        return getVm().getVmPoolId();
    }

    /**
     * @return name of underlying pool
     */
    @Override
    public String getEntityName() {
        return getVm().getVmPoolName();
    }

    @Override
    public String cannotConnectReason() {
        return constants.connectToPoolNotSupported();
    }
}
