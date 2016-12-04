package org.ovirt.engine.core.vdsbroker.irsbroker;

import javax.inject.Inject;

import org.ovirt.engine.core.common.FeatureSupported;
import org.ovirt.engine.core.common.asynctasks.AsyncTaskCreationInfo;
import org.ovirt.engine.core.common.asynctasks.AsyncTaskType;
import org.ovirt.engine.core.common.vdscommands.MoveImageGroupVDSCommandParameters;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.StoragePoolDao;
import org.ovirt.engine.core.vdsbroker.storage.StorageDomainHelper;

public class MoveImageGroupVDSCommand<P extends MoveImageGroupVDSCommandParameters> extends IrsCreateCommand<P> {

    @Inject
    private StoragePoolDao storagePoolDao;

    public MoveImageGroupVDSCommand(P parameters) {
        super(parameters);
    }

    @Override
    protected void executeIrsBrokerCommand() {
        StorageDomainHelper.checkNumberOfLVsForBlockDomain(getParameters().getDstDomainId());
        uuidReturn = moveImage(getParameters().getStoragePoolId().toString(),
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

    private OneUuidReturn moveImage(String storagePoolId, String storageDomainId, String dstDomainId,
            String imageGroupId, String vmId, int op, String postZero, boolean discard, String force) {
        if (FeatureSupported.discardAfterDeleteSupported(
                storagePoolDao.get(getParameters().getStoragePoolId()).getCompatibilityVersion())) {
            return getIrsProxy().moveImage(storagePoolId, storageDomainId, dstDomainId, imageGroupId, vmId, op,
                    postZero, discard, force);
        }
        return getIrsProxy().moveImage(storagePoolId, storageDomainId, dstDomainId, imageGroupId, vmId, op,
                    postZero, force);
    }
}
