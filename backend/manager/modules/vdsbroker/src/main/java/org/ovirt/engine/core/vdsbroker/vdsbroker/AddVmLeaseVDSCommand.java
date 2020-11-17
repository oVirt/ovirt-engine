package org.ovirt.engine.core.vdsbroker.vdsbroker;

import org.ovirt.engine.core.common.asynctasks.AsyncTaskCreationInfo;
import org.ovirt.engine.core.common.asynctasks.AsyncTaskType;
import org.ovirt.engine.core.common.vdscommands.VmLeaseVDSParameters;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.vdsbroker.irsbroker.IrsBrokerCommand;
import org.ovirt.engine.core.vdsbroker.irsbroker.LeaseTaskInfoReturn;

public class AddVmLeaseVDSCommand<T extends VmLeaseVDSParameters> extends IrsBrokerCommand<T> {

    private LeaseTaskInfoReturn returnValue;

    public AddVmLeaseVDSCommand(T parameters) {
        super(parameters);
    }

    @Override
    protected void executeIrsBrokerCommand() {
        returnValue = getIrsProxy().addLease(
                getParameters().getLeaseId().toString(),
                getParameters().getStorageDomainId().toString());
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
