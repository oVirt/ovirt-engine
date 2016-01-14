package org.ovirt.engine.core.vdsbroker.irsbroker;

import org.ovirt.engine.core.common.asynctasks.AsyncTaskCreationInfo;
import org.ovirt.engine.core.common.asynctasks.AsyncTaskType;
import org.ovirt.engine.core.common.vdscommands.CreateSnapshotVDSCommandParameters;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.vdsbroker.storage.StorageDomainHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CreateSnapshotVDSCommand<P extends CreateSnapshotVDSCommandParameters> extends IrsCreateCommand<P> {

    private static final Logger log = LoggerFactory.getLogger(CreateSnapshotVDSCommand.class);

    public CreateSnapshotVDSCommand(P parameters) {
        super(parameters);
    }

    @Override
    protected void executeIrsBrokerCommand() {
        StorageDomainHelper.checkNumberOfLVsForBlockDomain(getParameters().getStorageDomainId());
        setReturnValue(Guid.Empty);

        log.info("-- executeIrsBrokerCommand: calling 'createVolume' with two new parameters: description and UUID");
        // NOTE: The 'uuidReturn' variable will contain the taskID and not
        // the created image id!
        uuidReturn = getIrsProxy().createVolume(getParameters().getStorageDomainId().toString(),
                                                getParameters().getStoragePoolId().toString(),
                                                getParameters().getImageGroupId().toString(),
                                                Long.valueOf(getParameters().getImageSizeInBytes()).toString(),
                                                getParameters().getVolumeFormat().getValue(),
                                                getParameters().getImageType().getValue(),
                                                2,
                                                getParameters().getNewImageID().toString(),
                                                getParameters().getNewImageDescription(),
                                                getParameters().getSourceImageGroupId().toString(),
                                                getParameters().getImageId().toString(), null);

        proceedProxyReturnValue();
        Guid taskID = new Guid(uuidReturn.uuid);

        createdImageId = getParameters().getNewImageID().toString();
        setReturnValue(new Guid(createdImageId));

        getVDSReturnValue().setCreationInfo(
                new AsyncTaskCreationInfo(taskID, AsyncTaskType.createVolume,
                        getParameters().getStoragePoolId()));
    }
}
