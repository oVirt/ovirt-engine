package org.ovirt.engine.core.vdsbroker.irsbroker;

import org.ovirt.engine.core.common.asynctasks.AsyncTaskCreationInfo;
import org.ovirt.engine.core.common.asynctasks.AsyncTaskType;
import org.ovirt.engine.core.common.utils.LocationInfoHelper;
import org.ovirt.engine.core.common.vdscommands.DownloadImageVDSCommandParameters;
import org.ovirt.engine.core.compat.Guid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DownloadImageVDSCommand<P extends DownloadImageVDSCommandParameters> extends IrsCreateCommand<P> {

    private static final Logger log = LoggerFactory.getLogger(DownloadImageVDSCommand.class);

    public DownloadImageVDSCommand(P parameters) {
        super(parameters);
    }

    @Override
    protected void executeIrsBrokerCommand() {
        setReturnValue(Guid.Empty);

        log.info("-- executeIrsBrokerCommand: calling 'downloadImage', parameters:");
        log.info("++ dstSpUUID={}", getParameters().getStoragePoolId());
        log.info("++ dstSdUUID={}", getParameters().getStorageDomainId());
        log.info("++ dstImageGUID={}", getParameters().getImageGroupId());
        log.info("++ dstVolUUID={}", getParameters().getImageId());
        log.info("++ importLocation={}", getParameters().getDownloadInfo());

        uuidReturn =
                getIrsProxy().downloadImage(
                        LocationInfoHelper.prepareLocationInfoForVdsCommand(getParameters().getDownloadInfo()),
                        getParameters().getStoragePoolId().toString(),
                        getParameters().getStorageDomainId().toString(),
                        getParameters().getImageGroupId().toString(),
                        getParameters().getImageId().toString());

        proceedProxyReturnValue();

        getVDSReturnValue().setCreationInfo(
                new AsyncTaskCreationInfo(new Guid(uuidReturn.uuid), AsyncTaskType.downloadImage, getParameters().getStoragePoolId()));
    }
}
