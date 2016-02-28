package org.ovirt.engine.core.vdsbroker.irsbroker;

import org.ovirt.engine.core.common.asynctasks.AsyncTaskCreationInfo;
import org.ovirt.engine.core.common.asynctasks.AsyncTaskType;
import org.ovirt.engine.core.common.vdscommands.CreateImageVDSCommandParameters;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.vdsbroker.storage.StorageDomainHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CreateImageVDSCommand<P extends CreateImageVDSCommandParameters> extends IrsCreateCommand<P> {

    private static final Logger log = LoggerFactory.getLogger(CreateImageVDSCommand.class);

    public CreateImageVDSCommand(P parameters) {
        super(parameters);
    }

    @Override
    protected void executeIrsBrokerCommand() {
        StorageDomainHelper.checkNumberOfLVsForBlockDomain(getParameters().getStorageDomainId());
        setReturnValue(Guid.Empty);

        log.info("-- executeIrsBrokerCommand: calling 'createVolume' with two new parameters: description and UUID");
        // NOTE: The 'uuidReturn' variable will contain the taskID and not the
        // created image id!
        String imageInitSize = null;
        if (getParameters().getImageInitialSizeInBytes() != 0) {
            imageInitSize = Long.valueOf(getParameters().getImageInitialSizeInBytes()).toString();
        }
        uuidReturn = getIrsProxy().createVolume(
                getParameters().getStorageDomainId().toString(),
                getParameters().getStoragePoolId().toString(),
                getParameters().getImageGroupId().toString(),
                Long.valueOf(getParameters().getImageSizeInBytes()).toString(),
                getParameters().getVolumeFormat().getValue(),
                getParameters().getImageType().getValue(),
                2,
                getParameters().getNewImageID().toString(),
                getParameters().getNewImageDescription(), Guid.Empty.toString(),
                Guid.Empty.toString(),
                imageInitSize);

        proceedProxyReturnValue();

        Guid taskID = new Guid(uuidReturn.uuid);

        createdImageId = getParameters().getNewImageID().toString();
        setReturnValue(new Guid(createdImageId));

        getVDSReturnValue().setCreationInfo(
                new AsyncTaskCreationInfo(taskID, AsyncTaskType.createVolume, getParameters()
                        .getStoragePoolId()));
    }
}
