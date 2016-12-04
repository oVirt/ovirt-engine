package org.ovirt.engine.core.vdsbroker.irsbroker;

import javax.inject.Inject;

import org.ovirt.engine.core.common.FeatureSupported;
import org.ovirt.engine.core.common.asynctasks.AsyncTaskCreationInfo;
import org.ovirt.engine.core.common.asynctasks.AsyncTaskType;
import org.ovirt.engine.core.common.vdscommands.DestroyImageVDSCommandParameters;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.StoragePoolDao;

public class DestroyImageVDSCommand<P extends DestroyImageVDSCommandParameters> extends IrsCreateCommand<P> {

    @Inject
    private StoragePoolDao storagePoolDao;

    public DestroyImageVDSCommand(P parameters) {
        super(parameters);
    }

    @Override
    protected void executeIrsBrokerCommand() {

        DestroyImageVDSCommandParameters params = getParameters();
        int imageListSize = params.getImageList().size();
        String[] volUUID = new String[imageListSize];
        int i = 0;
        for (Guid tempGuid : params.getImageList()) {
            volUUID[i++] = tempGuid.toString();
        }

        uuidReturn = deleteVolume(params.getStorageDomainId().toString(),
                                                params.getStoragePoolId().toString(),
                                                params.getImageGroupId().toString(),
                                                volUUID,
                                                String.valueOf(params.getPostZero()),
                                                params.isDiscard(),
                                                String.valueOf(params.getForce()));

        proceedProxyReturnValue();

        Guid taskID = new Guid(uuidReturn.uuid);

        getVDSReturnValue()
                .setCreationInfo(
                        new AsyncTaskCreationInfo(taskID, AsyncTaskType.deleteVolume, getParameters()
                                .getStoragePoolId()));
    }

    private OneUuidReturn deleteVolume(String storageDomainId, String storagePoolId, String imageGroupId,
            String[] volUUID, String postZero, boolean discard, String force) {
        if (FeatureSupported.discardAfterDeleteSupported(
                storagePoolDao.get(getParameters().getStoragePoolId()).getCompatibilityVersion())) {
            return getIrsProxy().deleteVolume(storageDomainId, storagePoolId, imageGroupId, volUUID, postZero, discard,
                    force);
        }
        return getIrsProxy().deleteVolume(storageDomainId, storagePoolId, imageGroupId, volUUID, postZero, force);
    }
}
