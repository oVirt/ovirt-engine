package org.ovirt.engine.core.bll;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.ovirt.engine.core.bll.snapshots.SnapshotsValidator;
import org.ovirt.engine.core.bll.utils.VmDeviceUtils;
import org.ovirt.engine.core.common.PermissionSubject;
import org.ovirt.engine.core.common.VdcObjectType;
import org.ovirt.engine.core.common.action.AddVmFromSnapshotParameters;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.businessentities.DiskImage;
import org.ovirt.engine.core.common.businessentities.ImageStatus;
import org.ovirt.engine.core.common.businessentities.Snapshot;
import org.ovirt.engine.core.common.businessentities.Snapshot.SnapshotStatus;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VMStatus;
import org.ovirt.engine.core.common.queries.GetVmConfigurationBySnapshotQueryParams;
import org.ovirt.engine.core.common.queries.VdcQueryReturnValue;
import org.ovirt.engine.core.common.queries.VdcQueryType;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.NGuid;
import org.ovirt.engine.core.dal.VdcBllMessages;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;
import org.ovirt.engine.core.dao.SnapshotDao;
import org.ovirt.engine.core.utils.transaction.TransactionMethod;
import org.ovirt.engine.core.utils.transaction.TransactionSupport;

/**
 * This class holds main logic for cloning a vm from snapshot
 *
 * @param <T>
 */

@LockIdNameAttribute(fieldName = "sourceSnapshotId")
@NonTransactiveCommandAttribute(forceCompensation = true)
public class AddVmFromSnapshotCommand<T extends AddVmFromSnapshotParameters> extends AddVmAndCloneImageCommand<T> {

    private Map<Guid, DiskImage> diskImagesFromClientMap;
    private Guid sourceSnapshotId;
    private Snapshot snapshot;
    private VM sourceVmFromDb;
    private VM vmFromConfiguration;
    private Collection<DiskImage> diskImagesFromConfiguration;
    private NGuid storageDomainId;

    protected AddVmFromSnapshotCommand(Guid commandId) {
        super(commandId);
    }

    public AddVmFromSnapshotCommand(T params) {
        super(params);
        sourceSnapshotId = params.getSourceSnapshotId();
        diskImagesFromClientMap = ImagesHandler.getDiskImagesByIdMap(params.getDiskInfoList());
    }

    @Override
    public NGuid getStoragePoolId() {
        return (getSourceVmFromDb() != null) ? getSourceVmFromDb().getstorage_pool_id() : null;
    }

    @Override
    public NGuid getStorageDomainId() {
        if (storageDomainId == null) {
            // This is needed for logging the command using CommandBase.logCommand
            List<DiskImage> images = getDiskImageDao().getAllSnapshotsForVmSnapshot(sourceSnapshotId);
            storageDomainId = (!images.isEmpty()) ? images.get(0).getstorage_ids().get(0) : Guid.Empty;
        }
        return storageDomainId;
    }

    @Override
    public List<PermissionSubject> getPermissionCheckSubjects() {
        List<PermissionSubject> permissionList = new ArrayList<PermissionSubject>();
        permissionList.add(new PermissionSubject(getVdsGroupId(),
                VdcObjectType.VdsGroups,
                getActionType().getActionGroup()));
        permissionList.add(new PermissionSubject(getVmIdFromSnapshot(),
                VdcObjectType.VM,
                getActionType().getActionGroup()));
        permissionList =
                QuotaHelper.getInstance().addQuotaPermissionSubject(permissionList, getStoragePool(), getQuotaId());
        return permissionList;
    }

    protected Guid getVmIdFromSnapshot() {
        return (getSnapshot() != null) ? getSnapshot().getVmId() : Guid.Empty;
    }

    @Override
    protected boolean AddVmImages() {
        int numberOfStartedCopyTasks = 0;
        try {
            if (!getDiskImagesFromConfiguration().isEmpty()) {
                lockEntities();
                for (DiskImage diskImage : getDiskImagesFromConfiguration()) {
                    // For illegal image check if it was snapshot as illegal (therefore
                    // still exists at DB, or was it erased after snapshot - therefore the
                    // query returned to UI an illegal image)
                    if (diskImage.getimageStatus() == ImageStatus.ILLEGAL) {
                        DiskImage snapshotImageInDb =
                                getDiskImageDao().getSnapshotById(diskImage.getId());
                        if (snapshotImageInDb == null) {
                            // If the snapshot diskImage is null, it means the disk was probably
                            // erased after the snapshot was created.
                            // Create a disk to reflect the fact the disk existed during snapshot
                            saveNewDiskAndImage(diskImage);
                            newDiskImages.add(diskImage);
                        }
                    } else {// Only legal images can be copied
                        copyDiskImage(diskImage,
                                diskImage.getstorage_ids().get(0),
                                imageToDestinationDomainMap.get(diskImage.getId()),
                                VdcActionType.AddVmFromSnapshot);
                        numberOfStartedCopyTasks++;
                    }
                }
            }
        } finally {
            // If no tasks were created, endAction will not be called, but
            // it is still needed to unlock the entities
            if (numberOfStartedCopyTasks == 0) {
                unlockEntities();
            }
        }
        return true;
    }

    protected DiskImage cloneDiskImage(Guid newVmId,
            Guid storageDomainId,
            Guid newImageGroupId,
            Guid newImageGuid,
            DiskImage srcDiskImage) {

        DiskImage clonedDiskImage =
                super.cloneDiskImage(newVmId, storageDomainId, newImageGroupId, newImageGuid, srcDiskImage);

        // If disk image information was passed from client , use its volume information
        // In case the disk image information was not passed from client, use the volume information of the ancestral
        // image
        if (diskImagesFromClientMap != null && diskImagesFromClientMap.containsKey(srcDiskImage.getId())) {
            DiskImage diskImageFromClient = diskImagesFromClientMap.get(srcDiskImage.getId());
            changeVolumeInfo(clonedDiskImage, diskImageFromClient);
        } else {
            DiskImage ancestorDiskImage = getDiskImageDao().getAncestor(srcDiskImage.getId());
            changeVolumeInfo(clonedDiskImage, ancestorDiskImage);
        }

        return clonedDiskImage;
    }

    protected void changeVolumeInfo(DiskImage clonedDiskImage, DiskImage diskImageFromClient) {
        clonedDiskImage.setvolume_format(diskImageFromClient.getvolume_format());
        clonedDiskImage.setvolume_type(diskImageFromClient.getvolume_type());
    }

    protected Collection<DiskImage> getDiskImagesFromConfiguration() {
        if (diskImagesFromConfiguration == null) {
            diskImagesFromConfiguration = vmFromConfiguration.getDiskMap().values();
        }
        return diskImagesFromConfiguration;
    }

    protected void logErrorOneOrMoreActiveDomainsAreMissing() {
        log.errorFormat("Can not found any default active domain for one of the disks of snapshot with id : {0}",
                sourceSnapshotId);
    }

    protected Collection<DiskImage> getDiskImagesToBeCloned() {
        return getDiskImagesFromConfiguration();
    }

    private void saveNewDiskAndImage(final DiskImage diskImage) {
        TransactionSupport.executeInNewTransaction(new TransactionMethod<Void>() {
            @Override
            public Void runInTransaction() {
                // Allocating new IDs for image and disk as it's possible
                // that more than one clone will be made from this snapshot
                // So this is required to avoid PK violation at DB.
                diskImage.setId(Guid.NewGuid());
                diskImage.setimage_group_id(Guid.NewGuid());
                diskImage.setParentId(Guid.Empty);
                diskImage.setit_guid(Guid.Empty);
                diskImage.setvm_guid(getVmId());
                ImagesHandler.addDiskImage(diskImage);
                return null;
            }
        });
    }

    @Override
    protected void copyVmDevices() {
        VmDeviceUtils.copyVmDevices(getVmIdFromSnapshot(), getVmId(), newDiskImages);
    }

    @Override
    protected VdcActionType getChildActionType() {
        return VdcActionType.MoveOrCopyImageGroup;
    }

    @Override
    protected void EndSuccessfully() {
        super.EndSuccessfully();
        unlockEntities();
    }

    @Override
    protected void EndWithFailure() {
        super.EndWithFailure();
        unlockEntities();
    }

    protected Snapshot getSnapshot() {
        if (snapshot == null) {
            snapshot = getSnapshotDao().get(sourceSnapshotId);
            if (snapshot != null) {
                setSnapshotName(snapshot.getDescription());
            }
        }
        return snapshot;
    }

    @Override
    protected boolean canDoAction() {
        // If snapshot does not exist, there is not point in checking any of the VM related checks
        if (getSnapshot() == null) {
            addCanDoActionMessage(VdcBllMessages.ACTION_TYPE_FAILED_VM_SNAPSHOT_DOES_NOT_EXIST);
            return false;
        }

        vmFromConfiguration = getVmFromConfiguration();
        if (vmFromConfiguration == null) {
            addCanDoActionMessage(VdcBllMessages.ACTION_TYPE_FAILED_VM_SNAPSHOT_HAS_NO_CONFIGURATION);
            addCanDoActionMessage(String.format("$VmName %1$s", getVmName()));
            addCanDoActionMessage(String.format("$SnapshotName %1$s", getSnapshotName()));

            return false;
        }

        // Run all checks for AddVm, now that it is determined snapshot exists
        if (!super.canDoAction()) {
            return false;
        }

        if (!canLockSnapshot()) {
            return false;
        }
        return true;
    }

    protected VM getVmFromConfiguration() {
        VM result = null;
        VdcQueryReturnValue queryReturnValue =
                Backend.getInstance().RunQuery(VdcQueryType.GetVmConfigurationBySnapshot,
                        new GetVmConfigurationBySnapshotQueryParams(snapshot.getId()));
        if (queryReturnValue.getSucceeded()) {
            result = (VM) queryReturnValue.getReturnValue();
        }
        return result;
    }

    protected boolean canLockSnapshot() {
        return validate(new SnapshotsValidator().vmNotDuringSnapshot(getSnapshot().getVmId()));
    }

    @Override
    protected int getNeededDiskSize(Guid storageDomainId) {
        // Get the needed disk size by accumulating disk size
        // of images on a given storage domain
        int result = 0;
        for (DiskImage img : getDiskImagesFromConfiguration()) {
            if (img.getimageStatus() != ImageStatus.ILLEGAL) {
                if (img.getstorage_ids().get(0).getValue().equals(storageDomainId)) {
                    result = result + (int) Math.ceil(img.getActualSize());
                }
            }
        }
        return result;
    }

    protected SnapshotDao getSnapshotDao() {
        return DbFacade.getInstance().getSnapshotDao();
    }

    private void lockEntities() {
        TransactionSupport.executeInNewTransaction(new TransactionMethod<Void>() {

            @Override
            public Void runInTransaction() {
                // Assumption - a snapshot can be locked only if in status OK, so if canDoAction passed
                // this is the status of the snapshot. In addition the newly added VM is in down status
                getCompensationContext().snapshotEntityStatus(getSnapshot(), getSnapshot().getStatus());
                getSnapshotDao().updateStatus(sourceSnapshotId, SnapshotStatus.LOCKED);
                lockVmWithCompensationIfNeeded();
                getCompensationContext().stateChanged();
                return null;
            }
        });
        freeLock();
    }

    @Override
    protected boolean validateQuota() {
        // Set default quota id if storage pool enforcement is disabled.
        getParameters().setQuotaId(QuotaHelper.getInstance().getQuotaIdToConsume(getQuotaIdFromSourceVmEntity(),
                getStoragePool()));
        for (DiskImage img : getImagesForQuotaValidation()) {
            img.setQuotaId(QuotaHelper.getInstance()
                    .getQuotaIdToConsume(getQuotaIdFromSourceVmEntity(),
                            getStoragePool()));
        }
        if (!isInternalExecution()) {
            // TODO: Should be changed when multiple storage domain will be implemented and the desired quotas will be
            // transferred.
            return QuotaManager.validateMultiStorageQuota(getStoragePool().getQuotaEnforcementType(),
                        QuotaHelper.getInstance().getQuotaConsumeMap(getImagesForQuotaValidation()),
                        getCommandId(),
                        getReturnValue().getCanDoActionMessages());
        }
        return true;
    }

    @Override
    protected boolean checkImageConfiguration(DiskImage diskImage) {
        return ImagesHandler.CheckImageConfiguration(destStorages.get(imageToDestinationDomainMap.get(diskImage.getId()))
                .getStorageStaticData(),
                diskImage,
                getReturnValue().getCanDoActionMessages());
    }

    protected Collection<DiskImage> getImagesForQuotaValidation() {
        return getDiskImagesFromConfiguration();
    }

    protected Guid getQuotaIdFromSourceVmEntity() {
        getSourceVmFromDb();
        return (getSourceVmFromDb() != null) ? getSourceVmFromDb().getQuotaId() : null;
    }

    protected VM getSourceVmFromDb() {
        if (sourceVmFromDb == null) {
            sourceVmFromDb = getVmDAO().get(getVmIdFromSnapshot());
        }
        return sourceVmFromDb;
    }

    private void unlockEntities() {
        // Assumption - this is last DB change of command, no need for compensation here
        getSnapshotDao().updateStatus(sourceSnapshotId, SnapshotStatus.OK);
        getVmDynamicDao().updateStatus(getVmId(), VMStatus.Down);
    }

    protected void removeQuotaCommandLeftOver() {
        if (!isInternalExecution()) {
            QuotaManager.removeMultiStorageDeltaQuotaCommand(QuotaHelper.getInstance()
                    .getQuotaConsumeMap(getImagesForQuotaValidation()),
                    getStoragePool().getQuotaEnforcementType(),
                    getCommandId());
        }
    }

}
