package org.ovirt.engine.core.vdsbroker.irsbroker;

import org.ovirt.engine.core.common.asynctasks.AsyncTaskCreationInfo;
import org.ovirt.engine.core.common.asynctasks.AsyncTaskType;
import org.ovirt.engine.core.common.utils.LocationInfoHelper;
import org.ovirt.engine.core.common.vdscommands.DownloadImageVDSCommandParameters;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.utils.log.Log;
import org.ovirt.engine.core.utils.log.LogFactory;

public class DownloadImageVDSCommand<P extends DownloadImageVDSCommandParameters> extends IrsCreateCommand<P> {

    private static Log log = LogFactory.getLog(DownloadImageVDSCommand.class);

    public DownloadImageVDSCommand(P parameters) {
        super(parameters);
    }

    @Override
    protected void executeIrsBrokerCommand() {
        setReturnValue(Guid.Empty);

        log.info("-- executeIrsBrokerCommand: calling 'downloadImage' ");
        log.infoFormat("-- downloadImage parameters:" + "\r\n"
                + "                dstSpUUID={0}" + "\r\n"
                + "                dstSdUUID={1}" + "\r\n"
                + "                dstImageGUID={2}" + "\r\n"
                + "                dstVolUUID={3}" + "\r\n"
                + "                importLocation={4}" + "\r\n",
                getParameters().getStoragePoolId().toString(),
                getParameters().getStorageDomainId().toString(),
                getParameters().getImageGroupId().toString(),
                getParameters().getImageId().toString(),
                getParameters().getDownloadInfo());

        uuidReturn =
                getIrsProxy().downloadImage(
                        LocationInfoHelper.prepareLocationInfoForVdsCommand(getParameters().getDownloadInfo()),
                        getParameters().getStoragePoolId().toString(),
                        getParameters().getStorageDomainId().toString(),
                        getParameters().getImageGroupId().toString(),
                        getParameters().getImageId().toString());

        proceedProxyReturnValue();

        getVDSReturnValue().setCreationInfo(
                new AsyncTaskCreationInfo(new Guid(uuidReturn.mUuid), AsyncTaskType.copyImage, getParameters().getStoragePoolId()));
    }
}
