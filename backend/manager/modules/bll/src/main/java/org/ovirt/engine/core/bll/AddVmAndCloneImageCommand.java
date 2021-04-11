package org.ovirt.engine.core.bll;

import static org.ovirt.engine.core.bll.storage.disk.image.DisksFilter.ONLY_ACTIVE;
import static org.ovirt.engine.core.bll.storage.disk.image.DisksFilter.ONLY_NOT_SHAREABLE;
import static org.ovirt.engine.core.bll.storage.disk.image.DisksFilter.ONLY_PLUGGED;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.inject.Inject;

import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.bll.network.vm.VnicProfileHelper;
import org.ovirt.engine.core.bll.storage.disk.image.DisksFilter;
import org.ovirt.engine.core.bll.storage.disk.image.ImagesHandler;
import org.ovirt.engine.core.bll.utils.PermissionSubject;
import org.ovirt.engine.core.bll.validator.VmValidator;
import org.ovirt.engine.core.bll.validator.storage.DiskImagesValidator;
import org.ovirt.engine.core.bll.validator.storage.MultipleStorageDomainsValidator;
import org.ovirt.engine.core.bll.validator.storage.StorageDomainValidator;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.VdcObjectType;
import org.ovirt.engine.core.common.action.ActionParametersBase;
import org.ovirt.engine.core.common.action.ActionReturnValue;
import org.ovirt.engine.core.common.action.ActionType;
import org.ovirt.engine.core.common.action.AddVmParameters;
import org.ovirt.engine.core.common.action.MoveOrCopyImageGroupParameters;
import org.ovirt.engine.core.common.asynctasks.EntityInfo;
import org.ovirt.engine.core.common.businessentities.ActionGroup;
import org.ovirt.engine.core.common.businessentities.StorageDomain;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VmDevice;
import org.ovirt.engine.core.common.businessentities.network.VmNetworkInterface;
import org.ovirt.engine.core.common.businessentities.network.VmNic;
import org.ovirt.engine.core.common.businessentities.storage.CinderDisk;
import org.ovirt.engine.core.common.businessentities.storage.CopyVolumeType;
import org.ovirt.engine.core.common.businessentities.storage.Disk;
import org.ovirt.engine.core.common.businessentities.storage.DiskImage;
import org.ovirt.engine.core.common.businessentities.storage.DiskStorageType;
import org.ovirt.engine.core.common.businessentities.storage.ImageDbOperationScope;
import org.ovirt.engine.core.common.businessentities.storage.ImageOperation;
import org.ovirt.engine.core.common.businessentities.storage.ImageStatus;
import org.ovirt.engine.core.common.businessentities.storage.ManagedBlockStorageDisk;
import org.ovirt.engine.core.common.errors.EngineError;
import org.ovirt.engine.core.common.errors.EngineException;
import org.ovirt.engine.core.common.job.Job;
import org.ovirt.engine.core.common.utils.Pair;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.DiskDao;
import org.ovirt.engine.core.dao.DiskImageDao;
import org.ovirt.engine.core.dao.StorageDomainDao;
import org.ovirt.engine.core.dao.VmStaticDao;
import org.ovirt.engine.core.utils.transaction.TransactionSupport;

/**
 * This abstract class holds helper methods for concrete command classes that require to add a VM and clone an image in
 * the process
 */
public abstract class AddVmAndCloneImageCommand<T extends AddVmParameters> extends AddVmCommand<T> {

    @Inject
    private StorageDomainDao storageDomainDao;
    @Inject
    private DiskDao diskDao;
    @Inject
    private DiskImageDao diskImageDao;
    @Inject
    private VmStaticDao vmStaticDao;

    protected AddVmAndCloneImageCommand(Guid commandId) {
        super(commandId);
    }

    public AddVmAndCloneImageCommand(T parameters, CommandContext commandContext) {
        super(parameters, commandContext);
    }

    @Override
    protected boolean validateIsImagesOnDomains() {
        return true;
    }

    @Override
    protected Map<String, Pair<String, String>> getSharedLocks() {
        return null;
    }

    protected void copyDiskImage(
            DiskImage diskImage,
            Guid srcStorageDomainId,
            Guid destStorageDomainId,
            Guid diskProfileId,
            ActionType parentCommandType) {
        DiskImage newDiskImage = imagesHandler.cloneDiskImage(destStorageDomainId,
                Guid.newGuid(),
                Guid.newGuid(),
                diskImage,
                diskProfileId,
                getVmSnapshotId(),
                diskInfoDestinationMap != null ? diskInfoDestinationMap.get(diskImage.getId()) : null);
        ImagesHandler.setDiskAlias(newDiskImage, getVm());
        newDiskImage.setDiskAlias(String.format("%s_%s", newDiskImage.getDiskAlias(), getVm().getName()));
        newDiskImage.setActive(true);
        MoveOrCopyImageGroupParameters parameters = createCopyParameters(newDiskImage,
                srcStorageDomainId,
                diskImage.getId(),
                diskImage.getImageId(), parentCommandType);
        parameters.setRevertDbOperationScope(ImageDbOperationScope.IMAGE);
        ActionReturnValue result = executeChildCopyingCommand(parameters);
        if (parameters.getDestImages().isEmpty()) {
            parameters.getDestImages().add(newDiskImage);
        }

        handleCopyResult(diskImage, parameters.getDestImages(), result);
    }

    @Override
    protected void removeVmRelatedEntitiesFromDb() {
        removeVmImages();
        super.removeVmRelatedEntitiesFromDb();
    }

    protected void removeVmImages() {
        // Remove vm images, in case they were not already removed by child commands
        List<ActionParametersBase> imageParams = getParameters().getImagesParameters();
        for (ActionParametersBase param : imageParams) {
            DiskImage diskImage = getDiskImageToRemoveByParam((MoveOrCopyImageGroupParameters) param);
            if (diskImage != null) {
                imagesHandler.removeDiskImage(diskImage, getVmId());
            }
        }
    }

    @Override
    protected Collection<DiskImage> getImagesToCheckDestinationStorageDomains() {
        return getDiskImagesToBeCloned();
    }

    protected MoveOrCopyImageGroupParameters createCopyParameters(DiskImage diskImage,
            Guid srcStorageDomainId,
            Guid srcImageGroupId,
            Guid srcImageId, ActionType parentCommandType) {
        MoveOrCopyImageGroupParameters params =
                new MoveOrCopyImageGroupParameters(getVmId(),
                        srcImageGroupId,
                        srcImageId,
                        diskImage.getId(),
                        diskImage.getImageId(),
                        diskImage.getStorageIds().get(0),
                        ImageOperation.Copy);
        params.setAddImageDomainMapping(false);
        params.setCopyVolumeType(CopyVolumeType.LeafVol);
        params.setVolumeFormat(diskImage.getVolumeFormat());
        params.setVolumeType(diskImage.getVolumeType());
        params.setUseCopyCollapse(true);
        params.setSourceDomainId(srcStorageDomainId);
        params.setWipeAfterDelete(diskImage.isWipeAfterDelete());
        params.setEntityInfo(new EntityInfo(VdcObjectType.VM, getVmId()));
        params.setParentParameters(getParameters());
        params.setParentCommand(parentCommandType);
        params.setJobWeight(Job.MAX_WEIGHT);

        return params;
    }

    private List<DiskImage> getDiskImagesToValidate() {
        List<Disk> disks = diskDao.getAllForVm(getSourceVmFromDb().getId());
        List<DiskImage> allDisks = DisksFilter.filterImageDisks(disks, ONLY_NOT_SHAREABLE, ONLY_ACTIVE);
        List<CinderDisk> cinderDisks = DisksFilter.filterCinderDisks(disks, ONLY_PLUGGED);
        allDisks.addAll(cinderDisks);
        allDisks.addAll(DisksFilter.filterManagedBlockStorageDisks(disks, ONLY_PLUGGED));
        return allDisks;
    }

    @Override
    protected boolean validate() {
        List<DiskImage> disksToCheck = getDiskImagesToValidate();
        DiskImagesValidator diskImagesValidator = new DiskImagesValidator(disksToCheck);
        if (!validate(diskImagesValidator.diskImagesNotLocked())) {
            return false;
        }

        Set<Guid> storageIds = ImagesHandler.getAllStorageIdsForImageIds(disksToCheck);
        MultipleStorageDomainsValidator storageValidator =
                new MultipleStorageDomainsValidator(getStoragePoolId(), storageIds);
        if (!validate(storageValidator.allDomainsExistAndActive())) {
            return false;
        }
        if (!validate(storageValidator.isSupportedByManagedBlockStorageDomains(getActionType()))) {
            return false;
        }

        if (!validate(new VmValidator(getSourceVmFromDb()).vmNotLocked())) {
            return false;
        }

        // Run all checks for AddVm, now that it is determined snapshot exists
        if (!super.validate()) {
            return false;
        }

        for (DiskImage diskImage : getDiskImagesToBeCloned()) {
            if (diskImage.getDiskStorageType() == DiskStorageType.IMAGE && !checkImageConfiguration(diskImage)) {
                return false;
            }
        }

        return true;
    }

    protected boolean checkImageConfiguration(DiskImage diskImage) {
        return ImagesHandler.checkImageConfiguration(destStorages.get(diskInfoDestinationMap.get(diskImage.getId())
                .getStorageIds()
                .get(0))
                .getStorageStaticData(),
                diskImage,
                getReturnValue().getValidationMessages());
    }

    /**
     * Handle the result of copying the image
     * @param srcDiskImage
     *            disk image that represents the source image
     * @param copiedDiskImages
     *            list of disk images that represents the copied images
     * @param result
     *            result of execution of child command
     */
    private void handleCopyResult(DiskImage srcDiskImage, List<DiskImage> copiedDiskImages, ActionReturnValue result) {
        // If a copy cannot be made, abort
        if (!result.getSucceeded()) {
            throw new EngineException(EngineError.VolumeCreationError);
        } else {
            copiedDiskImages.forEach(diskImage -> imagesHandler.addDiskImageWithNoVmDevice(diskImage));
            getTaskIdList().addAll(result.getInternalVdsmTaskIdList());
            getSrcDiskIdToTargetDiskIdMapping().put(srcDiskImage.getId(), copiedDiskImages.get(0).getId());
        }
    }

    /**
     * Executes the child command responsible for the image copying
     * @param parameters
     *            parameters for copy
     */
    protected ActionReturnValue executeChildCopyingCommand(ActionParametersBase parameters) {
        return runInternalActionWithTasksContext(getChildActionType(), parameters);
    }

    @Override
    protected boolean buildAndCheckDestStorageDomains() {
        if (diskInfoDestinationMap.isEmpty()) {
            List<StorageDomain> domains = storageDomainDao.getAllForStoragePool(getStoragePoolId());
            Map<Guid, StorageDomain> storageDomainsMap = new HashMap<>();
            for (StorageDomain storageDomain : domains) {
                StorageDomainValidator validator = new StorageDomainValidator(storageDomain);
                if (validate(validator.isDomainExistAndActive()) && validate(validator.domainIsValidDestination())) {
                    storageDomainsMap.put(storageDomain.getId(), storageDomain);
                }
            }
            for (Disk disk : getDiskImagesToBeCloned()) {
                DiskImage image = (DiskImage) disk;
                for (Guid storageId : image.getStorageIds()) {
                    if (storageDomainsMap.containsKey(storageId)) {
                        diskInfoDestinationMap.put(image.getId(), image);
                        break;
                    }
                }
            }
            if (getDiskImagesToBeCloned().size() != diskInfoDestinationMap.size()) {
                logErrorOneOrMoreActiveDomainsAreMissing();
                return false;
            }
            List<Guid> storageDomainDest = new ArrayList<>();
            for (DiskImage diskImage : diskInfoDestinationMap.values()) {
                Guid storageDomainId = diskImage.getStorageIds().get(0);
                if (storageDomainDest.contains(storageDomainId)) {
                    destStorages.put(storageDomainId, storageDomainsMap.get(storageDomainId));
                }
                storageDomainDest.add(storageDomainId);
            }
            return true;
        }
        return super.buildAndCheckDestStorageDomains();
    }

    @Override
    protected boolean validateFreeSpace(StorageDomainValidator storageDomainValidator, List<DiskImage> disksList) {
        for (DiskImage diskImage : disksList) {
            List<DiskImage> snapshots = diskImageDao.getAllSnapshotsForLeaf(diskImage.getImageId());
            diskImage.getSnapshots().addAll(snapshots);
        }
        return validate(storageDomainValidator.hasSpaceForClonedDisks(disksList));
    }

    /**
     * Logs error if one or more active domains are missing for disk images
     */
    protected abstract void logErrorOneOrMoreActiveDomainsAreMissing();

    /**
     * Returns collection of DiskImage objects to use for construction of the imageToDestinationDomainMap
     */
    protected Collection<DiskImage> getDiskImagesToBeCloned() {
        return getSourceDisks();
    }

    protected abstract Collection<DiskImage> getSourceDisks();

    protected DiskImage getDiskImageToRemoveByParam(MoveOrCopyImageGroupParameters param) {
        Guid imageGroupId = param.getDestImageGroupId();
        Guid imageId = param.getDestinationImageId();
        DiskImage diskImage = new DiskImage();
        diskImage.setId(imageGroupId);
        diskImage.setImageId(imageId);
        return diskImage;
    }

    @Override
    protected void executeVmCommand() {
        super.executeVmCommand();
        setVm(null);
        if (getActionType() != ActionType.CloneVmNoCollapse) {
            getVm().setVmtGuid(VmTemplateHandler.BLANK_VM_TEMPLATE_ID);
        }

        vmStaticDao.update(getVm().getStaticData());
    }

    @Override
    protected boolean checkTemplateImages() {
        return true;
    }

    @Override
    protected abstract Guid getStoragePoolIdFromSourceImageContainer();

    @Override
    protected boolean shouldCheckSpaceInStorageDomains() {
        return !getImagesToCheckDestinationStorageDomains().isEmpty();
    }

    @Override
    public List<PermissionSubject> getPermissionCheckSubjects() {
        List<PermissionSubject> permissionList = new ArrayList<>();
        permissionList.add(new PermissionSubject(getClusterId(),
                VdcObjectType.Cluster,
                getActionType().getActionGroup()));
        for (DiskImage disk : getParameters().getDiskInfoDestinationMap().values()) {
            if (disk.getStorageIds() != null && !disk.getStorageIds().isEmpty()) {
                permissionList.add(new PermissionSubject(disk.getStorageIds().get(0),
                        VdcObjectType.Storage, ActionGroup.CREATE_DISK));
            }
        }
        addPermissionSubjectForAdminLevelProperties(permissionList);
        return permissionList;
    }

    @Override
    protected List<? extends VmNic> getVmInterfaces() {
        if (_vmInterfaces == null) {
            _vmInterfaces = getVmFromConfiguration().getInterfaces();
        }
        return _vmInterfaces;
    }

    @Override
    protected void addVmNetwork() {
        VnicProfileHelper vnicProfileHelper =
                new VnicProfileHelper(getClusterId(),
                        getStoragePoolId(),
                        AuditLogType.ADD_VM_FROM_SNAPSHOT_INVALID_INTERFACES);

        for (VmNetworkInterface iface : getVmFromConfiguration().getInterfaces()) {
            vnicProfileHelper.updateNicWithVnicProfileForUser(iface, getCurrentUser());
        }

        vnicProfileHelper.auditInvalidInterfaces(getVmName());
        super.addVmNetwork();
    }

    @Override
    protected void addVmImages() {
        int numberOfStartedCopyTasks = 0;
        List<DiskImage> cinderDisks = new ArrayList<>();
        List<DiskImage> managedBlockDisks = new ArrayList<>();
        try {
            if (!getSourceDisks().isEmpty()) {
                lockEntities();
                for (DiskImage diskImage : getSourceDisks()) {
                    // For illegal image check if it was snapshot as illegal (therefore
                    // still exists at DB, or was it erased after snapshot - therefore the
                    // query returned to UI an illegal image)
                    if (diskImage.getImageStatus() == ImageStatus.ILLEGAL) {
                        DiskImage snapshotImageInDb =
                                diskImageDao.getSnapshotById(diskImage.getImageId());
                        if (snapshotImageInDb == null) {
                            // If the snapshot diskImage is null, it means the disk was probably
                            // erased after the snapshot was created.
                            // Create a disk to reflect the fact the disk existed during snapshot
                            saveIllegalDisk(diskImage);
                        }
                    } else {// Only legal images can be copied
                        switch (diskImage.getDiskStorageType()) {
                            case CINDER:
                                CinderDisk cinder = (CinderDisk) diskImage;
                                cinder.setVmSnapshotId(getVmSnapshotId());
                                cinderDisks.add(cinder);
                                break;
                            case MANAGED_BLOCK_STORAGE:
                                ManagedBlockStorageDisk managedBlockDisk = (ManagedBlockStorageDisk) diskImage;
                                managedBlockDisk.setVmSnapshotId(getVmSnapshotId());
                                managedBlockDisks.add(managedBlockDisk);
                                break;
                            default:
                                copyDiskImage(diskImage,
                                        diskImage.getStorageIds().get(0),
                                        getDestStorageDomain(diskImage.getId()),
                                        diskInfoDestinationMap.get(diskImage.getId()).getDiskProfileId(),
                                        getActionType());
                                numberOfStartedCopyTasks++;
                                break;
                        }
                    }
                }
                addVmCinderDisks(cinderDisks);
                addManagedBlockDisks(managedBlockDisks);
            }
        } finally {
            // If no tasks were created, endAction will not be called, but
            // it is still needed to unlock the entities
            if ((numberOfStartedCopyTasks == 0) && cinderDisks.isEmpty() && managedBlockDisks.isEmpty()) {
                unlockEntities();
            }
        }
    }

    private void saveIllegalDisk(final DiskImage diskImage) {
        TransactionSupport.executeInNewTransaction(() -> {
            // Allocating new IDs for image and disk as it's possible
            // that more than one clone will be made from this source
            // So this is required to avoid PK violation at DB.
            diskImage.setImageId(Guid.newGuid());
            diskImage.setId(Guid.newGuid());
            diskImage.setParentId(Guid.Empty);
            diskImage.setImageTemplateId(Guid.Empty);

            ImagesHandler.setDiskAlias(diskImage, getVm());
            imagesHandler.addDiskImage(diskImage, getVmId());
            return null;
        });
    }

    @Override
    protected void copyVmDevices() {
        List<VmDevice> devices = new ArrayList<>(getVmFromConfiguration().getUnmanagedDeviceList());
        devices.addAll(getVmFromConfiguration().getManagedVmDeviceMap().values());
        getVmDeviceUtils().copyVmDevices(getSourceVmId(),
                getVmId(),
                getSourceVmFromDb().getStaticData(),
                getVm().getStaticData(),
                devices,
                getSrcDeviceIdToTargetDeviceIdMapping(),
                isSoundDeviceEnabled(),
                getParameters().isTpmEnabled(),
                getParameters().isConsoleEnabled(),
                isVirtioScsiEnabled(),
                getParameters().getGraphicsDevices().keySet(),
                false,
                getVmDeviceUtils().canCopyHostDevices(getSourceVmId(), getVm().getStaticData()),
                getEffectiveCompatibilityVersion());
        getVmDeviceUtils().copyVmExternalData(getSourceVmId(), getVmId());
    }

    @Override
    protected boolean isDisksVolumeFormatValid() {
        return true;
    }

    protected abstract VM getVmFromConfiguration();

    protected Guid getDestStorageDomain(Guid diskImageID){
        return diskInfoDestinationMap.get(diskImageID).getStorageIds().get(0);
    }

    @Override
    protected abstract Guid getSourceVmId();

    @Override
    protected ActionType getChildActionType() {
        return ActionType.CopyImageGroup;
    }

    protected abstract VM getSourceVmFromDb();

    protected void unlockEntities() {

    }
    protected void lockEntities() {

    }
}
