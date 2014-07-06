package org.ovirt.engine.core.vdsbroker.irsbroker;

import org.ovirt.engine.core.common.asynctasks.AsyncTaskCreationInfo;
import org.ovirt.engine.core.common.asynctasks.AsyncTaskType;
import org.ovirt.engine.core.common.vdscommands.CopyImageVDSCommandParameters;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.utils.log.Log;
import org.ovirt.engine.core.utils.log.LogFactory;
import org.ovirt.engine.core.vdsbroker.storage.StorageDomainHelper;

public class CopyImageVDSCommand<P extends CopyImageVDSCommandParameters> extends IrsCreateCommand<P> {
    public CopyImageVDSCommand(P parameters) {
        super(parameters);
    }

    @Override
    protected void executeIrsBrokerCommand() {
        StorageDomainHelper.checkNumberOfLVsForBlockDomain(getParameters().getDstStorageDomainId());
        /**
         * TODO: EINAV: - Consider 'RunAsync' parameter (pass it to IRS too). - Add 'description' parameter. - we should
         * provide 'volumeUUID'. - TaskID should be provided by IRS.
         */
        setReturnValue(Guid.Empty);

        log.info("-- executeIrsBrokerCommand: calling 'copyImage' with two new parameters: description and UUID");
        log.infoFormat("-- copyImage parameters:" + "\r\n" + "                sdUUID={0}" + "\r\n"
                + "                spUUID={1}" + "\r\n" + "                vmGUID={2}" + "\r\n"
                + "                srcImageGUID={3}" + "\r\n" + "                srcVolUUID={4}" + "\r\n"
                + "                dstImageGUID={5}" + "\r\n" + "                dstVolUUID={6}" + "\r\n"
                + "                descr={7}" + "\r\n" + "                dstSdUUID={8}"
                , getParameters().getStorageDomainId()
                .toString(), getParameters().getStoragePoolId().toString(), getParameters().getVmId()
                .toString(), getParameters().getImageGroupId().toString(), getParameters().getImageId()
                .toString(), getParameters().getdstImageGroupId().toString(), getParameters().getDstImageId()
                .toString(), getParameters().getImageDescription(), getParameters().getDstStorageDomainId());

        // NOTE: The 'uuidReturn' variable will contain the taskID and not the
        // created image id!
        uuidReturn = getIrsProxy().copyImage(getParameters().getStorageDomainId().toString(),
                                             getParameters().getStoragePoolId().toString(),
                                             getParameters().getVmId().toString(),
                                             getParameters().getImageGroupId().toString(),
                                             getParameters().getImageId().toString(),
                                             getParameters().getdstImageGroupId().toString(),
                                             getParameters().getDstImageId().toString(),
                                             getParameters().getImageDescription(),
                                             getParameters().getDstStorageDomainId().toString(),
                                             getParameters().getCopyVolumeType().getValue(),
                                             getParameters().getVolumeFormat().getValue(),
                                             getParameters().getPreallocate().getValue(),
                                             String.valueOf(getParameters().getPostZero()).toLowerCase(),
                                             String.valueOf(getParameters().getForce()).toLowerCase());
        proceedProxyReturnValue();

        Guid taskID = new Guid(uuidReturn.mUuid);

        mCreatedImageId = getParameters().getDstImageId().toString();
        setReturnValue(taskID);

        getVDSReturnValue().setCreationInfo(
                new AsyncTaskCreationInfo(taskID, AsyncTaskType.copyImage, getParameters().getStoragePoolId()));
    }

    private static final Log log = LogFactory.getLog(CopyImageVDSCommand.class);
}
