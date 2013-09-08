package org.ovirt.engine.core.vdsbroker.irsbroker;

import org.ovirt.engine.core.common.asynctasks.AsyncTaskCreationInfo;
import org.ovirt.engine.core.common.asynctasks.AsyncTaskType;
import org.ovirt.engine.core.common.utils.LocationInfoHelper;
import org.ovirt.engine.core.common.vdscommands.UploadImageVDSCommandParameters;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.utils.log.Log;
import org.ovirt.engine.core.utils.log.LogFactory;


// Currently implemented as irs command because we there's no way
// to monitor hsm tasks, this should be executed from hsm/vds command
// as it's becomes possible.
public class UploadImageVDSCommand<P extends UploadImageVDSCommandParameters> extends IrsCreateCommand<P> {

    private static Log log = LogFactory.getLog(UploadImageVDSCommand.class);

    public UploadImageVDSCommand(P parameters) {
        super(parameters);
    }

    @Override
    protected void executeIrsBrokerCommand() {
        setReturnValue(Guid.Empty);
        log.info("-- executeIrsBrokerCommand: calling 'uploadImage' ");
        log.infoFormat("-- uploadImage parameters:" + "\r\n"
                + "                srcSpUUID={0}" + "\r\n"
                + "                srcSdUUID={1}" + "\r\n"
                + "                srcImageGUID={2}" + "\r\n"
                + "                srcVolUUID={3}" + "\r\n"
                + "                uploadLocation={4}",
                getParameters().getStoragePoolId().toString(),
                getParameters().getStorageDomainId().toString(),
                getParameters().getImageGroupId().toString(),
                getParameters().getImageId().toString(),
                getParameters().getUploadInfo());

        uuidReturn =
                getIrsProxy().uploadImage(
                        LocationInfoHelper.prepareLocationInfoForVdsCommand(getParameters().getUploadInfo()),
                        getParameters().getStoragePoolId().toString(),
                        getParameters().getStorageDomainId().toString(),
                        getParameters().getImageGroupId().toString(),
                        getParameters().getImageId().toString());

        proceedProxyReturnValue();
        getVDSReturnValue().setCreationInfo(
                new AsyncTaskCreationInfo(new Guid(uuidReturn.mUuid), AsyncTaskType.copyImage, getParameters().getStoragePoolId()));
    }
}
