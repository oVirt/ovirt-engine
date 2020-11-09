package org.ovirt.engine.core.vdsbroker.irsbroker;

import org.ovirt.engine.core.common.asynctasks.AsyncTaskCreationInfo;
import org.ovirt.engine.core.common.asynctasks.AsyncTaskType;
import org.ovirt.engine.core.common.vdscommands.DeactivateStorageDomainVDSCommandParameters;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.vdsbroker.vdsbroker.Status;

public class SwitchMasterStorageDomainVDSCommand<P extends DeactivateStorageDomainVDSCommandParameters>
        extends IrsBrokerCommand<P> {

    private OneUuidReturn uuidReturn;

    public SwitchMasterStorageDomainVDSCommand(P parameters) {
        super(parameters);
    }

    @Override
    protected void executeIrsBrokerCommand() {
        uuidReturn = getIrsProxy().switchMaster(getParameters().getStoragePoolId().toString(),
                getParameters().getMasterStorageDomainId().toString(),
                getParameters().getStorageDomainId().toString(),
                getParameters().getMasterVersion());
        proceedProxyReturnValue();

        Guid taskID = new Guid(uuidReturn.uuid);

        getVDSReturnValue().setCreationInfo(new AsyncTaskCreationInfo(taskID, AsyncTaskType.switchMaster,
                getParameters().getStoragePoolId()));
    }

    @Override
    protected Status getReturnStatus() {
        return uuidReturn.getStatus();
    }
}
