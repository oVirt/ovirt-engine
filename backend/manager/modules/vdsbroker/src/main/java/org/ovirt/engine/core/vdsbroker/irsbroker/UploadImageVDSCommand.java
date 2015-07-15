package org.ovirt.engine.core.vdsbroker.irsbroker;

import org.ovirt.engine.core.common.asynctasks.AsyncTaskCreationInfo;
import org.ovirt.engine.core.common.asynctasks.AsyncTaskType;
import org.ovirt.engine.core.common.utils.LocationInfoHelper;
import org.ovirt.engine.core.common.vdscommands.UploadImageVDSCommandParameters;
import org.ovirt.engine.core.compat.Guid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


// Currently implemented as irs command because we there's no way
// to monitor hsm tasks, this should be executed from hsm/vds command
// as it's becomes possible.
public class UploadImageVDSCommand<P extends UploadImageVDSCommandParameters> extends IrsCreateCommand<P> {

    private static final Logger log = LoggerFactory.getLogger(UploadImageVDSCommand.class);

    public UploadImageVDSCommand(P parameters) {
        super(parameters);
    }

    @Override
    protected void executeIrsBrokerCommand() {
        setReturnValue(Guid.Empty);
        log.info("-- executeIrsBrokerCommand: calling 'uploadImage', parameters:");
        log.info("++ srcSpUUID={}", getParameters().getStoragePoolId());
        log.info("++ srcSdUUID={}", getParameters().getStorageDomainId());
        log.info("++ srcImageGUID={}", getParameters().getImageGroupId());
        log.info("++ srcVolUUID={}", getParameters().getImageId());
        log.info("++ uploadLocation={}", getParameters().getUploadInfo());

        uuidReturn =
                getIrsProxy().uploadImage(
                        LocationInfoHelper.prepareLocationInfoForVdsCommand(getParameters().getUploadInfo()),
                        getParameters().getStoragePoolId().toString(),
                        getParameters().getStorageDomainId().toString(),
                        getParameters().getImageGroupId().toString(),
                        getParameters().getImageId().toString());

        proceedProxyReturnValue();
        getVDSReturnValue().setCreationInfo(
                new AsyncTaskCreationInfo(new Guid(uuidReturn.uuid), AsyncTaskType.copyImage, getParameters().getStoragePoolId()));
    }
}
