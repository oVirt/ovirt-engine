package org.ovirt.engine.core.vdsbroker.irsbroker;

import org.ovirt.engine.core.common.asynctasks.AsyncTaskCreationInfo;
import org.ovirt.engine.core.common.asynctasks.AsyncTaskType;
import org.ovirt.engine.core.common.vdscommands.CreateSnapshotVDSCommandParameters;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.LogCompat;
import org.ovirt.engine.core.compat.LogFactoryCompat;

public class CreateSnapshotVDSCommand<P extends CreateSnapshotVDSCommandParameters> extends IrsCreateCommand<P> {
    public CreateSnapshotVDSCommand(P parameters) {
        super(parameters);
    }

    @Override
    protected void ExecuteIrsBrokerCommand() {
        setReturnValue(Guid.Empty);

        log.info("-- CreateSnapshotVDSCommand::ExecuteIrsBrokerCommand: calling 'createVolume' with two new parameters: description and UUID");
        log.infoFormat("-- createVolume parameters:" + "\r\n" + "                sdUUID={0}"
                + "\r\n" + "                spUUID={1}" + "\r\n" + "                imgGUID={2}"
                + "\r\n" + "                size={3} bytes" + "\r\n"
                + "                volFormat={4}" + "\r\n" + "                volType={5}" + "\r\n"
                + "                diskType={6}" + "\r\n" + "                volUUID={7}" + "\r\n"
                + "                descr={8}" + "\r\n" + "                srcImgGUID={9}" + "\r\n"
                + "                srcVolUUID={10}" + "\r\n" + "                ",
                getParameters().getStorageDomainId().toString(), getParameters()
                        .getStoragePoolId().toString(), getParameters().getImageGroupId()
                        .toString(), getParameters().getImageSizeInBytes(),
                getParameters().getVolumeFormat().name(), getParameters()
                        .getImageType().name(), getParameters().getDiskType().name(),
                getParameters().getNewImageID().toString(), getParameters()
                        .getNewImageDescription(), getParameters().getSourceImageGroupId()
                        .toString(), getParameters().getImageId().toString());

        // NOTE: The 'uuidReturn' variable will contain the taskID and not
        // the created image id!
        uuidReturn = getIrsProxy().createVolume(getParameters().getStorageDomainId().toString(),
                                                getParameters().getStoragePoolId().toString(),
                                                getParameters().getImageGroupId().toString(),
                                                (new Long(getParameters().getImageSizeInBytes())).toString(),
                                                getParameters().getVolumeFormat().getValue(),
                                                getParameters().getImageType().getValue(),
                                                getParameters().getDiskType().getValue(),
                                                getParameters().getNewImageID().toString(),
                                                getParameters().getNewImageDescription(),
                                                getParameters().getSourceImageGroupId().toString(),
                                                getParameters().getImageId().toString());

        ProceedProxyReturnValue();
        Guid taskID = new Guid(uuidReturn.mUuid);

        mCreatedImageId = getParameters().getNewImageID().toString();
        setReturnValue(new Guid(mCreatedImageId));

        getVDSReturnValue().setCreationInfo(
                new AsyncTaskCreationInfo(taskID, AsyncTaskType.createVolume,
                        getParameters().getStoragePoolId()));
    }

    private static LogCompat log = LogFactoryCompat.getLog(CreateSnapshotVDSCommand.class);
}
