package org.ovirt.engine.core.vdsbroker.irsbroker;

import org.ovirt.engine.core.common.asynctasks.AsyncTaskCreationInfo;
import org.ovirt.engine.core.common.asynctasks.AsyncTaskType;
import org.ovirt.engine.core.common.vdscommands.CreateImageVDSCommandParameters;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.utils.log.Log;
import org.ovirt.engine.core.utils.log.LogFactory;
import org.ovirt.engine.core.vdsbroker.storage.StorageDomainHelper;

public class CreateImageVDSCommand<P extends CreateImageVDSCommandParameters> extends IrsCreateCommand<P> {
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

        proceedProxyReturnValue();

        Guid taskID = new Guid(uuidReturn.mUuid);

        mCreatedImageId = getParameters().getNewImageID().toString();
        setReturnValue(new Guid(mCreatedImageId));

        getVDSReturnValue().setCreationInfo(
                new AsyncTaskCreationInfo(taskID, AsyncTaskType.createVolume, getParameters()
                        .getStoragePoolId()));
    }

    private static final Log log = LogFactory.getLog(CreateImageVDSCommand.class);
}
