package org.ovirt.engine.core.vdsbroker.irsbroker;

import javax.inject.Inject;

import org.ovirt.engine.core.common.asynctasks.AsyncTaskCreationInfo;
import org.ovirt.engine.core.common.asynctasks.AsyncTaskType;
import org.ovirt.engine.core.common.vdscommands.CreateVolumeVDSCommandParameters;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.vdsbroker.storage.StorageDomainHelper;

public class CreateVolumeVDSCommand<P extends CreateVolumeVDSCommandParameters> extends IrsCreateCommand<P> {
    @Inject
    private StorageDomainHelper storageDomainHelper;

    public CreateVolumeVDSCommand(P parameters) {
        super(parameters);
    }

    @Override
    protected void executeIrsBrokerCommand() {
        storageDomainHelper.checkNumberOfLVsForBlockDomain(getParameters().getStorageDomainId());
        setReturnValue(Guid.Empty);

        // NOTE: The 'uuidReturn' variable will contain the taskID and not
        // the created image id!
        String imageInitSize = null;
        if (getParameters().getImageInitialSizeInBytes() != 0) {
            imageInitSize = String.valueOf(getParameters().getImageInitialSizeInBytes());
        }

        uuidReturn = getIrsProxy().createVolume(getParameters().getStorageDomainId().toString(),
                getParameters().getStoragePoolId().toString(),
                getParameters().getImageGroupId().toString(),
                Long.valueOf(getParameters().getImageSizeInBytes()).toString(),
                getParameters().getVolumeFormat().getValue(),
                getParameters().getImageType().getValue(),
                getParameters().getDiskContentType().getStorageValue(),
                getParameters().getNewImageID().toString(),
                getParameters().getNewImageDescription(),
                getParameters().getSourceImageGroupId().toString(),
                getParameters().getImageId().toString(),
                imageInitSize,
                getParameters().shouldAddBitmaps()
                );

        proceedProxyReturnValue();
        Guid taskID = new Guid(uuidReturn.uuid);

        createdImageId = getParameters().getNewImageID().toString();
        setReturnValue(new Guid(createdImageId));

        getVDSReturnValue().setCreationInfo(
                new AsyncTaskCreationInfo(taskID, AsyncTaskType.createVolume,
                        getParameters().getStoragePoolId()));
    }
}
