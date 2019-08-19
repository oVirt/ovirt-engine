package org.ovirt.engine.core.bll.validator.storage;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import org.ovirt.engine.core.bll.ValidationResult;
import org.ovirt.engine.core.bll.VmHandler;
import org.ovirt.engine.core.bll.interfaces.BackendInternal;
import org.ovirt.engine.core.bll.storage.disk.image.DisksFilter;
import org.ovirt.engine.core.bll.storage.disk.image.ImagesHandler;
import org.ovirt.engine.core.bll.storage.utils.BlockStorageDiscardFunctionalityHelper;
import org.ovirt.engine.core.common.action.ActionType;
import org.ovirt.engine.core.common.businessentities.StorageDomain;
import org.ovirt.engine.core.common.businessentities.StorageDomainDynamic;
import org.ovirt.engine.core.common.businessentities.StorageDomainStatic;
import org.ovirt.engine.core.common.businessentities.StorageDomainStatus;
import org.ovirt.engine.core.common.businessentities.StorageDomainType;
import org.ovirt.engine.core.common.businessentities.StorageFormatType;
import org.ovirt.engine.core.common.businessentities.StoragePoolIsoMap;
import org.ovirt.engine.core.common.businessentities.SubchainInfo;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VMStatus;
import org.ovirt.engine.core.common.businessentities.VmBase;
import org.ovirt.engine.core.common.businessentities.VmDynamic;
import org.ovirt.engine.core.common.businessentities.storage.DiskImage;
import org.ovirt.engine.core.common.businessentities.storage.LUNs;
import org.ovirt.engine.core.common.businessentities.storage.StorageType;
import org.ovirt.engine.core.common.businessentities.storage.VolumeFormat;
import org.ovirt.engine.core.common.businessentities.storage.VolumeType;
import org.ovirt.engine.core.common.constants.StorageConstants;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.common.queries.QueryReturnValue;
import org.ovirt.engine.core.common.queries.QueryType;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.VmDao;
import org.ovirt.engine.core.dao.VmDynamicDao;
import org.ovirt.engine.core.di.Injector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class StorageDomainValidator {
    private static final long INITIAL_BLOCK_ALLOCATION_SIZE = 1024L * 1024L * 1024L;
    private static final long EMPTY_QCOW_HEADER_SIZE = 1024L * 1024L;

    private final Logger log = LoggerFactory.getLogger(StorageDomainValidator.class);
    protected final StorageDomain storageDomain;

    public StorageDomainValidator(StorageDomain domain) {
        storageDomain = domain;
    }

    public ValidationResult isDomainExist() {
        if (storageDomain == null) {
            return new ValidationResult(EngineMessage.ACTION_TYPE_FAILED_STORAGE_DOMAIN_NOT_EXIST);
        }
        return ValidationResult.VALID;
    }

    public ValidationResult isNotBackupDomain() {
        if (storageDomain.isBackup()) {
            return new ValidationResult(EngineMessage.ACTION_TYPE_FAILED_VM_DISKS_ON_BACKUP_STORAGE);
        }
        return ValidationResult.VALID;
    }

    public ValidationResult isNotIsoOrExportForBackup() {
        if (storageDomain.getStorageDomainType().isIsoOrImportExportDomain()
                && storageDomain.getStorageStaticData().isBackup()) {
            return new ValidationResult(EngineMessage.ACTION_TYPE_FAILED_DOMAIN_TYPE_DOES_NOT_SUPPORT_BACKUP);
        }
        return ValidationResult.VALID;
    }

    public ValidationResult isDomainExistAndActive() {
        ValidationResult domainExistValidation = isDomainExist();
        if (!ValidationResult.VALID.equals(domainExistValidation)) {
            return domainExistValidation;
        }
        if (storageDomain.getStatus() != StorageDomainStatus.Active) {
            return new ValidationResult(EngineMessage.ACTION_TYPE_FAILED_STORAGE_DOMAIN_STATUS_ILLEGAL2,
                    String.format("$%1$s %2$s", "status", storageDomain.getStatus().name()));
        }
        return ValidationResult.VALID;
    }

    public ValidationResult domainIsValidDestination() {
        if (storageDomain.getStorageDomainType().isIsoOrImportExportDomain()) {
            return new ValidationResult(EngineMessage.ACTION_TYPE_FAILED_STORAGE_DOMAIN_TYPE_ILLEGAL);
        }
        return ValidationResult.VALID;
    }

    public ValidationResult isDomainWithinThresholds() {
        if (storageDomain.getStorageType().isCinderDomain()) {
            return ValidationResult.VALID;
        }
        StorageDomainDynamic dynamicData = storageDomain.getStorageDynamicData();
        StorageDomainStatic staticData = storageDomain.getStorageStaticData();
        if (dynamicData != null && staticData != null
                && dynamicData.getAvailableDiskSize() != null
                && staticData.getCriticalSpaceActionBlocker() != null
                && dynamicData.getAvailableDiskSize() < staticData.getCriticalSpaceActionBlocker()) {
            return new ValidationResult(EngineMessage.ACTION_TYPE_FAILED_DISK_SPACE_LOW_ON_STORAGE_DOMAIN,
                    storageName());
        }
        return ValidationResult.VALID;
    }

    private String storageName() {
        return String.format("$%1$s %2$s", "storageName", storageDomain.getStorageName());
    }

    /**
     * Verify there's enough space in the storage domain for creating new DiskImages.
     * Some space should be allocated on the storage domain according to the volumes type and format, and allocation policy,
     * according to the following table:
     *
     *      | File Domain                             | Block Domain
     * -----|-----------------------------------------|-------------
     * qcow | 1M (header size)                        | 1G
     * -----|-----------------------------------------|-------------
     * raw  | preallocated: disk capacity (getSize()) | disk capacity
     *      | thin (sparse): 1M                       | (there is no raw sparse on
     *      |                                         | block domains)
     *
     */
    private double getTotalSizeForNewDisks(Collection<DiskImage> diskImages) {
        return getTotalSizeForDisksByMethod(diskImages, diskImage -> {
            double sizeForDisk = diskImage.getSize();
            if (diskImage.getVolumeFormat() == VolumeFormat.COW) {
                if (storageDomain.getStorageType().isFileDomain()) {
                    sizeForDisk = EMPTY_QCOW_HEADER_SIZE;
                } else {
                    sizeForDisk = INITIAL_BLOCK_ALLOCATION_SIZE;
                }
            } else if (diskImage.getVolumeType() == VolumeType.Sparse) {
                sizeForDisk = EMPTY_QCOW_HEADER_SIZE;
            }
            return sizeForDisk;
        });
    }

    /**
     * Returns the required space in the storage domain for creating cloned DiskImages with collapse.
     * */
    private double getTotalSizeForClonedDisks(Collection<DiskImage> diskImages) {
        return getTotalSizeForDisksByMethod(diskImages, this::getTotalSizeForClonedDisk);
    }

    /**
     * Calculates the required space in the storage domain for creating cloned DiskImages with collapse.
     * When creating COW volume the actual used space will be the needed space * QCOW_OVERHEAD_FACTOR as implemented
     * currently in the VDSM code.
     *
     * */
    private double getTotalSizeForClonedDisk(DiskImage diskImage) {
        double sizeForDisk = ImagesHandler.getTotalActualSizeOfDisk(diskImage, storageDomain.getStorageStaticData());

        if (diskImage.getVolumeFormat() == VolumeFormat.COW) {
            sizeForDisk = Math.ceil(StorageConstants.QCOW_OVERHEAD_FACTOR * sizeForDisk);
        }
        return sizeForDisk;
    }

    /**
     * Verify there's enough space in the storage domain for creating cloned DiskImages with snapshots without collapse.
     * Space should be allocated according to the volumes type and format, and allocation policy,
     * according to the following table:
     *
     *      | File Domain                             | Block Domain
     * -----|-----------------------------------------|----------------
     * qcow | 1.1 * used space                        |1.1 * used space
     * -----|-----------------------------------------|----------------
     * raw  | preallocated: disk capacity             |disk capacity
     *      | sparse: used space                      |
     *
     * */
    private double getTotalSizeForDisksWithSnapshots(Collection<DiskImage> diskImages) {
        return getTotalSizeForDisksByMethod(diskImages, diskImage -> {
            double sizeForDisk = diskImage.getSize();
            if ((storageDomain.getStorageType().isFileDomain() && diskImage.getVolumeType() == VolumeType.Sparse)
                || diskImage.getVolumeFormat() == VolumeFormat.COW) {
                sizeForDisk = diskImage.getActualDiskWithSnapshotsSizeInBytes();
            }

            if (diskImage.getVolumeFormat() == VolumeFormat.COW) {
                sizeForDisk = Math.ceil(StorageConstants.QCOW_OVERHEAD_FACTOR * sizeForDisk);
            }
            return sizeForDisk;
        });
    }

    private double getTotalSizeForMerge(Collection<SubchainInfo> subchains, ActionType actionType) {
        return subchains
                .stream()
                .mapToDouble(subchain -> getRequiredSizeForMerge(subchain, actionType))
                .sum();
    }

    /**
     * Validate space for new, empty disks. Used for a new Active Image.
     */
    public ValidationResult hasSpaceForNewDisks(Collection<DiskImage> diskImages) {
        if (storageDomain.getStorageType().isCinderDomain()) {
            return ValidationResult.VALID;
        }
        Long availableSize = storageDomain.getAvailableDiskSizeInBytes();
        double totalSizeForDisks = getTotalSizeForNewDisks(diskImages);

        return validateRequiredSpace(availableSize, totalSizeForDisks);
    }

    /**
     * Validate space for a cloned disk with the collapse option.
     */
    public ValidationResult hasSpaceForClonedDisks(Collection<DiskImage> diskImages) {
        if (storageDomain.getStorageType().isCinderDomain()) {
            return ValidationResult.VALID;
        }
        Long availableSize = storageDomain.getAvailableDiskSizeInBytes();
        double totalSizeForDisks = getTotalSizeForClonedDisks(diskImages);

        return validateRequiredSpace(availableSize, totalSizeForDisks);
    }

    public ValidationResult hasSpaceForMerge(List<SubchainInfo> subchains, ActionType snapshotActionType) {
        if (storageDomain.getStorageType().isCinderDomain() || storageDomain.getStorageType().isManagedBlockStorage()) {
            return ValidationResult.VALID;
        }
        Long availableSize = storageDomain.getAvailableDiskSizeInBytes();
        double totalSizeForDisks = getTotalSizeForMerge(subchains, snapshotActionType);

        return validateRequiredSpace(availableSize, totalSizeForDisks);
    }

    /**
     * Validate space for cloned disks without the collapse option. Every snapshot will be cloned.
     */
    public ValidationResult hasSpaceForDisksWithSnapshots(Collection<DiskImage> diskImages) {
        if (storageDomain.getStorageType().isCinderDomain()) {
            return ValidationResult.VALID;
        }
        Long availableSize = storageDomain.getAvailableDiskSizeInBytes();
        double totalSizeForDisks = getTotalSizeForDisksWithSnapshots(diskImages);

        return validateRequiredSpace(availableSize, totalSizeForDisks);
    }

    /**
     * Validate space for new and cloned (with collapse) disks. When this option is needed a combined method should be
     * used in order to check the space on the domain for the two types of validation, done here.
     * Note that at this time there is no need for the same functionality for clone without collapse,
     * so there's no method for this.
     */
    public ValidationResult hasSpaceForAllDisks(Collection<DiskImage> newDiskImages, Collection<DiskImage> clonedDiskImages) {
        if (storageDomain.getStorageType().isCinderDomain()) {
            return ValidationResult.VALID;
        }
        Long availableSize = storageDomain.getAvailableDiskSizeInBytes();
        double totalSizeForNewDisks = getTotalSizeForNewDisks(newDiskImages);
        double totalSizeForClonedDisks = getTotalSizeForClonedDisks(clonedDiskImages);
        double totalSizeForDisks = totalSizeForNewDisks + totalSizeForClonedDisks;

        return validateRequiredSpace(availableSize, totalSizeForDisks);
    }

    /**
     * Validate space for a cloned disk without the collapse option. Every snapshot will be cloned.
     */
    public ValidationResult hasSpaceForDiskWithSnapshots(DiskImage diskImage) {
        return hasSpaceForDisksWithSnapshots(Collections.singleton(diskImage));
    }

    /**
     * Validate space for a cloned disk with the collapse option.
     */
    public ValidationResult hasSpaceForClonedDisk(DiskImage diskImage) {
        return hasSpaceForClonedDisks(Collections.singleton(diskImage));
    }

    /**
     * Validate space for a new, empty disk. Used for a new Active Image.
     */
    public ValidationResult hasSpaceForNewDisk(DiskImage diskImage) {
        return hasSpaceForNewDisks(Collections.singleton(diskImage));
    }

    private ValidationResult validateRequiredSpace(Long availableSize, double requiredSize) {
        // If availableSize is not yet set, we'll allow the operation.
        if (availableSize == null || availableSize.doubleValue() >= requiredSize) {
            return ValidationResult.VALID;
        }

        return new ValidationResult(EngineMessage.ACTION_TYPE_FAILED_DISK_SPACE_LOW_ON_STORAGE_DOMAIN,
                storageName());
    }

    /**
     * Validates all the storage domains by a given predicate.
     *
     * @return {@link ValidationResult#VALID} if all the domains are OK, or the
     * first validation error if they aren't.
     */
    private double getTotalSizeForDisksByMethod(Collection<DiskImage> diskImages, SizeAssessment sizeAssessment) {
        double totalSizeForDisks = 0.0;
        if (diskImages != null) {
            for (DiskImage diskImage : diskImages) {
                double sizeForDisk = sizeAssessment.getSizeForDisk(diskImage);
                totalSizeForDisks += sizeForDisk;
            }
        }
        return totalSizeForDisks;
    }

    /**
     * Calculates the required space for snpashot merge (live and cold).
     * The calculation is performed as follows:
     *
     *
     *      | File Domain                                                           | Block Domain
     * -----|-----------------------------------------------------------------------|--------------
     * qcow | min(virtual_size(top) * 1.1 - actual_size(base), actual_size(top))    | same as file
     * -----|-----------------------------------------------------------------------|--------------
     * raw  | min(virtual_size(top) / 1.1, virtual_size(base) - actual_size(base))  | 0
     *      |                                                                       |
     *
     *  * The qcow/raw refers to the base snapshot format
     *  * base/top - base snapshot/ top snapshot
     *  * 1.1 - qcow2 overhead
     *
     * @param subchain - The snapshot subchain, containing base and top snapshots
     * @param snapshotActionType - Type of the merge operation (cold/live)
     * @return required size for merge
     */
    private double getRequiredSizeForMerge(SubchainInfo subchain, ActionType snapshotActionType) {
        DiskImage baseSnapshot = subchain.getBaseImage();
        DiskImage topSnapshot = subchain.getTopImage();

        // The snapshot is the root snapshot
        if (Guid.isNullOrEmpty(baseSnapshot.getParentId())) {
            if (baseSnapshot.getVolumeFormat() == VolumeFormat.RAW) {
                // Raw/Block can only be preallocated thus we are necessarily overlapping
                // with existing data
                if (baseSnapshot.getVolumeType() == VolumeType.Preallocated) {
                    return 0.0;
                }

                return Math.min(topSnapshot.getActualSizeInBytes() / StorageConstants.QCOW_OVERHEAD_FACTOR,
                        baseSnapshot.getSize() - baseSnapshot.getActualSizeInBytes());
            }
        }

        // The required size for the extension of the volume we merge into.
        // If the actual size of top is larger than the actual size of base we
        // will be overlapping, hence we extend by the lower of the two.
        return Math.min(topSnapshot.getSize() * StorageConstants.QCOW_OVERHEAD_FACTOR - baseSnapshot.getActualSizeInBytes(),
                topSnapshot.getActualSizeInBytes());

    }

    @FunctionalInterface
    private static interface SizeAssessment {
        public double getSizeForDisk(DiskImage diskImage);
    }

    public ValidationResult isInProcess() {
        StoragePoolIsoMap domainIsoMap = storageDomain.getStoragePoolIsoMapData();

        if (domainIsoMap.getStatus() != null && domainIsoMap.getStatus().isStorageDomainInProcess()) {
            return new ValidationResult(EngineMessage.ACTION_TYPE_FAILED_STORAGE_DOMAIN_STATUS_ILLEGAL2,
                    String.format("$status %1$s", domainIsoMap.getStatus()));
        }
        return ValidationResult.VALID;
    }

    public ValidationResult isStorageFormatCompatibleWithDomain() {
        StorageFormatType storageFormat = storageDomain.getStorageFormat();
        StorageType storageType = storageDomain.getStorageType();
        StorageDomainType storageDomainFunction = storageDomain.getStorageDomainType();
        boolean validationSucceeded = true;

        if (storageFormat == null) {
            validationSucceeded = false;
        }
        // V2 is applicable only for block data storage domains
        if (validationSucceeded && storageFormat == StorageFormatType.V2) {
            if ( !(storageDomainFunction.isDataDomain() && storageType.isBlockDomain()) ) {
                validationSucceeded = false;
            }
        }
        if (validationSucceeded && storageFormat.compareTo(StorageFormatType.V3) >= 0) {
            // Above V3 is applicable only for data storage domains
            if (!storageDomainFunction.isDataDomain()) {
                validationSucceeded = false;
            }
        }

        return validationSucceeded? ValidationResult.VALID : new ValidationResult(EngineMessage.ACTION_TYPE_FAILED_STORAGE_DOMAIN_FORMAT_ILLEGAL_HOST,
                    String.format("$storageFormat %1$s", storageDomain.getStorageFormat()));
    }

    public ValidationResult isDataDomain() {
        if (storageDomain.getStorageDomainType().isDataDomain()) {
            return ValidationResult.VALID;
        }
        return new ValidationResult(EngineMessage.ACTION_TYPE_FAILED_ACTION_IS_SUPPORTED_ONLY_FOR_DATA_DOMAINS);
    }

    public ValidationResult isDiscardAfterDeleteLegalForExistingStorageDomain() {
        return isDiscardAfterDeleteLegal(this::discardAfterDeleteLegalForExistingStorageDomainPredicate);
    }

    protected Boolean discardAfterDeleteLegalForExistingStorageDomainPredicate() {
        return Boolean.TRUE.equals(storageDomain.getSupportsDiscard());
    }

    public ValidationResult isDiscardAfterDeleteLegalForNewBlockStorageDomain(Collection<LUNs> luns) {
        return isDiscardAfterDeleteLegal(getDiscardAfterDeleteLegalForNewBlockStorageDomainPredicate(luns));
    }

    protected Supplier<Boolean> getDiscardAfterDeleteLegalForNewBlockStorageDomainPredicate(Collection<LUNs> luns) {
        return () -> Injector.get(BlockStorageDiscardFunctionalityHelper.class).allLunsSupportDiscard(luns);
    }

    protected ValidationResult isDiscardAfterDeleteLegal(Supplier<Boolean> supportsDiscardSupplier) {
        if (!storageDomain.getDiscardAfterDelete()) {
            return ValidationResult.VALID;
        }

        if (storageDomain.getStorageType().isBlockDomain()) {
            if (supportsDiscardSupplier.get()) {
                return ValidationResult.VALID;
            }
            return new ValidationResult(
                    EngineMessage.ACTION_TYPE_FAILED_DISCARD_AFTER_DELETE_NOT_SUPPORTED_BY_UNDERLYING_STORAGE,
                    String.format("$storageDomainName %s", storageDomain.getName()));
        }
        return new ValidationResult(
                EngineMessage.ACTION_TYPE_FAILED_DISCARD_AFTER_DELETE_SUPPORTED_ONLY_BY_BLOCK_DOMAINS);
    }

    public ValidationResult isRunningVmsOrVmLeasesForBackupDomain(VmHandler vmHandler) {
        Set<String> invalidVmsForBackupStorageDomain = new HashSet<>();
        QueryReturnValue ret = getEntitiesWithLeaseIdForStorageDomain(storageDomain.getId());
        if (!ret.getSucceeded()) {
            return new ValidationResult(EngineMessage.ACTION_TYPE_FAILED_RETRIEVE_VMS_FOR_WITH_LEASES);
        }
        getRetVal(ret).forEach(vmBase -> {
            VmDynamic vm = getVmDynamicDao().get(vmBase.getId());
            if (vm != null && vm.getStatus() != VMStatus.Down) {
                invalidVmsForBackupStorageDomain.add(vmBase.getName());
            }
        });
        List<VM> vms = getVmDao().getAllActiveForStorageDomain(storageDomain.getId());
        vms.forEach(vmHandler::updateDisksFromDb);
        invalidVmsForBackupStorageDomain.addAll(vms.stream()
                        .filter(vm -> vm.getDiskMap()
                                .values()
                                .stream()
                                .filter(DisksFilter.ONLY_IMAGES)
                                .filter(DisksFilter.ONLY_PLUGGED)
                                .map(DiskImage.class::cast)
                                .anyMatch(vmDisk -> vmDisk.getStorageIds().get(0).equals(storageDomain.getId())))
                        .map(VM::getName)
                        .collect(Collectors.toList()));
        if (!invalidVmsForBackupStorageDomain.isEmpty()) {
            log.warn("Can't update the backup property of the storage domain since it contains VMs with " +
                    "leases or active disks which are attached to running VMs." +
                    "The following VMs list are: '{}'",
                    invalidVmsForBackupStorageDomain);
            return new ValidationResult(
                    EngineMessage.ACTION_TYPE_FAILED_RUNNING_VM_OR_VM_LEASES_PRESENT_ON_STORAGE_DOMAIN);
        }
        return ValidationResult.VALID;
    }

    public boolean isManagedBlockStorage() {
        return storageDomain.getStorageType().isManagedBlockStorage();
    }

    private List<VmBase> getRetVal(QueryReturnValue ret) {
        return ret.<List<VmBase>>getReturnValue();
    }

    protected QueryReturnValue getEntitiesWithLeaseIdForStorageDomain(Guid storageDomainId) {
        return getBackend().runInternalQuery(
                    QueryType.GetEntitiesWithLeaseByStorageId,
                    new IdQueryParameters(storageDomainId));
    }

    private BackendInternal getBackend() {
        return Injector.get(BackendInternal.class);
    }

    protected VmDao getVmDao() {
        return Injector.get(VmDao.class);
    }

    protected VmDynamicDao getVmDynamicDao() {
        return Injector.get(VmDynamicDao.class);
    }
}
