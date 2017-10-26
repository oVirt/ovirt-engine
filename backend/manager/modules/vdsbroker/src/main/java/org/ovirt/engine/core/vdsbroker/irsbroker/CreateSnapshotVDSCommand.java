package org.ovirt.engine.core.vdsbroker.irsbroker;

import javax.inject.Inject;

import org.ovirt.engine.core.common.FeatureSupported;
import org.ovirt.engine.core.common.asynctasks.AsyncTaskCreationInfo;
import org.ovirt.engine.core.common.asynctasks.AsyncTaskType;
import org.ovirt.engine.core.common.businessentities.storage.DiskContentType;
import org.ovirt.engine.core.common.vdscommands.CreateSnapshotVDSCommandParameters;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.vdsbroker.storage.StorageDomainHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CreateSnapshotVDSCommand<P extends CreateSnapshotVDSCommandParameters> extends IrsCreateCommand<P> {

    private static final Logger log = LoggerFactory.getLogger(CreateSnapshotVDSCommand.class);

    @Inject
    private StorageDomainHelper storageDomainHelper;

    public CreateSnapshotVDSCommand(P parameters) {
        super(parameters);
    }

    @Override
    protected void executeIrsBrokerCommand() {
        storageDomainHelper.checkNumberOfLVsForBlockDomain(getParameters().getStorageDomainId());
        setReturnValue(Guid.Empty);

        log.info("-- executeIrsBrokerCommand: calling 'createVolume' with two new parameters: description and UUID");
        // NOTE: The 'uuidReturn' variable will contain the taskID and not
        // the created image id!
        String imageInitSize = null;
        if (getParameters().getImageInitialSizeInBytes() != 0) {
            imageInitSize = String.valueOf(getParameters().getImageInitialSizeInBytes());
        }

        String diskType = FeatureSupported.isContentTypeSupported(getParameters().getPoolCompatibilityVersion()) ?
                getParameters().getDiskContentType().getStorageValue() : DiskContentType.LEGACY_DISK_TYPE;
        uuidReturn = getIrsProxy().createVolume(getParameters().getStorageDomainId().toString(),
                                                getParameters().getStoragePoolId().toString(),
                                                getParameters().getImageGroupId().toString(),
                                                Long.valueOf(getParameters().getImageSizeInBytes()).toString(),
                                                getParameters().getVolumeFormat().getValue(),
                                                getParameters().getImageType().getValue(),
                                                diskType,
                                                getParameters().getNewImageID().toString(),
                                                getParameters().getNewImageDescription(),
                                                getParameters().getSourceImageGroupId().toString(),
                                                getParameters().getImageId().toString(),
                                                imageInitSize);

        proceedProxyReturnValue();
        Guid taskID = new Guid(uuidReturn.uuid);

        createdImageId = getParameters().getNewImageID().toString();
        setReturnValue(new Guid(createdImageId));

        getVDSReturnValue().setCreationInfo(
                new AsyncTaskCreationInfo(taskID, AsyncTaskType.createVolume,
                        getParameters().getStoragePoolId()));
    }
}
