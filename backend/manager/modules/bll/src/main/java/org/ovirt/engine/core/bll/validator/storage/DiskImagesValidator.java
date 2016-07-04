package org.ovirt.engine.core.bll.validator.storage;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.ovirt.engine.core.bll.Backend;
import org.ovirt.engine.core.bll.ValidationResult;
import org.ovirt.engine.core.common.businessentities.Snapshot;
import org.ovirt.engine.core.common.businessentities.StorageDomain;
import org.ovirt.engine.core.common.businessentities.StorageDomainStatus;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VmDevice;
import org.ovirt.engine.core.common.businessentities.storage.DiskImage;
import org.ovirt.engine.core.common.businessentities.storage.ImageStatus;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.common.vdscommands.GetImagesListVDSCommandParameters;
import org.ovirt.engine.core.common.vdscommands.VDSCommandType;
import org.ovirt.engine.core.common.vdscommands.VDSReturnValue;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;
import org.ovirt.engine.core.dao.DiskImageDao;
import org.ovirt.engine.core.dao.SnapshotDao;
import org.ovirt.engine.core.dao.StorageDomainStaticDao;
import org.ovirt.engine.core.dao.VmDao;
import org.ovirt.engine.core.dao.VmDeviceDao;

/**
 * A validator for the {@link DiskImage} class. Since most usecases require validations of multiple {@link DiskImage}s
 * (e.g., all the disks belonging to a VM/template), this class works on a {@link Collection} of {@link DiskImage}s.
 *
 */
public class DiskImagesValidator {

    private Iterable<DiskImage> diskImages;

    public DiskImagesValidator(Iterable<DiskImage> disks) {
        this.diskImages = disks;
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

    protected DiskImage getExistingDisk(Guid id) {
        return getDbFacade().getDiskImageDao().get(id);
    }

    protected boolean isDiskExists(Guid id) {
        return DbFacade.getInstance().getBaseDiskDao().exists(id);
    }

    /**
     * Validates that non of the disks exists
     *
     * @return A {@link ValidationResult} with the validation information.
     */
    public ValidationResult diskImagesAlreadyExist() {

        List<String> existingDisksAliases = new ArrayList<>();
        for (DiskImage diskImage : diskImages) {
            DiskImage existingDisk = getExistingDisk(diskImage.getId());
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
     * Validates that the disks exists
     *
     * @return A {@link ValidationResult} with the validation information.
     */
    public ValidationResult diskImagesNotExist() {
        List<String> disksNotExistInDbIds = new ArrayList<>();
        for (DiskImage diskImage : diskImages) {
            if (!isDiskExists(diskImage.getId())) {
                disksNotExistInDbIds.add(diskImage.getId().toString());
            }
        }

        if (!disksNotExistInDbIds.isEmpty()) {
            return new ValidationResult(EngineMessage.ACTION_TYPE_FAILED_DISKS_NOT_EXIST,
                    String.format("$diskIds %s", StringUtils.join(disksNotExistInDbIds, ", ")));
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
               if (device.getSnapshotId() != null && (!onlyPlugged || device.getIsPlugged())) {
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
     * @param disksList list of the disks for check
     * @param imageToDestinationDomainMap map containing the destination domain for each of the disks
     * @return validation result indicating whether the disks don't exist on the target storage domains
     */
    public ValidationResult diskImagesOnStorage(Map<Guid, Guid> imageToDestinationDomainMap, Guid storagePoolId) {
        Map<Guid, List<Guid>> domainImages = new HashMap<>();
        for (DiskImage diskImage : diskImages) {
            Guid targetStorageDomainId = imageToDestinationDomainMap.get(diskImage.getId());
            List<Guid> imagesOnStorageDomain = domainImages.get(targetStorageDomainId);

            if (imagesOnStorageDomain == null) {
                VDSReturnValue returnValue = Backend.getInstance().getResourceManager().runVdsCommand(
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

    /**
     * Checks that each of the disks has at least one domain in valid status in the given map
     * @param validDomainsForDisk Map containing valid domains for each disk
     * @param storageDomains Map containing the storage domain objects
     * @param message Validation message to use in case of error
     * @param applicableStatuses Applicable domain statuses to use as replacement in the given message
     * @return A {@link ValidationResult} with the validation information.
     */
    public ValidationResult diskImagesOnAnyApplicableDomains(Map<Guid, Set<Guid>> validDomainsForDisk,
            Map<Guid, StorageDomain> storageDomains,
            EngineMessage message,
            Set<StorageDomainStatus> applicableStatuses) {

        StringBuilder disksInfo = new StringBuilder();
        for (DiskImage diskImage : diskImages) {
            Set<Guid> applicableDomains = validDomainsForDisk.get(diskImage.getId());
            if (!applicableDomains.isEmpty()) {
                continue;
            }

            List<String> nonApplicableStorageInfo = new LinkedList<>();
            for (Guid id : diskImage.getStorageIds()) {
                StorageDomain domain = storageDomains.get(id);
                nonApplicableStorageInfo.add(String.format("%s - %s", domain.getName(), domain.getStatus()
                        .toString()));
            }

            disksInfo.append(String.format("%s (%s) %n",
                    diskImage.getDiskAlias(),
                    StringUtils.join(nonApplicableStorageInfo, " / ")));
        }

        ValidationResult result = ValidationResult.VALID;

        if (disksInfo.length() > 0) {
            result = new ValidationResult(message,
                    String.format("$disksInfo %s",
                            disksInfo.toString()),
                            String.format("$applicableStatus %s", StringUtils.join(applicableStatuses, ",")));
        }

        return result;
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

    private DbFacade getDbFacade() {
       return DbFacade.getInstance();
    }

    protected VmDeviceDao getVmDeviceDao() {
       return getDbFacade().getVmDeviceDao();
    }

    protected VmDao getVmDao() {
        return getDbFacade().getVmDao();
    }

    protected SnapshotDao getSnapshotDao() {
        return getDbFacade().getSnapshotDao();
    }

    protected DiskImageDao getDiskImageDao() {
        return getDbFacade().getDiskImageDao();
    }

    protected StorageDomainStaticDao getStorageDomainStaticDao() {
        return getDbFacade().getStorageDomainStaticDao();
    }
}
