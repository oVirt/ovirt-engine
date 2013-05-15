package org.ovirt.engine.core.bll;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.ovirt.engine.core.bll.snapshots.SnapshotsValidator;
import org.ovirt.engine.core.bll.utils.PermissionSubject;
import org.ovirt.engine.core.bll.utils.VmDeviceUtils;
import org.ovirt.engine.core.bll.validator.DiskImagesValidator;
import org.ovirt.engine.core.bll.validator.VmValidator;
import org.ovirt.engine.core.common.VdcObjectType;
import org.ovirt.engine.core.common.action.AddVmFromSnapshotParameters;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.businessentities.ActionGroup;
import org.ovirt.engine.core.common.businessentities.DiskImage;
import org.ovirt.engine.core.common.businessentities.ImageStatus;
import org.ovirt.engine.core.common.businessentities.Snapshot;
import org.ovirt.engine.core.common.businessentities.Snapshot.SnapshotStatus;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VMStatus;
import org.ovirt.engine.core.common.businessentities.VmDevice;
import org.ovirt.engine.core.common.businessentities.network.VmNetworkInterface;
import org.ovirt.engine.core.common.locks.LockingGroup;
import org.ovirt.engine.core.common.errors.VdcBllMessages;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.common.queries.VdcQueryReturnValue;
import org.ovirt.engine.core.common.queries.VdcQueryType;
import org.ovirt.engine.core.common.utils.Pair;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.NGuid;
import org.ovirt.engine.core.dao.SnapshotDao;
import org.ovirt.engine.core.utils.GuidUtils;
import org.ovirt.engine.core.utils.transaction.TransactionMethod;
import org.ovirt.engine.core.utils.transaction.TransactionSupport;

/**
 * This class holds main logic for cloning a vm from snapshot
 *
 * @param <T>
 */

@DisableInPrepareMode
@LockIdNameAttribute
@NonTransactiveCommandAttribute(forceCompensation = true)
public class AddVmFromSnapshotCommand<T extends AddVmFromSnapshotParameters> extends AddVmAndCloneImageCommand<T> {

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
    }

    @Override
    public NGuid getStoragePoolId() {
        return (getSourceVmFromDb() != null) ? getSourceVmFromDb().getStoragePoolId() : null;
    }

    @Override
    public NGuid getStorageDomainId() {
        if (storageDomainId == null) {
            // This is needed for logging the command using CommandBase.logCommand
            List<DiskImage> images = getDiskImageDao().getAllSnapshotsForVmSnapshot(sourceSnapshotId);
            storageDomainId = (!images.isEmpty()) ? images.get(0).getStorageIds().get(0) : Guid.Empty;
        }
        return storageDomainId;
    }

    @Override
    protected Guid getStoragePoolIdFromSourceImageContainer() {
        return sourceVmFromDb.getStoragePoolId().getValue();
    }

    @Override
    protected boolean shouldCheckSpaceInStorageDomains() {
        return !getImagesToCheckDestinationStorageDomains().isEmpty();
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
        for (DiskImage disk : getParameters().getDiskInfoDestinationMap().values()) {
            if (disk.getStorageIds() != null && !disk.getStorageIds().isEmpty()) {
                permissionList.add(new PermissionSubject(GuidUtils.getGuidValue(disk.getStorageIds().get(0)),
                        VdcObjectType.Storage, ActionGroup.CREATE_DISK));
            }
        }
        addPermissionSubjectForAdminLevelProperties(permissionList);
        return permissionList;
    }

    @Override
    protected List<VmNetworkInterface> getVmInterfaces() {
        if (_vmInterfaces == null) {
            _vmInterfaces = vmFromConfiguration.getInterfaces();
        }
        return _vmInterfaces;
    }

    protected Guid getVmIdFromSnapshot() {
        return (getSnapshot() != null) ? getSnapshot().getVmId() : Guid.Empty;
    }

    @Override
    protected boolean addVmImages() {
        int numberOfStartedCopyTasks = 0;
        try {
            if (!getDiskImagesFromConfiguration().isEmpty()) {
                lockEntities();
                for (DiskImage diskImage : getDiskImagesFromConfiguration()) {
                    // For illegal image check if it was snapshot as illegal (therefore
                    // still exists at DB, or was it erased after snapshot - therefore the
                    // query returned to UI an illegal image)
                    if (diskImage.getImageStatus() == ImageStatus.ILLEGAL) {
                        DiskImage snapshotImageInDb =
                                getDiskImageDao().getSnapshotById(diskImage.getImageId());
                        if (snapshotImageInDb == null) {
                            // If the snapshot diskImage is null, it means the disk was probably
                            // erased after the snapshot was created.
                            // Create a disk to reflect the fact the disk existed during snapshot
                            saveIllegalDisk(diskImage);
                        }
                    } else {// Only legal images can be copied
                        copyDiskImage(diskImage,
                                diskImage.getStorageIds().get(0),
                                diskInfoDestinationMap.get(diskImage.getId()).getStorageIds().get(0),
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

    @Override
    protected DiskImage cloneDiskImage(Guid newVmId,
            Guid storageDomainId,
            Guid newImageGroupId,
            Guid newImageGuid,
            DiskImage srcDiskImage) {

        DiskImage clonedDiskImage =
                super.cloneDiskImage(newVmId, storageDomainId, newImageGroupId, newImageGuid, srcDiskImage);
        // If volume information was changed at client , use its volume information.
        // If volume information was not changed at client - use the volume information of the ancestral image
        if (diskInfoDestinationMap != null && diskInfoDestinationMap.containsKey(srcDiskImage.getId())) {
            DiskImage diskImageFromClient = diskInfoDestinationMap.get(srcDiskImage.getId());
            if (volumeInfoChanged(diskImageFromClient, srcDiskImage)) {
                changeVolumeInfo(clonedDiskImage, diskImageFromClient);
            } else {
                DiskImage ancestorDiskImage = getDiskImageDao().getAncestor(srcDiskImage.getImageId());
                changeVolumeInfo(clonedDiskImage, ancestorDiskImage);
            }
        } else {
            DiskImage ancestorDiskImage = getDiskImageDao().getAncestor(srcDiskImage.getImageId());
            changeVolumeInfo(clonedDiskImage, ancestorDiskImage);
        }

        return clonedDiskImage;
    }

    private boolean volumeInfoChanged(DiskImage diskImageFromClient, DiskImage srcDiskImage) {
        return (diskImageFromClient.getVolumeFormat() != srcDiskImage.getVolumeFormat() || diskImageFromClient.getVolumeType() != srcDiskImage.getVolumeType());
    }

    protected void changeVolumeInfo(DiskImage clonedDiskImage, DiskImage diskImageFromClient) {
        clonedDiskImage.setvolumeFormat(diskImageFromClient.getVolumeFormat());
        clonedDiskImage.setVolumeType(diskImageFromClient.getVolumeType());
    }

    protected Collection<DiskImage> getDiskImagesFromConfiguration() {
        if (diskImagesFromConfiguration == null) {
            diskImagesFromConfiguration =
                    ImagesHandler.filterImageDisks(vmFromConfiguration.getDiskMap().values(),
                            false,
                            true);
        }
        return diskImagesFromConfiguration;
    }

    @Override
    protected void logErrorOneOrMoreActiveDomainsAreMissing() {
        log.errorFormat("Can not found any default active domain for one of the disks of snapshot with id : {0}",
                sourceSnapshotId);
    }

    @Override
    protected Collection<DiskImage> getDiskImagesToBeCloned() {
        return getDiskImagesFromConfiguration();
    }

    private void saveIllegalDisk(final DiskImage diskImage) {
        TransactionSupport.executeInNewTransaction(new TransactionMethod<Void>() {
            @Override
            public Void runInTransaction() {
                // Allocating new IDs for image and disk as it's possible
                // that more than one clone will be made from this snapshot
                // So this is required to avoid PK violation at DB.
                diskImage.setImageId(Guid.NewGuid());
                diskImage.setId(Guid.NewGuid());
                diskImage.setParentId(Guid.Empty);
                diskImage.setImageTemplateId(Guid.Empty);
                ImagesHandler.setDiskAlias(diskImage, getVm());
                ImagesHandler.addDiskImage(diskImage, getVmId());
                return null;
            }
        });
    }

    @Override
    protected void copyVmDevices() {
        List<VmDevice> devices = new ArrayList<VmDevice>(vmFromConfiguration.getVmUnamagedDeviceList());
        devices.addAll(vmFromConfiguration.getManagedVmDeviceMap().values());
        VmDeviceUtils.copyVmDevices(getVmIdFromSnapshot(), getVmId(), vmFromConfiguration, vmFromConfiguration.getStaticData(), true, devices, newDiskImages, _vmInterfaces);
    }

    @Override
    protected VdcActionType getChildActionType() {
        return VdcActionType.CopyImageGroup;
    }

    @Override
    protected void endSuccessfully() {
        super.endSuccessfully();
        unlockEntities();
    }

    @Override
    protected void endWithFailure() {
        super.endWithFailure();
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
        SnapshotsValidator snapshotsValidator = createSnapshotsValidator();

        // If snapshot does not exist or is broken, there is not point in checking any of the VM related checks
        if (!validate(snapshotsValidator.snapshotExists(getSnapshot()))
                || !validate(snapshotsValidator.snapshotNotBroken(getSnapshot()))
                || !validate(snapshotsValidator.vmNotDuringSnapshot(getSnapshot().getVmId()))) {
            return false;
        }

        vmFromConfiguration = getVmFromConfiguration();
        if (vmFromConfiguration == null) {
            addCanDoActionMessage(VdcBllMessages.ACTION_TYPE_FAILED_VM_SNAPSHOT_HAS_NO_CONFIGURATION);
            addCanDoActionMessage(String.format("$VmName %1$s", getVmName()));
            addCanDoActionMessage(String.format("$SnapshotName %1$s", getSnapshotName()));

            return false;
        }

        List<DiskImage> disksToCheck =
                ImagesHandler.filterImageDisks(getDiskDao().getAllForVm(getSourceVmFromDb().getId()), true, false);
        DiskImagesValidator diskImagesValidator = new DiskImagesValidator(disksToCheck);
        if (!validate(diskImagesValidator.diskImagesNotLocked())) {
            return false;
        }

        if (!validate(new VmValidator(getSourceVmFromDb()).vmNotLocked())) {
            return false;
        }

        // Run all checks for AddVm, now that it is determined snapshot exists
        if (!super.canDoAction()) {
            return false;
        }

        return true;
    }

    protected SnapshotsValidator createSnapshotsValidator() {
        return new SnapshotsValidator();
    }

    protected VM getVmFromConfiguration() {
        VM result = null;
        VdcQueryReturnValue queryReturnValue =
                Backend.getInstance().RunQuery(VdcQueryType.GetVmConfigurationBySnapshot,
                        new IdQueryParameters(snapshot.getId()));
        if (queryReturnValue.getSucceeded()) {
            result = (VM) queryReturnValue.getReturnValue();
        }
        return result;
    }

    @Override
    protected int getNeededDiskSize(Guid storageDomainId) {
        // Get the needed disk size by accumulating disk size
        // of images on a given storage domain
        int result = 0;
        for (DiskImage img : getDiskImagesFromConfiguration()) {
            if (img.getImageStatus() != ImageStatus.ILLEGAL) {
                if (img.getStorageIds().get(0).getValue().equals(storageDomainId)) {
                    result = result + (int) Math.ceil(img.getActualSize());
                }
            }
        }
        return result;
    }

    protected SnapshotDao getSnapshotDao() {
        return getDbFacade().getSnapshotDao();
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
    protected boolean checkImageConfiguration(DiskImage diskImage) {
        return ImagesHandler.CheckImageConfiguration(destStorages.get(diskInfoDestinationMap.get(diskImage.getId())
                .getStorageIds()
                .get(0))
                .getStorageStaticData(),
                diskImage,
                getReturnValue().getCanDoActionMessages());
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

    @Override
    protected Map<String, Pair<String, String>> getExclusiveLocks() {
        if (getSourceVmFromDb() == null) {
            return null;
        }

        return Collections.singletonMap(getSourceVmFromDb().getId().toString(),
                LockMessagesMatchUtil.makeLockingPair(LockingGroup.VM, VdcBllMessages.ACTION_TYPE_FAILED_OBJECT_LOCKED));
    }

}
