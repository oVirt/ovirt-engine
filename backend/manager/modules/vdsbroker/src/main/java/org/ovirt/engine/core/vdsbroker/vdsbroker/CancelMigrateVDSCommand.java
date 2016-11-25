package org.ovirt.engine.core.vdsbroker.vdsbroker;

import javax.inject.Inject;

import org.ovirt.engine.core.common.errors.EngineError;
import org.ovirt.engine.core.common.errors.VDSError;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.vdsbroker.ResourceManager;

public class CancelMigrateVDSCommand<P extends CancelMigrationVDSParameters> extends VdsBrokerCommand<P> {

    @Inject
    private ResourceManager resourceManager;

    public CancelMigrateVDSCommand(P parameters) {
        super(parameters);
    }

    @Override
    protected void executeVdsBrokerCommand() {
        Guid vmId = getParameters().getVmId();
        status = getBroker().migrateCancel(vmId.toString());
        proceedProxyReturnValue();
        if (!getParameters().isRerunAfterCancel()) {
            resourceManager.removeAsyncRunningVm(vmId);
        }
    }

    /**
     * overrode to improve error handling when cancel migration failed because the VM doesn't exist on the target host.<BR>
     * may happen when migration already ended.
     */
    @Override
    protected void proceedProxyReturnValue() {
        EngineError returnStatus = getReturnValueFromStatus(getReturnStatus());
        switch (returnStatus) {
        case noVM:
            VDSExceptionBase outEx =
                    createDefaultConcreteException("Cancel migration has failed. Please try again in a few moments and track the VM's event list for details");
            initializeVdsError(returnStatus);
            outEx.setVdsError(new VDSError(EngineError.MIGRATION_CANCEL_ERROR_NO_VM, getReturnStatus().message));
            throw outEx;
        default:
            super.proceedProxyReturnValue();
        }
    }
}
