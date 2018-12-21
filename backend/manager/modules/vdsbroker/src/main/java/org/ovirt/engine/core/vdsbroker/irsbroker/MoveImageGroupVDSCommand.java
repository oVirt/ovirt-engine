package org.ovirt.engine.core.vdsbroker.irsbroker;

import javax.inject.Inject;

import org.ovirt.engine.core.common.asynctasks.AsyncTaskCreationInfo;
import org.ovirt.engine.core.common.asynctasks.AsyncTaskType;
import org.ovirt.engine.core.common.vdscommands.MoveImageGroupVDSCommandParameters;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.StoragePoolDao;
import org.ovirt.engine.core.vdsbroker.storage.StorageDomainHelper;

public class MoveImageGroupVDSCommand<P extends MoveImageGroupVDSCommandParameters> extends IrsCreateCommand<P> {

    @Inject
    private StoragePoolDao storagePoolDao;

    @Inject
    private StorageDomainHelper storageDomainHelper;

    public MoveImageGroupVDSCommand(P parameters) {
        super(parameters);
    }

    @Override
    protected void executeIrsBrokerCommand() {
        storageDomainHelper.checkNumberOfLVsForBlockDomain(getParameters().getDstDomainId());
        uuidReturn = getIrsProxy().moveImage(getParameters().getStoragePoolId().toString(),
                                             getParameters().getStorageDomainId().toString(),
                                             getParameters().getDstDomainId().toString(),
                                             getParameters().getImageGroupId().toString(),
                                             getParameters().getVmId().toString(),
                                             getParameters().getOp().getValue(),
                                             String.valueOf(getParameters().getPostZero()).toLowerCase(),
                                             getParameters().isDiscard(),
                                             String.valueOf(getParameters().getForce()).toLowerCase());
        proceedProxyReturnValue();

        Guid taskID = new Guid(uuidReturn.uuid);

        getVDSReturnValue().setCreationInfo(
                new AsyncTaskCreationInfo(taskID, AsyncTaskType.moveImage, getParameters().getStoragePoolId()));
    }
}
