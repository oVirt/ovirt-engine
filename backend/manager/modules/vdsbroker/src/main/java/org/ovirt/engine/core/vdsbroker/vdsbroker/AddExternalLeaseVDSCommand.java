package org.ovirt.engine.core.vdsbroker.vdsbroker;

import org.ovirt.engine.core.common.asynctasks.AsyncTaskCreationInfo;
import org.ovirt.engine.core.common.asynctasks.AsyncTaskType;
import org.ovirt.engine.core.common.vdscommands.AddExternalLeaseVDSParameters;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.vdsbroker.irsbroker.IrsBrokerCommand;
import org.ovirt.engine.core.vdsbroker.irsbroker.LeaseTaskInfoReturn;

public class AddExternalLeaseVDSCommand<T extends AddExternalLeaseVDSParameters> extends IrsBrokerCommand<T> {
    private LeaseTaskInfoReturn returnValue;

    public AddExternalLeaseVDSCommand(T parameters) {
        super(parameters);
    }

    @Override
    protected void executeIrsBrokerCommand() {
        returnValue = getIrsProxy().addLease(
                getParameters().getLeaseId().toString(),
                getParameters().getStorageDomainId().toString(),
                getParameters().getMetadata());

        proceedProxyReturnValue();

        Guid taskID = new Guid(returnValue.getTaskId());
        getVDSReturnValue().setCreationInfo(
                new AsyncTaskCreationInfo(taskID, AsyncTaskType.addLease,
                        getParameters().getStoragePoolId()));
    }

    @Override
    protected Status getReturnStatus() {
        return returnValue.getStatus();
    }

}
