package org.ovirt.engine.core.vdsbroker.vdsbroker;

import org.ovirt.engine.core.common.errors.VDSError;
import org.ovirt.engine.core.common.errors.VdcBllErrors;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.vdsbroker.ResourceManager;

public class CancelMigrateVDSCommand<P extends CancelMigrationVDSParameters> extends VdsBrokerCommand<P> {
    public CancelMigrateVDSCommand(P parameters) {
        super(parameters);
    }

    @Override
    protected void executeVdsBrokerCommand() {
        Guid vmId = getParameters().getVmId();
        status = getBroker().migrateCancel(vmId.toString());
        proceedProxyReturnValue();
        if (!getParameters().isRerunAfterCancel()) {
            ResourceManager.getInstance().RemoveAsyncRunningVm(vmId);
        }
    }

    /**
     * overrode to improve error handling when cancel migration failed because the VM doesn't exist on the target host.<BR>
     * may happen when migration already ended.
     */
    @Override
    protected void proceedProxyReturnValue() {
        VdcBllErrors returnStatus = getReturnValueFromStatus(getReturnStatus());
        switch (returnStatus) {
        case noVM:
            VDSExceptionBase outEx =
                    createDefaultConcreteException("Cancel migration has failed. Please try again in a few moments and track the VM's event list for details");
            initializeVdsError(returnStatus);
            outEx.setVdsError(new VDSError(VdcBllErrors.MIGRATION_CANCEL_ERROR_NO_VM, getReturnStatus().mMessage));
            throw outEx;
        default:
            super.proceedProxyReturnValue();
        }
    }
}
