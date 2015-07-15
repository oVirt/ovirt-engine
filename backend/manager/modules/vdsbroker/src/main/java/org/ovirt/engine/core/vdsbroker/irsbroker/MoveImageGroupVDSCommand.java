package org.ovirt.engine.core.vdsbroker.irsbroker;

import org.ovirt.engine.core.common.asynctasks.AsyncTaskCreationInfo;
import org.ovirt.engine.core.common.asynctasks.AsyncTaskType;
import org.ovirt.engine.core.common.vdscommands.MoveImageGroupVDSCommandParameters;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.vdsbroker.storage.StorageDomainHelper;

public class MoveImageGroupVDSCommand<P extends MoveImageGroupVDSCommandParameters> extends IrsCreateCommand<P> {
    public MoveImageGroupVDSCommand(P parameters) {
        super(parameters);
    }

    @Override
    protected void executeIrsBrokerCommand() {
        StorageDomainHelper.checkNumberOfLVsForBlockDomain(getParameters().getDstDomainId());
        uuidReturn = getIrsProxy().moveImage(getParameters().getStoragePoolId().toString(),
                                             getParameters().getStorageDomainId().toString(),
                                             getParameters().getDstDomainId().toString(),
                                             getParameters().getImageGroupId().toString(),
                                             getParameters().getVmId().toString(),
                                             getParameters().getOp().getValue(),
                                             String.valueOf(getParameters().getPostZero()).toLowerCase(),
                                             String.valueOf(getParameters().getForce()).toLowerCase());
        proceedProxyReturnValue();

        Guid taskID = new Guid(uuidReturn.uuid);

        getVDSReturnValue().setCreationInfo(
                new AsyncTaskCreationInfo(taskID, AsyncTaskType.moveImage, getParameters().getStoragePoolId()));
    }
}
