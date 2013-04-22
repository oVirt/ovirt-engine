package org.ovirt.engine.core.vdsbroker.irsbroker;

import org.ovirt.engine.core.common.asynctasks.AsyncTaskCreationInfo;
import org.ovirt.engine.core.common.asynctasks.AsyncTaskType;
import org.ovirt.engine.core.common.vdscommands.CreateImageVDSCommandParameters;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.utils.log.Log;
import org.ovirt.engine.core.utils.log.LogFactory;

public class CreateImageVDSCommand<P extends CreateImageVDSCommandParameters> extends IrsCreateCommand<P> {
    public CreateImageVDSCommand(P parameters) {
        super(parameters);
    }

    @Override
    protected void ExecuteIrsBrokerCommand() {
        setReturnValue(Guid.Empty);

        log.info("-- CreateImageVDSCommand::ExecuteIrsBrokerCommand: calling 'createVolume' with two new parameters: description and UUID");
        log.infoFormat("-- createVolume parameters:" + "\r\n" + "                sdUUID={0}"
                + "\r\n" + "                spUUID={1}" + "\r\n" + "                imgGUID={2}"
                + "\r\n" + "                size={3} bytes" + "\r\n"
                + "                volFormat={4}" + "\r\n" + "                volType={5}" + "\r\n"
                + "                volUUID={6}" + "\r\n"
                + "                descr={7}" + "\r\n" + "                srcImgGUID={8}" + "\r\n"
                + "                srcVolUUID={9}" + "\r\n" + "                ",
                getParameters().getStorageDomainId().toString(), getParameters()
                        .getStoragePoolId().toString(), getParameters().getImageGroupId()
                        .toString(), getParameters().getImageSizeInBytes(),
                getParameters().getVolumeFormat().name(), getParameters()
                        .getImageType().name(), getParameters().getNewImageID().toString(), getParameters()
                        .getNewImageDescription(), Guid.Empty.toString(), Guid.Empty.toString());

        // NOTE: The 'uuidReturn' variable will contain the taskID and not the
        // created image id!
        uuidReturn = getIrsProxy().createVolume(
                                                getParameters().getStorageDomainId().toString(),
                                                getParameters().getStoragePoolId().toString(),
                                                getParameters().getImageGroupId().toString(),
                                                (Long.valueOf(getParameters().getImageSizeInBytes())).toString(),
                                                getParameters().getVolumeFormat().getValue(),
                                                getParameters().getImageType().getValue(),
                                                2,
                                                getParameters().getNewImageID().toString(),
                                                getParameters().getNewImageDescription(), Guid.Empty.toString(),
                                                Guid.Empty.toString());

        ProceedProxyReturnValue();

        Guid taskID = new Guid(uuidReturn.mUuid);

        mCreatedImageId = getParameters().getNewImageID().toString();
        setReturnValue(new Guid(mCreatedImageId));

        getVDSReturnValue().setCreationInfo(
                new AsyncTaskCreationInfo(taskID, AsyncTaskType.createVolume, getParameters()
                        .getStoragePoolId()));
    }

    private static Log log = LogFactory.getLog(CreateImageVDSCommand.class);
}
