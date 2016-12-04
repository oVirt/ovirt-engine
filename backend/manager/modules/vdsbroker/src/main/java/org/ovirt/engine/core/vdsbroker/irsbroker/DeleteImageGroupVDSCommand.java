package org.ovirt.engine.core.vdsbroker.irsbroker;

import javax.inject.Inject;

import org.ovirt.engine.core.common.FeatureSupported;
import org.ovirt.engine.core.common.asynctasks.AsyncTaskCreationInfo;
import org.ovirt.engine.core.common.asynctasks.AsyncTaskType;
import org.ovirt.engine.core.common.vdscommands.DeleteImageGroupVDSCommandParameters;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.StoragePoolDao;

public class DeleteImageGroupVDSCommand<P extends DeleteImageGroupVDSCommandParameters> extends IrsCreateCommand<P> {

    @Inject
    private StoragePoolDao storagePoolDao;

    public DeleteImageGroupVDSCommand(P parameters) {
        super(parameters);
    }

    @Override
    protected void executeIrsBrokerCommand() {
        uuidReturn = deleteImage(getParameters().getStorageDomainId().toString(),
                                               getParameters().getStoragePoolId().toString(),
                                               getParameters().getImageGroupId().toString(),
                                               String.valueOf(getParameters().getPostZero()),
                                               getParameters().isDiscard(),
                                               String.valueOf(getParameters().getForceDelete()).toLowerCase());

        proceedProxyReturnValue();

        Guid taskID = new Guid(uuidReturn.uuid);

        getVDSReturnValue().setCreationInfo(
                new AsyncTaskCreationInfo(taskID, AsyncTaskType.deleteImage, getParameters().getStoragePoolId()));
    }

    private OneUuidReturn deleteImage(String storageDomainId, String storagePoolId, String imageGroupId,
            String postZero, boolean discard, String forceDelete) {
        if (FeatureSupported.discardAfterDeleteSupported(
                storagePoolDao.get(getParameters().getStoragePoolId()).getCompatibilityVersion())) {
            return getIrsProxy().deleteImage(storageDomainId, storagePoolId, imageGroupId, postZero, discard,
                    forceDelete);
        }
        return getIrsProxy().deleteImage(storageDomainId, storagePoolId, imageGroupId, postZero, forceDelete);
    }
}
