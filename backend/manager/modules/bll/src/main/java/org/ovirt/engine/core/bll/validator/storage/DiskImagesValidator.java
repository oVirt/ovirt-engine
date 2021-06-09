package org.ovirt.engine.core.bll.validator.storage;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.apache.commons.lang.StringUtils;
import org.ovirt.engine.core.bll.ValidationResult;
import org.ovirt.engine.core.common.businessentities.Snapshot;
import org.ovirt.engine.core.common.businessentities.StorageFormatType;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VmDevice;
import org.ovirt.engine.core.common.businessentities.storage.Disk;
import org.ovirt.engine.core.common.businessentities.storage.DiskBackup;
import org.ovirt.engine.core.common.businessentities.storage.DiskImage;
import org.ovirt.engine.core.common.businessentities.storage.ImageStatus;
import org.ovirt.engine.core.common.businessentities.storage.QcowCompat;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.common.interfaces.VDSBrokerFrontend;
import org.ovirt.engine.core.common.vdscommands.GetImagesListVDSCommandParameters;
import org.ovirt.engine.core.common.vdscommands.VDSCommandType;
import org.ovirt.engine.core.common.vdscommands.VDSReturnValue;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.DiskDao;
import org.ovirt.engine.core.dao.DiskImageDao;
import org.ovirt.engine.core.dao.SnapshotDao;
import org.ovirt.engine.core.dao.StorageDomainStaticDao;
import org.ovirt.engine.core.dao.StoragePoolDao;
import org.ovirt.engine.core.dao.VmDao;
import org.ovirt.engine.core.dao.VmDeviceDao;
import org.ovirt.engine.core.di.Injector;
import org.ovirt.engine.core.utils.ReplacementUtils;

/**
 * A validator for the {@link DiskImage} class. Since most usecases require validations of multiple {@link DiskImage}s
 * (e.g., all the disks belonging to a VM/template), this class works on a {@link Collection} of {@link DiskImage}s.
 *
 */
public class DiskImagesValidator {

    private Collection<DiskImage> diskImages;

    public DiskImagesValidator(Collection<DiskImage> disks) {
        this.diskImages = disks;
    }

    public DiskImagesValidator(DiskImage... disks) {
        this.diskImages = Arrays.asList(disks);
    }

    /**
     * Validates that non of the disk are {@link ImageStatus#ILLEGAL}.
     *
     * @return A {@link ValidationResult} with the validation information.
     */
    public ValidationResult diskImagesNotIllegal() {
        return diskImagesNotInStatus(ImageStatus.ILLEGAL, EngineMessage.ACTION_TYPE_FAILED_DISKS_ILLEGAL);
    }

    /**
     * Validates that non of the disk are {@link ImageStatus#LOCKED}.
     *
     * @return A {@link ValidationResult} with the validation information.
     */
    public ValidationResult diskImagesNotLocked() {
        return diskImagesNotInStatus(ImageStatus.LOCKED, EngineMessage.ACTION_TYPE_FAILED_DISKS_LOCKED);
    }

    protected Disk getExistingDisk(Guid id) {
        return getDiskDao().get(id);
    }

    /**
     * Validates that non of the disks exists
     *
     * @return A {@link ValidationResult} with the validation information.
     */
    public ValidationResult diskImagesAlreadyExist() {

        List<String> existingDisksAliases = new ArrayList<>();
        for (DiskImage diskImage : diskImages) {
            Disk existingDisk = getExistingDisk(diskImage.getId());
            if (existingDisk != null) {
                existingDisksAliases.add(diskImage.getDiskAlias().isEmpty() ? existingDisk.getDiskAlias() : diskImage.getDiskAlias());
            }
        }

        if (!existingDisksAliases.isEmpty()) {
            return new ValidationResult(EngineMessage.ACTION_TYPE_FAILED_IMPORT_DISKS_ALREADY_EXIST,
                    String.format("$diskAliases %s", StringUtils.join(existingDisksAliases, ", ")));
        }

        return ValidationResult.VALID;
    }

    /**
     * Validates that non of the disk are in the given {@link #status}.
     *
     * @param status
     *            The status to check
     * @param failMessage
     *            The validation message to return in case of failure.
     * @return A {@link ValidationResult} with the validation information. If none of the disks are in the given status,
     *         {@link ValidationResult#VALID} is returned. If one or more disks are in that status, a
     *         {@link ValidationResult} with {@link #failMessage} and the names of the disks in that status is returned.
     */
    private ValidationResult diskImagesNotInStatus(ImageStatus status, EngineMessage failMessage) {
        List<String> disksInStatus = new ArrayList<>();
        for (DiskImage diskImage : diskImages) {
            if (diskImage.getImageStatus() == status) {
                disksInStatus.add(diskImage.getDiskAlias());
            }
        }

        if (!disksInStatus.isEmpty()) {
            return new ValidationResult(failMessage,
                    String.format("$diskAliases %s", StringUtils.join(disksInStatus, ", ")));
        }

        return ValidationResult.VALID;
    }

    public ValidationResult diskImagesSnapshotsNotAttachedToOtherVms(boolean onlyPlugged) {
        LinkedList<String> pluggedDiskSnapshotInfo = new LinkedList<>();
        for (DiskImage diskImage : diskImages) {
            List<VmDevice> devices = getVmDeviceDao().getVmDevicesByDeviceId(diskImage.getId(), null);
            for (VmDevice device : devices) {
               if (device.getSnapshotId() != null && (!onlyPlugged || device.isPlugged())) {
                   VM vm = getVmDao().get(device.getVmId());
                   Snapshot snapshot = getSnapshotDao().get(device.getSnapshotId());
                   pluggedDiskSnapshotInfo.add(String.format("%s ,%s, %s",
                           diskImage.getDiskAlias(), snapshot.getDescription(), vm.getName()));
               }
            }
        }

        if (!pluggedDiskSnapshotInfo.isEmpty()) {
            EngineMessage message =
                    onlyPlugged ? EngineMessage.ACTION_TYPE_FAILED_VM_DISK_SNAPSHOT_IS_PLUGGED_TO_ANOTHER_VM
                            : EngineMessage.ACTION_TYPE_FAILED_VM_DISK_SNAPSHOT_IS_ATTACHED_TO_ANOTHER_VM;
            return new ValidationResult(message,
                    String.format("$disksInfo %s", String.format(StringUtils.join(pluggedDiskSnapshotInfo, "%n"))));
        }

        return ValidationResult.VALID;
    }

    public ValidationResult diskImagesSnapshotsAttachedToVm(Guid vmId) {
        LinkedList<String> diskSnapshotInfo = new LinkedList<>();
        VM vm = getVmDao().get(vmId);
        for (DiskImage diskImage : diskImages) {
            List<VmDevice> devices = getVmDeviceDao().getVmDevicesByDeviceId(diskImage.getId(), vmId);
            if (devices.isEmpty()) {
                // The specified disk image does not belong to the vm
                Snapshot snapshot = getSnapshotDao().get(diskImage.getSnapshotId());
                Disk disk = Injector.get(DiskDao.class).get(diskImage.getId());
                diskSnapshotInfo.add(String.format("%s ,%s",
                        disk.getDiskAlias(), snapshot.getDescription()));
            }
        }

        if (!diskSnapshotInfo.isEmpty()) {
            EngineMessage message = EngineMessage.ACTION_TYPE_FAILED_VM_DISK_SNAPSHOT_NOT_ATTACHED_TO_VM;
            return new ValidationResult(message,
                    String.format("$disksInfo %s", String.format(StringUtils.join(diskSnapshotInfo, "%n"))),
                    String.format("$vmName %s", vm.getName()));
        }

        return ValidationResult.VALID;
    }


    /**
     * checks that the given disks has no derived disks on the given storage domain.
     * if the provided storage domain id is null, it will be checked that there are no
     * derived disks on any storage domain.
     */
    public ValidationResult diskImagesHaveNoDerivedDisks(Guid storageDomainId) {
        List<String> disksInfo = new LinkedList<>();
        for (DiskImage diskImage : diskImages) {
            if (diskImage.getVmEntityType() != null && diskImage.getVmEntityType().isTemplateType()) {
                List<DiskImage> basedDisks = getDiskImageDao().getAllSnapshotsForParent(diskImage.getImageId());
                for (DiskImage basedDisk : basedDisks) {
                    if (storageDomainId == null || basedDisk.getStorageIds().contains(storageDomainId)) {
                        disksInfo.add(String.format("%s  (%s) ", basedDisk.getDiskAlias(), basedDisk.getId()));
                    }
                }
            }
        }

        if (!disksInfo.isEmpty()) {
            return new ValidationResult(EngineMessage.ACTION_TYPE_FAILED_DETECTED_DERIVED_DISKS,
                    String.format("$disksInfo %s",
                            String.format(StringUtils.join(disksInfo, "%n"))));
        }

        return ValidationResult.VALID;
    }

    /**
     * checks that the given disks do not exist on the target storage domains
     * @param imageToDestinationDomainMap map containing the destination domain for each of the disks
     * @param storagePoolId the storage pool ID to check whether the disks are residing on
     * @return validation result indicating whether the disks don't exist on the target storage domains
     */
    public ValidationResult diskImagesOnStorage(Map<Guid, Guid> imageToDestinationDomainMap, Guid storagePoolId) {
        Map<Guid, List<Guid>> domainImages = new HashMap<>();
        for (DiskImage diskImage : diskImages) {
            Guid targetStorageDomainId = imageToDestinationDomainMap.get(diskImage.getId());
            List<Guid> imagesOnStorageDomain = domainImages.get(targetStorageDomainId);

            if (imagesOnStorageDomain == null) {
                VDSReturnValue returnValue = Injector.get(VDSBrokerFrontend.class).runVdsCommand(
                        VDSCommandType.GetImagesList,
                        new GetImagesListVDSCommandParameters(targetStorageDomainId, storagePoolId)
                );

                if (returnValue.getSucceeded()) {
                    imagesOnStorageDomain = (List<Guid>) returnValue.getReturnValue();
                    domainImages.put(targetStorageDomainId, imagesOnStorageDomain);
                } else {
                    return new ValidationResult(EngineMessage.ERROR_GET_IMAGE_LIST,
                            String.format("$sdName %1$s", getStorageDomainStaticDao().get(targetStorageDomainId).getName()));
                }
            }

            if (imagesOnStorageDomain.contains(diskImage.getId())) {
                return new ValidationResult(EngineMessage.ACTION_TYPE_FAILED_STORAGE_DOMAIN_ALREADY_CONTAINS_DISK);
            }
        }

        return ValidationResult.VALID;
    }

    public ValidationResult isQcowVersionSupportedForDcVersion() {
        // If storage pool format type is less than V4 and the disk is with QCOW compatibility level of 1.1 (QCOW2_V3)
        // then the engine should fail the operation.
        for (DiskImage diskImage : diskImages) {
            StorageFormatType storagePoolFormatType =
                    getStoragePoolDao().get(diskImage.getStoragePoolId()).getStoragePoolFormatType();
            if (storagePoolFormatType.compareTo(StorageFormatType.V4) < 0
                    && diskImage.getQcowCompat() == QcowCompat.QCOW2_V3) {
                return new ValidationResult(EngineMessage.ACTION_TYPE_FAILED_QCOW_COMPAT_DOES_NOT_MATCH_DC_VERSION);
            }
        }
        return ValidationResult.VALID;
    }

    public ValidationResult disksInStatus(ImageStatus applicableStatus, EngineMessage message) {
        for (DiskImage diskImage : diskImages) {
            if (diskImage.getImageStatus() != applicableStatus) {
                return new ValidationResult(message,
                        String.format("$status %s", applicableStatus.name()));
            }
        }

        return ValidationResult.VALID;
    }

    /**
     * checks that there are no duplicated ids among a stream of ids
     * @return validation result indicating if any disk id was supplied more than once by the user
     */
    public ValidationResult noDuplicatedIds() {
        String duplicated = diskImages.stream()
                .map(DiskImage::getId)
                .collect(Collectors.groupingBy(Function.identity(), Collectors.counting()))
                .entrySet()
                .stream()
                .filter(e -> e.getValue() > 1L)
                .map(e -> e.getKey().toString())
                .collect(Collectors.joining(", "));

        if (!duplicated.isEmpty()) {
            return new ValidationResult(EngineMessage.ACTION_TYPE_FAILED_DUPLICATED_DISK_OR_IMAGE_IDS,
                    String.format("$ids %s", duplicated));
        }
        return ValidationResult.VALID;
    }

    public ValidationResult snapshotAlreadyExists(Map<Guid, DiskImage> diskImagesMap) {
        Set<Guid> diskIds = diskImages.stream()
                .flatMap(diskImage -> getDiskImageDao().getAllSnapshotsForImageGroup(diskImage.getId()).stream())
                .map(DiskImage::getImageId)
                .collect(Collectors.toSet());
        Set<Guid> providedImageIds = diskImagesMap.values().stream().map(DiskImage::getImageId).collect(Collectors.toSet());
        Set<Guid> existingGuids = providedImageIds.stream().filter(diskIds::contains).collect(Collectors.toSet());
        if (!existingGuids.isEmpty()) {
            return new ValidationResult(EngineMessage.ACTION_TYPE_FAILED_VM_SNAPSHOT_IMAGE_ALREADY_EXISTS,
                    ReplacementUtils.replaceWith("ImageIds", existingGuids));
        }

        return ValidationResult.VALID;
    }

    /**
     * Checks that incremental backup is enabled for all disks.
     * @return validation result indicating if incremental backup is disabled in any disk.
     */
    public ValidationResult incrementalBackupEnabled() {
        String backupEnabled = diskImages.stream()
                .filter(d -> d.getBackup() != DiskBackup.Incremental)
                .map(d -> d.getId().toString())
                .collect(Collectors.joining(", "));

        if (!backupEnabled.isEmpty()) {
            return new ValidationResult(EngineMessage.ACTION_TYPE_FAILED_INCREMENTAL_BACKUP_DISABLED_FOR_DISKS,
                    String.format("$ids %s", backupEnabled));
        }
        return ValidationResult.VALID;
    }

    protected VmDeviceDao getVmDeviceDao() {
       return Injector.get(VmDeviceDao.class);
    }

    protected VmDao getVmDao() {
        return Injector.get(VmDao.class);
    }

    protected SnapshotDao getSnapshotDao() {
        return Injector.get(SnapshotDao.class);
    }

    protected DiskImageDao getDiskImageDao() {
        return Injector.get(DiskImageDao.class);
    }

    protected StoragePoolDao getStoragePoolDao() {
        return Injector.get(StoragePoolDao.class);
    }

    protected StorageDomainStaticDao getStorageDomainStaticDao() {
        return Injector.get(StorageDomainStaticDao.class);
    }

    protected DiskDao getDiskDao() {
        return Injector.get(DiskDao.class);
    }
}
