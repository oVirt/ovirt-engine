package org.ovirt.engine.core.vdsbroker.irsbroker;

import javax.inject.Inject;

import org.ovirt.engine.core.common.FeatureSupported;
import org.ovirt.engine.core.common.asynctasks.AsyncTaskCreationInfo;
import org.ovirt.engine.core.common.asynctasks.AsyncTaskType;
import org.ovirt.engine.core.common.vdscommands.CopyImageVDSCommandParameters;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.StoragePoolDao;
import org.ovirt.engine.core.vdsbroker.storage.StorageDomainHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CopyImageVDSCommand<P extends CopyImageVDSCommandParameters> extends IrsCreateCommand<P> {

    private static final Logger log = LoggerFactory.getLogger(CopyImageVDSCommand.class);

    @Inject
    private StoragePoolDao storagePoolDao;

    public CopyImageVDSCommand(P parameters) {
        super(parameters);
    }

    @Override
    protected void executeIrsBrokerCommand() {
        StorageDomainHelper.checkNumberOfLVsForBlockDomain(getParameters().getDstStorageDomainId());
        setReturnValue(Guid.Empty);

        log.info("-- executeIrsBrokerCommand: calling 'copyImage' with two new parameters: description and UUID. Parameters:");
        log.info("++ sdUUID={}", getParameters().getStorageDomainId());
        log.info("++ spUUID={}", getParameters().getStoragePoolId());
        log.info("++ vmGUID={}", getParameters().getVmId());
        log.info("++ srcImageGUID={}", getParameters().getImageGroupId());
        log.info("++ srcVolUUID={}", getParameters().getImageId());
        log.info("++ dstImageGUID={}", getParameters().getdstImageGroupId());
        log.info("++ dstVolUUID={}", getParameters().getDstImageId());
        log.info("++ descr={}", getParameters().getImageDescription());
        log.info("++ dstSdUUID={}", getParameters().getDstStorageDomainId());

        // NOTE: The 'uuidReturn' variable will contain the taskID and not the
        // created image id!
        uuidReturn = copyImage(getParameters().getStorageDomainId().toString(),
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
                                             getParameters().isDiscard(),
                                             String.valueOf(getParameters().getForce()).toLowerCase());
        proceedProxyReturnValue();

        Guid taskID = new Guid(uuidReturn.uuid);

        createdImageId = getParameters().getDstImageId().toString();
        setReturnValue(taskID);

        getVDSReturnValue().setCreationInfo(
                new AsyncTaskCreationInfo(taskID, AsyncTaskType.copyImage, getParameters().getStoragePoolId()));
    }

    private OneUuidReturn copyImage(String storageDomainId, String storagePoolId, String vmId, String imageGroupId,
            String imageId, String dstImageGroupId, String dstImageId, String imageDescription,
            String dstStorageDomainId, int copyVolumeType, int volumeFormat, int preallocate, String postZero,
            boolean discard, String force) {
        if (FeatureSupported.discardAfterDeleteSupported(
                storagePoolDao.get(getParameters().getStoragePoolId()).getCompatibilityVersion())) {
            return getIrsProxy().copyImage(storageDomainId, storagePoolId, vmId, imageGroupId, imageId, dstImageGroupId,
                    dstImageId, imageDescription, dstStorageDomainId, copyVolumeType, volumeFormat, preallocate,
                    postZero, discard, force);
        }
        return getIrsProxy().copyImage(storageDomainId, storagePoolId, vmId, imageGroupId, imageId, dstImageGroupId,
                    dstImageId, imageDescription, dstStorageDomainId, copyVolumeType, volumeFormat, preallocate,
                    postZero, force);
    }
}
