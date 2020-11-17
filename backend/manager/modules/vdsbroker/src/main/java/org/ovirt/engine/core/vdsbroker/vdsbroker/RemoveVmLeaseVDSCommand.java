package org.ovirt.engine.core.vdsbroker.vdsbroker;

import org.ovirt.engine.core.common.asynctasks.AsyncTaskCreationInfo;
import org.ovirt.engine.core.common.asynctasks.AsyncTaskType;
import org.ovirt.engine.core.common.vdscommands.VmLeaseVDSParameters;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.vdsbroker.irsbroker.IrsBrokerCommand;
import org.ovirt.engine.core.vdsbroker.irsbroker.LeaseTaskInfoReturn;

public class RemoveVmLeaseVDSCommand<T extends VmLeaseVDSParameters> extends IrsBrokerCommand<T> {

    private LeaseTaskInfoReturn returnValue;

    public RemoveVmLeaseVDSCommand(T parameters) {
        super(parameters);
    }

    @Override
    protected void executeIrsBrokerCommand() {
        returnValue = getIrsProxy().removeLease(
                getParameters().getLeaseId().toString(),
                getParameters().getStorageDomainId().toString());
        proceedProxyReturnValue();

        Guid taskID = new Guid(returnValue.getTaskId());
        getVDSReturnValue().setCreationInfo(
                new AsyncTaskCreationInfo(taskID, AsyncTaskType.removeLease,
                        getParameters().getStoragePoolId()));
    }

    @Override
    protected Status getReturnStatus() {
        return returnValue.getStatus();
    }
}
