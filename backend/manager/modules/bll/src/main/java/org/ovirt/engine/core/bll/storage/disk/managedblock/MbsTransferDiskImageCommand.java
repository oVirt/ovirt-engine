package org.ovirt.engine.core.bll.storage.disk.managedblock;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import org.ovirt.engine.core.bll.NonTransactiveCommandAttribute;
import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.bll.storage.disk.image.TransferDiskImageCommand;
import org.ovirt.engine.core.bll.validator.storage.DiskImagesValidator;
import org.ovirt.engine.core.bll.validator.storage.DiskValidator;
import org.ovirt.engine.core.bll.validator.storage.ManagedBlockStorageDomainValidator;
import org.ovirt.engine.core.bll.validator.storage.StorageDomainValidator;
import org.ovirt.engine.core.common.action.ActionReturnValue;
import org.ovirt.engine.core.common.action.ActionType;
import org.ovirt.engine.core.common.action.ConnectManagedBlockStorageDeviceCommandParameters;
import org.ovirt.engine.core.common.action.DisconnectManagedBlockStorageDeviceParameters;
import org.ovirt.engine.core.common.action.TransferDiskImageParameters;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.storage.Disk;
import org.ovirt.engine.core.common.businessentities.storage.DiskImage;
import org.ovirt.engine.core.common.businessentities.storage.DiskImageDynamic;
import org.ovirt.engine.core.common.businessentities.storage.ImageStatus;
import org.ovirt.engine.core.common.businessentities.storage.ImageTransfer;
import org.ovirt.engine.core.common.businessentities.storage.ImageTransferPhase;
import org.ovirt.engine.core.common.businessentities.storage.ManagedBlockStorage;
import org.ovirt.engine.core.common.businessentities.storage.ManagedBlockStorageDisk;
import org.ovirt.engine.core.common.businessentities.storage.VolumeFormat;
import org.ovirt.engine.core.common.errors.EngineError;
import org.ovirt.engine.core.common.errors.EngineException;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.common.utils.SizeConverter;
import org.ovirt.engine.core.common.utils.managedblock.ManagedBlockCommandParameters;
import org.ovirt.engine.core.common.utils.managedblock.ManagedBlockExecutor;
import org.ovirt.engine.core.common.utils.managedblock.ManagedBlockExecutor.ManagedBlockCommand;
import org.ovirt.engine.core.common.vdscommands.AttachManagedBlockStorageVolumeVDSCommandParameters;
import org.ovirt.engine.core.common.vdscommands.ConvertManagedBlockVolumeVDSCommandParameters;
import org.ovirt.engine.core.common.vdscommands.VDSCommandType;
import org.ovirt.engine.core.common.vdscommands.VDSReturnValue;
import org.ovirt.engine.core.compat.CommandStatus;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.ManagedBlockStorageDao;
import org.ovirt.engine.core.utils.JsonHelper;
import org.ovirt.engine.core.utils.transaction.TransactionSupport;

@NonTransactiveCommandAttribute
public class MbsTransferDiskImageCommand<T extends TransferDiskImageParameters>
        extends TransferDiskImageCommand<T> {

    @Inject
    private ManagedBlockStorageDao managedBlockStorageDao;
    @Inject
    private ManagedBlockExecutor managedBlockExecutor;

    public MbsTransferDiskImageCommand(T parameters, CommandContext cmdContext) {
        super(parameters, cmdContext);
    }

    @Override
    protected boolean hostSelectionIgnoresDomainCache() {
        return true;
    }

    @Override
    protected boolean validateImageTransfer() {
        DiskImage diskImage = getDiskImage();
        if (!validate(ManagedBlockStorageDomainValidator.isOperationSupportedByManagedBlockStorage(getActionType()))) {
            return false;
        }
        DiskValidator diskValidator = getDiskValidator(diskImage);
        DiskImagesValidator diskImagesValidator = getDiskImagesValidator(diskImage);
        StorageDomainValidator storageDomainValidator = getStorageDomainValidator(
                storageDomainDao.getForStoragePool(diskImage.getStorageIds().get(0), diskImage.getStoragePoolId()));
        boolean isValid =
                validate(diskValidator.isDiskExists())
                && validate(diskImagesValidator.diskImagesNotIllegal())
                && validate(storageDomainValidator.isDomainExistAndActive());

        if (isBackup()) {
            if (isHybridBackup()) {
                if (!snapshotDao.exists(getBackup().getVmId(), getBackup().getSnapshotId())) {
                    return failValidation(EngineMessage.ACTION_TYPE_FAILED_VM_SNAPSHOT_DOES_NOT_EXIST);
                }
            }
            return isValid && validate(isVmBackupReady()) && validate(isFormatApplicableForBackup());
        }
        return isValid
                && validateActiveDiskPluggedToAnyNonDownVm(diskImage, diskValidator)
                && validate(diskImagesValidator.diskImagesNotLocked());
    }

    @Override
    protected String prepareImage(Guid vdsId) {
        if (isLiveBackup()) {
            return super.prepareImage(vdsId);
        }
        validateHostConnectorForMbs();
        DiskImage disk = getDiskImage();
        if (disk instanceof ManagedBlockStorageDisk) {
            String path = connectAttachAndGetMbsVolumePath((ManagedBlockStorageDisk) disk);
            if (path != null) {
                return path;
            }
        }
        return null;
    }

    @Override
    protected void tearDownImage(Guid vdsId, Guid backupId) {
        if (backupId != null) {
            return;
        }

        DiskImage image = getDiskImage();
        if (image.isDiskSnapshot() && !isDiskSnapshotPluggedToDownVmsOnly(image)) {
            return;
        }

        ImageTransfer entity = imageTransferDao.get(getCommandId());
        if (entity != null && entity.getDiskId() != null) {
            detachManagedBlockVolumeFromHost(entity);
            disconnectManagedBlockVolumeForTransfer(entity);
        }
    }

    @Override
    protected boolean needsConversionAfterUpload() {
        if (super.needsConversionAfterUpload()) {
            return true;
        }
        if (getParameters().getSourceVolumeFormat() == null
                && VolumeFormat.COW.equals(getParameters().getVolumeFormat())) {
            log.debug("needsConversionAfterUpload: true (MBS qcow2 upload)");
            return true;
        }
        return false;
    }

    @Override
    protected boolean connectManagedBlockVolumeBeforeNbd() {
        return connectAndAttachManagedBlockVolumeForTransfer();
    }

    @Override
    protected void detachManagedBlockVolumeWhenSessionStops(ImageTransfer entity) {
        if (!needsConversionAfterUpload() && entity.getDiskId() != null) {
            detachManagedBlockVolumeFromHost(entity);
            disconnectManagedBlockVolumeForTransfer(entity);
        }
    }

    @Override
    protected ManagedBlockUploadConversionResult startManagedBlockUploadConversion(StateContext context) {
        return startMbsUploadConversion(context) ? ManagedBlockUploadConversionResult.STARTED
                : ManagedBlockUploadConversionResult.FAILED;
    }

    @Override
    protected boolean handleManagedBlockConverting(StateContext context) {
        if (getParameters().getConvertedVolumeId() == null) {
            return false;
        }
        Guid sdId = getStorageDomainId();
        Guid srcVolId = getDiskImage().getImageId();
        Guid dstVolId = getParameters().getConvertedVolumeId();
        VolumeFormat srcFmt = getParameters().getSourceVolumeFormat() != null
                ? getParameters().getSourceVolumeFormat()
                : getParameters().getVolumeFormat();
        VolumeFormat dstFmt = getParameters().getSourceVolumeFormat() != null
                && getParameters().getVolumeFormat() != null
                ? getParameters().getVolumeFormat()
                : VolumeFormat.RAW;
        String srcFormat = srcFmt == VolumeFormat.COW ? "qcow2" : "raw";
        String dstFormat = dstFmt == VolumeFormat.COW ? "qcow2" : "raw";

        VDS vds = vdsDao.get(context.entity.getVdsId());
        if (vds == null) {
            log.error("Host not found for MBS conversion");
            updateEntityPhase(ImageTransferPhase.FINALIZING_FAILURE);
            setCommandStatus(CommandStatus.FAILED);
            return true;
        }
        try {
            ConvertManagedBlockVolumeVDSCommandParameters convertParams =
                    new ConvertManagedBlockVolumeVDSCommandParameters(vds, sdId, srcVolId, dstVolId, srcFormat, dstFormat);
            VDSReturnValue vdsReturnValue = runVdsCommand(VDSCommandType.ConvertManagedBlockVolume, convertParams);
            if (!vdsReturnValue.getSucceeded()) {
                log.error("ConvertManagedBlockVolume failed for transfer '{}': {}", getCommandId(), vdsReturnValue.getVdsError());
                updateEntityPhase(ImageTransferPhase.FINALIZING_FAILURE);
                setCommandStatus(CommandStatus.FAILED);
                return true;
            }
            log.info("MBS upload conversion completed for transfer '{}', finishing", getCommandId());
            finishMbsUploadConversion(context);
        } catch (Exception e) {
            log.error("Failed MBS conversion for transfer '{}': {}", getCommandId(), e);
            updateEntityPhase(ImageTransferPhase.FINALIZING_FAILURE);
            setCommandStatus(CommandStatus.FAILED);
        }
        return true;
    }

    @Override
    protected boolean setVolumeLegalityInStorage(boolean legal) {
        return true;
    }

    @Override
    protected boolean verifyImage(Guid transferingVdsId) {
        return true;
    }

    private boolean connectAndAttachManagedBlockVolumeForTransfer() {
        DiskImage disk = getDiskImage();
        if (!(disk instanceof ManagedBlockStorageDisk)) {
            return true;
        }
        ManagedBlockStorageDisk mbsDisk = (ManagedBlockStorageDisk) disk;
        Guid storageDomainId = mbsDisk.getStorageIds().isEmpty() ? getStorageDomainId() : mbsDisk.getStorageIds().get(0);

        ActionReturnValue connectResult = connectManagedBlockStorageDeviceForTransfer(mbsDisk, storageDomainId);
        if (!connectResult.getSucceeded()) {
            log.error("Failed to connect managed block volume for image transfer '{}': {}",
                    getCommandId(), connectResult.getFault());
            updateEntityPhaseToStoppedBySystem(
                    org.ovirt.engine.core.common.AuditLogType.TRANSFER_IMAGE_STOPPED_BY_SYSTEM_FAILED_TO_CREATE_TICKET);
            return false;
        }

        @SuppressWarnings("unchecked")
        Map<String, Object> connectionInfo = (Map<String, Object>) connectResult.getActionReturnValue();
        if (connectionInfo != null && !attachManagedBlockVolumeToHostForTransfer(mbsDisk, storageDomainId, connectionInfo)) {
            return false;
        }
        return true;
    }

    private ActionReturnValue connectManagedBlockStorageDeviceForTransfer(ManagedBlockStorageDisk mbsDisk,
            Guid storageDomainId) {
        ConnectManagedBlockStorageDeviceCommandParameters connectParams =
                new ConnectManagedBlockStorageDeviceCommandParameters(storageDomainId,
                        getVds().getConnectorInfo(),
                        mbsDisk.getImageId());
        return runInternalAction(ActionType.ConnectManagedBlockStorageDevice, connectParams);
    }

    private boolean attachManagedBlockVolumeToHostForTransfer(ManagedBlockStorageDisk mbsDisk,
            Guid storageDomainId,
            Map<String, Object> connectionInfo) {
        AttachManagedBlockStorageVolumeVDSCommandParameters attachParams =
                new AttachManagedBlockStorageVolumeVDSCommandParameters(getVds(),
                        connectionInfo,
                        storageDomainId);
        attachParams.setVolumeId(mbsDisk.getImageId());
        VDSReturnValue attachResult = runVdsCommand(VDSCommandType.AttachManagedBlockStorageVolume, attachParams);
        if (!attachResult.getSucceeded()) {
            log.error("Failed to attach managed block volume to host for image transfer '{}': {}",
                    getCommandId(), attachResult.getVdsError());
            updateEntityPhaseToStoppedBySystem(
                    org.ovirt.engine.core.common.AuditLogType.TRANSFER_IMAGE_STOPPED_BY_SYSTEM_FAILED_TO_CREATE_TICKET);
            return false;
        }
        return true;
    }

    private enum MbsConversionStartProgress {
        VOLUME_CREATED,
        CONNECTED_ON_HOST,
        ATTACHED_ON_HOST
    }

    private boolean startMbsUploadConversion(StateContext context) {
        Guid sdId = getStorageDomainId();
        Guid dstVolId = Guid.newGuid();
        ManagedBlockStorage managedBlockStorage = managedBlockStorageDao.get(sdId);
        if (managedBlockStorage == null) {
            log.error("Managed block storage domain '{}' not found for conversion", sdId);
            return false;
        }

        log.debug("MBS upload conversion for transfer '{}': disk={} srcVol={} -> dstVol={}, srcFmt={} destFmt={}",
                getCommandId(), getParameters().getImageGroupID(), getDiskImage().getImageId(), dstVolId,
                getParameters().getSourceVolumeFormat(), getParameters().getVolumeFormat());

        if (!mbsConversionCreateVolume(managedBlockStorage, dstVolId)) {
            return false;
        }

        VDS vds = vdsDao.get(context.entity.getVdsId());
        if (vds == null || vds.getConnectorInfo() == null) {
            log.error("Host or connector info missing for MBS conversion");
            cleanupOrphanMbsConversionVolume(managedBlockStorage, sdId, vds, dstVolId, null,
                    MbsConversionStartProgress.VOLUME_CREATED);
            return false;
        }

        ActionReturnValue connectResult = mbsConversionConnectVolume(vds, sdId, dstVolId);
        if (!connectResult.getSucceeded()) {
            log.error("Connect failed for MBS conversion: {}", connectResult.getFault());
            cleanupOrphanMbsConversionVolume(managedBlockStorage, sdId, vds, dstVolId, null,
                    MbsConversionStartProgress.VOLUME_CREATED);
            return false;
        }
        Map<String, Object> connectionInfo = connectResult.getActionReturnValue();
        if (connectionInfo == null) {
            log.error("No connection info returned for MBS conversion");
            cleanupOrphanMbsConversionVolume(managedBlockStorage, sdId, vds, dstVolId, null,
                    MbsConversionStartProgress.VOLUME_CREATED);
            return false;
        }

        if (!mbsConversionAttachVolume(vds, connectionInfo, sdId, dstVolId)) {
            log.error("Attach failed for MBS conversion");
            cleanupOrphanMbsConversionVolume(managedBlockStorage, sdId, vds, dstVolId, connectionInfo,
                    MbsConversionStartProgress.CONNECTED_ON_HOST);
            return false;
        }

        try {
            getParameters().setConvertedVolumeId(dstVolId);
            persistCommand(getParameters().getParentCommand(), true);
            log.info("Started MBS upload conversion for transfer '{}': convertedVolumeId={}",
                    getCommandId(), dstVolId);
            return true;
        } catch (Exception e) {
            log.error("Failed to persist MBS upload conversion for transfer '{}': {}", getCommandId(), e);
            cleanupOrphanMbsConversionVolume(managedBlockStorage, sdId, vds, dstVolId, connectionInfo,
                    MbsConversionStartProgress.ATTACHED_ON_HOST);
            return false;
        }
    }

    private boolean mbsConversionCreateVolume(ManagedBlockStorage managedBlockStorage, Guid dstVolId) {
        try {
            long sizeGiB = SizeConverter.convert(getDiskImage().getSize(),
                    SizeConverter.SizeUnit.BYTES, SizeConverter.SizeUnit.GiB).longValue();
            List<String> extraParams = new ArrayList<>();
            extraParams.add(dstVolId.toString());
            extraParams.add(Long.toString(sizeGiB));
            ManagedBlockCommandParameters params = new ManagedBlockCommandParameters(
                    JsonHelper.mapToJson(managedBlockStorage.getAllDriverOptions(), false),
                    extraParams, getCorrelationId());
            if (!managedBlockExecutor.runCommand(ManagedBlockCommand.CREATE_VOLUME, params).getSucceed()) {
                log.error("CREATE_VOLUME failed for transfer '{}'", getCommandId());
                return false;
            }
            return true;
        } catch (Exception e) {
            log.error("CREATE_VOLUME raised for transfer '{}': {}", getCommandId(), e);
            return false;
        }
    }

    private ActionReturnValue mbsConversionConnectVolume(VDS vds, Guid sdId, Guid dstVolId) {
        ConnectManagedBlockStorageDeviceCommandParameters connectParams =
                new ConnectManagedBlockStorageDeviceCommandParameters(sdId, vds.getConnectorInfo(), dstVolId);
        return runInternalAction(ActionType.ConnectManagedBlockStorageDevice, connectParams);
    }

    private boolean mbsConversionAttachVolume(VDS vds, Map<String, Object> connectionInfo, Guid sdId,
            Guid dstVolId) {
        AttachManagedBlockStorageVolumeVDSCommandParameters attachParams =
                new AttachManagedBlockStorageVolumeVDSCommandParameters(vds, connectionInfo, sdId);
        attachParams.setVolumeId(dstVolId);
        try {
            VDSReturnValue attachResult = runVdsCommand(VDSCommandType.AttachManagedBlockStorageVolume, attachParams);
            if (!attachResult.getSucceeded()) {
                log.error("Attach failed for MBS conversion: {}", attachResult.getVdsError());
                return false;
            }
            return true;
        } catch (Exception e) {
            log.error("Attach raised for MBS conversion: {}", e);
            return false;
        }
    }

    private void cleanupOrphanMbsConversionVolume(ManagedBlockStorage mbs, Guid sdId, VDS vds,
            Guid volId, Map<String, Object> connectionInfo, MbsConversionStartProgress progress) {
        if (mbs == null) {
            return;
        }
        log.warn("Cleaning up orphan MBS conversion volume '{}' for transfer '{}' (progress={})",
                volId, getCommandId(), progress);
        if (progress == MbsConversionStartProgress.ATTACHED_ON_HOST && vds != null) {
            mbsTryDetach(vds, sdId, volId);
        }
        if (progress == MbsConversionStartProgress.CONNECTED_ON_HOST
                || progress == MbsConversionStartProgress.ATTACHED_ON_HOST) {
            if (vds != null && connectionInfo != null) {
                mbsConversionTryDisconnectDevice(sdId, vds, volId, connectionInfo);
            }
            mbsTryDisconnect(mbs, volId);
        }
        mbsTryDelete(mbs, volId);
    }

    private void mbsConversionTryDisconnectDevice(Guid sdId, VDS vds, Guid volId,
            Map<String, Object> connectionInfo) {
        try {
            DisconnectManagedBlockStorageDeviceParameters disconnectParams =
                    new DisconnectManagedBlockStorageDeviceParameters(sdId, connectionInfo, volId, vds.getId());
            runInternalAction(ActionType.DisconnectManagedBlockStorageDevice, disconnectParams);
        } catch (Exception e) {
            log.warn("Orphan cleanup: disconnect device for volume {} failed: {}", volId, e);
        }
    }

    /**
     * Finish MBS conversion keeping base_disks.disk_id stable.
     *
     * The upload creates a volume whose UUID becomes disk_id. Conversion creates a
     * second volume (newVolId). Instead of creating a new base_disks row and
     * repointing every referencing table, we keep disk_id unchanged and only swap
     * the images row — exactly what finishUploadConversion does for non-MBS.
     *
     * DB invariant before: images.image_group_id = diskId, images.image_guid = oldImageId
     * DB invariant after:  images.image_group_id = diskId, images.image_guid = newVolId
     * base_disks.disk_id = diskId throughout — no repoint needed.
     */
    private void finishMbsUploadConversion(StateContext context) {
        final Guid diskId = getParameters().getImageGroupID(); // stable — does NOT change
        final Guid oldImageId = getDiskImage().getImageId(); // upload volume UUID
        final Guid newVolId = getParameters().getConvertedVolumeId(); // converted volume UUID
        final Guid sdId = getStorageDomainId();
        final DiskImage currentImage = getDiskImage();

        VDS vds = vdsDao.get(context.entity.getVdsId());
        if (vds == null) {
            log.error("Host not found for MBS conversion finish");
            setCommandStatus(CommandStatus.FAILED);
            return;
        }
        log.info("Finishing MBS upload conversion for transfer '{}': vol {} -> {} (disk={} stable)",
                getCommandId(), oldImageId, newVolId, diskId);

        ManagedBlockStorage mbs = managedBlockStorageDao.get(sdId);

        mbsTryDetach(vds, sdId, oldImageId);
        mbsTryDisconnect(mbs, oldImageId);
        mbsTryDelete(mbs, oldImageId);

        TransactionSupport.executeInNewTransaction(() -> {
            imageStorageDomainMapDao.remove(oldImageId);
            diskImageDynamicDao.remove(oldImageId);
            imageDao.remove(oldImageId);

            DiskImage finishedImage = buildConversionFinishImage(diskId, newVolId, currentImage);
            imagesHandler.saveImage(finishedImage); // images: image_group_id=diskId, image_guid=newVolId
            baseDiskDao.update(new ManagedBlockStorageDisk(finishedImage)); // update format in-place

            DiskImageDynamic dynamic = new DiskImageDynamic();
            dynamic.setId(newVolId);
            dynamic.setActualSize(currentImage.getActualSizeInBytes());
            diskImageDynamicDao.save(dynamic);
            return null;
        });

        setImageId(newVolId); // update images-layer ref; diskId is unchanged

        // Storage: detach/disconnect the converted volume (still attached from conversion).
        detachAndDisconnectMbsVolume(vds, newVolId, sdId, mbs);

        completeMbsConversionSuccess(context);
    }

    /**
     * Build the DiskImage for the conversion result.
     * diskId  = stable disk entity UUID (base_disks.disk_id = images.image_group_id) — unchanged.
     * imageId = new converted volume UUID  (images.image_guid).
     */
    private DiskImage buildConversionFinishImage(Guid diskId, Guid imageId, DiskImage src) {
        VolumeFormat destFormat = getParameters().getVolumeFormat() != null
                && getParameters().getSourceVolumeFormat() != null
                ? getParameters().getVolumeFormat()
                : VolumeFormat.RAW;
        DiskImage img = new DiskImage();
        img.setId(diskId); // stable — images.image_group_id
        img.setImageId(imageId); // new volume — images.image_guid
        img.setVolumeFormat(destFormat);
        img.setVolumeType(src.getVolumeType());
        img.setSize(src.getSize());
        img.setDiskAlias(src.getDiskAlias());
        img.setDiskDescription(src.getDiskDescription());
        img.setStorageIds(src.getStorageIds());
        img.setStoragePoolId(src.getStoragePoolId());
        img.setActive(true);
        img.setParentId(Guid.Empty);
        img.setImageTemplateId(Guid.Empty);
        img.setQuotaId(src.getQuotaId());
        img.setDiskProfileId(src.getDiskProfileId());
        img.setWipeAfterDelete(src.isWipeAfterDelete());
        if (VolumeFormat.COW.equals(getParameters().getVolumeFormat())) {
            img.setBackup(src.getBackup());
        }
        return img;
    }

    private ManagedBlockCommandParameters buildMbsParams(ManagedBlockStorage mbs, Guid volId)
            throws IOException {
        return new ManagedBlockCommandParameters(
                JsonHelper.mapToJson(mbs.getAllDriverOptions(), false),
                Collections.singletonList(volId.toString()),
                getCorrelationId());
    }

    private void mbsTryDetach(VDS vds, Guid sdId, Guid volId) {
        try {
            AttachManagedBlockStorageVolumeVDSCommandParameters params =
                    new AttachManagedBlockStorageVolumeVDSCommandParameters(vds);
            params.setVolumeId(volId);
            params.setStorageDomainId(sdId);
            runVdsCommand(VDSCommandType.DetachManagedBlockStorageVolume, params);
        } catch (Exception e) {
            log.warn("Detach volume '{}' failed: {}", volId, e);
        }
    }

    private void mbsTryDisconnect(ManagedBlockStorage mbs, Guid volId) {
        if (mbs == null) {
            return;
        }
        try {
            managedBlockExecutor.runCommand(ManagedBlockCommand.DISCONNECT_VOLUME,
                    buildMbsParams(mbs, volId));
        } catch (Exception e) {
            log.warn("Managed block DISCONNECT_VOLUME for '{}' failed: {}", volId, e);
        }
    }

    private void mbsTryDelete(ManagedBlockStorage mbs, Guid volId) {
        if (mbs == null) {
            return;
        }
        try {
            managedBlockExecutor.runCommand(ManagedBlockCommand.DELETE_VOLUME,
                    buildMbsParams(mbs, volId));
        } catch (Exception e) {
            log.warn("Managed block DELETE_VOLUME for '{}' failed: {}", volId, e);
        }
    }

    private void detachAndDisconnectMbsVolume(VDS vds, Guid volId, Guid sdId, ManagedBlockStorage mbs) {
        mbsTryDetach(vds, sdId, volId);
        mbsTryDisconnect(mbs, volId);
    }

    private void completeMbsConversionSuccess(StateContext context) {
        setVolumeLegalityInStorage(true);
        if (VolumeFormat.COW.equals(getParameters().getVolumeFormat()) && getDiskImage() != null) {
            setQcowCompat(getDiskImage().getImage(), getStoragePool().getId(), getDiskImage().getId(),
                    getDiskImage().getImageId(), getStorageDomainId(), context.entity.getVdsId());
            imageDao.update(getDiskImage().getImage());
        }
        setImageStatus(ImageStatus.OK);
        setAuditLogTypeFromPhase(ImageTransferPhase.FINISHED_SUCCESS);
        setCommandStatus(CommandStatus.SUCCEEDED);
    }

    private void validateHostConnectorForMbs() {
        if (getVds() == null || getVds().getConnectorInfo() == null) {
            throw new EngineException(EngineError.StorageException,
                    "Host has no connector info; cannot connect managed block volume for image transfer");
        }
    }

    private String connectAttachAndGetMbsVolumePath(ManagedBlockStorageDisk mbsDisk) {
        Guid storageDomainId = mbsDisk.getStorageIds().isEmpty()
                ? getStorageDomainId()
                : mbsDisk.getStorageIds().get(0);
        Map<String, Object> connectionInfo = connectMbsVolume(storageDomainId, mbsDisk.getImageId());
        if (connectionInfo == null) {
            return null;
        }
        return attachMbsVolumeAndGetPath(storageDomainId, mbsDisk.getImageId(), connectionInfo);
    }

    private Map<String, Object> connectMbsVolume(Guid storageDomainId, Guid volumeId) {
        ConnectManagedBlockStorageDeviceCommandParameters connectParams =
                new ConnectManagedBlockStorageDeviceCommandParameters(storageDomainId,
                        getVds().getConnectorInfo(),
                        volumeId);
        ActionReturnValue connectResult =
                runInternalAction(ActionType.ConnectManagedBlockStorageDevice, connectParams);
        if (!connectResult.getSucceeded()) {
            throw new EngineException(EngineError.StorageException,
                    connectResult.getFault() != null ? connectResult.getFault().getMessage() : "Failed to connect managed block volume");
        }
        return connectResult.getActionReturnValue();
    }

    private String attachMbsVolumeAndGetPath(Guid storageDomainId, Guid volumeId, Map<String, Object> connectionInfo) {
        AttachManagedBlockStorageVolumeVDSCommandParameters attachParams =
                new AttachManagedBlockStorageVolumeVDSCommandParameters(getVds(), connectionInfo, storageDomainId);
        attachParams.setVolumeId(volumeId);
        VDSReturnValue attachResult = runVdsCommand(VDSCommandType.AttachManagedBlockStorageVolume, attachParams);
        if (!attachResult.getSucceeded()) {
            throw new EngineException(EngineError.StorageException,
                    attachResult.getVdsError() != null ? attachResult.getVdsError().getMessage() : "Failed to attach managed block volume");
        }
        String path = extractPathFromAttachResult(attachResult.getReturnValue());
        if (path == null) {
            throw new EngineException(EngineError.StorageException,
                    "Attach managed block volume succeeded but did not return a path");
        }
        return FILE_URL_SCHEME + path;
    }

    @SuppressWarnings("unchecked")
    private static String extractPathFromAttachResult(Object returnValue) {
        if (!(returnValue instanceof Map)) {
            return null;
        }
        Map<String, Object> resultMap = (Map<String, Object>) returnValue;
        String path = (String) resultMap.get("path");
        if (path == null) {
            path = (String) resultMap.get("managed_path");
        }
        return path;
    }

    private void detachManagedBlockVolumeFromHost(ImageTransfer imageTransfer) {
        Disk disk = diskDao.get(imageTransfer.getDiskId());
        if (!(disk instanceof ManagedBlockStorageDisk)) {
            return;
        }
        ManagedBlockStorageDisk mbsDisk = (ManagedBlockStorageDisk) disk;
        if (mbsDisk.getStorageIds() == null || mbsDisk.getStorageIds().isEmpty()) {
            return;
        }
        VDS vds = vdsDao.get(imageTransfer.getVdsId());
        if (vds == null) {
            log.warn("Host '{}' not found for managed block volume detach", imageTransfer.getVdsId());
            return;
        }
        mbsTryDetach(vds, mbsDisk.getStorageIds().get(0), mbsDisk.getImageId());
    }

    private void disconnectManagedBlockVolumeForTransfer(ImageTransfer imageTransfer) {
        Disk disk = diskDao.get(imageTransfer.getDiskId());
        if (!(disk instanceof ManagedBlockStorageDisk)) {
            return;
        }
        ManagedBlockStorageDisk mbsDisk = (ManagedBlockStorageDisk) disk;
        if (mbsDisk.getStorageIds() == null || mbsDisk.getStorageIds().isEmpty()) {
            log.warn("Managed block disk '{}' has no storage domain for disconnect", imageTransfer.getDiskId());
            return;
        }
        Guid storageDomainId = mbsDisk.getStorageIds().get(0);
        ManagedBlockStorage managedBlockStorage = managedBlockStorageDao.get(storageDomainId);
        mbsTryDisconnect(managedBlockStorage, mbsDisk.getImageId());
    }
}
