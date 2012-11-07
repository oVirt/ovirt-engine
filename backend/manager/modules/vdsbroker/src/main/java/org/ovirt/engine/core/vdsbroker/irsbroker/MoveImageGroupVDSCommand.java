package org.ovirt.engine.core.vdsbroker.irsbroker;

import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.common.asynctasks.AsyncTaskCreationInfo;
import org.ovirt.engine.core.common.asynctasks.AsyncTaskType;
import org.ovirt.engine.core.common.vdscommands.MoveImageGroupVDSCommandParameters;

public class MoveImageGroupVDSCommand<P extends MoveImageGroupVDSCommandParameters> extends IrsCreateCommand<P> {
    public MoveImageGroupVDSCommand(P parameters) {
        super(parameters);
    }

    @Override
    protected void ExecuteIrsBrokerCommand() {
        uuidReturn = getIrsProxy().moveImage(getParameters().getStoragePoolId().toString(),
                                             getParameters().getStorageDomainId().toString(),
                                             getParameters().getDstDomainId().toString(),
                                             getParameters().getImageGroupId().toString(),
                                             getParameters().getVmId().toString(),
                                             getParameters().getOp().getValue(),
                                             (new Boolean(getParameters().getPostZero())).toString().toLowerCase(),
                                             (new Boolean(getParameters().getForce())).toString().toLowerCase());
        ProceedProxyReturnValue();

        Guid taskID = new Guid(uuidReturn.mUuid);

        getVDSReturnValue().setCreationInfo(
                new AsyncTaskCreationInfo(taskID, AsyncTaskType.moveImage, getParameters().getStoragePoolId()));
    }
}
