package org.ovirt.engine.core.bll.validator;

import java.util.Collection;
import java.util.Collections;

import org.ovirt.engine.core.bll.ValidationResult;
import org.ovirt.engine.core.common.businessentities.DiskImage;
import org.ovirt.engine.core.common.businessentities.StorageDomain;
import org.ovirt.engine.core.common.businessentities.StorageDomainDynamic;
import org.ovirt.engine.core.common.businessentities.StorageDomainStatus;
import org.ovirt.engine.core.common.businessentities.VolumeFormat;
import org.ovirt.engine.core.common.businessentities.VolumeType;
import org.ovirt.engine.core.common.config.Config;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.common.errors.VdcBllMessages;

public class StorageDomainValidator {

    private static final double QCOW_OVERHEAD_FACTOR = 1.1;
    private static final long INITIAL_BLOCK_ALLOCATION_SIZE = 1024L * 1024L * 1024L;
    private static final long EMPTY_QCOW_HEADER_SIZE = 1024L * 1024L;

    private final StorageDomain storageDomain;

    public StorageDomainValidator(StorageDomain domain) {
        storageDomain = domain;
    }

    public ValidationResult isDomainExist() {
        if (storageDomain == null) {
            return new ValidationResult(VdcBllMessages.ACTION_TYPE_FAILED_STORAGE_DOMAIN_NOT_EXIST);
        }
        return ValidationResult.VALID;
    }

    public ValidationResult isDomainExistAndActive() {
        ValidationResult domainExistValidation = isDomainExist();
        if (!ValidationResult.VALID.equals(domainExistValidation)) {
            return domainExistValidation;
        }
        if (storageDomain.getStatus() != StorageDomainStatus.Active) {
            return new ValidationResult(VdcBllMessages.ACTION_TYPE_FAILED_STORAGE_DOMAIN_STATUS_ILLEGAL2,
                    String.format("$%1$s %2$s", "status", storageDomain.getStatus().name()));
        }
        return ValidationResult.VALID;
    }

    public ValidationResult domainIsValidDestination() {
        if (storageDomain.getStorageDomainType().isIsoOrImportExportDomain()) {
            return new ValidationResult(VdcBllMessages.ACTION_TYPE_FAILED_STORAGE_DOMAIN_TYPE_ILLEGAL);
        }
        return ValidationResult.VALID;
    }

    public ValidationResult isDomainWithinThresholds() {
        StorageDomainDynamic dynamic = storageDomain.getStorageDynamicData();
        if (dynamic != null && dynamic.getfreeDiskInGB() < getLowDiskSpaceThreshold()) {
            return new ValidationResult(VdcBllMessages.ACTION_TYPE_FAILED_DISK_SPACE_LOW_ON_STORAGE_DOMAIN,
                    storageName());
        }
        return ValidationResult.VALID;
    }

    private String storageName() {
        return String.format("$%1$s %2$s", "storageName", storageDomain.getStorageName());
    }

    private static Integer getLowDiskSpaceThreshold() {
        return Config.<Integer> getValue(ConfigValues.FreeSpaceCriticalLowInGB);
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
        return getTotalSizeForDisksByMethod(diskImages, new SizeAssessment() {
            @Override
            public double getSizeForDisk(DiskImage diskImage) {
                double sizeForDisk = diskImage.getCapacity();
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
            }
        });
    }

    /**
     * Verify there's enough space in the storage domain for creating cloned DiskImages.
     * Space should be allocated according to the volumes type and format, and allocation policy,
     * according to the following table:
     *
     *      | File Domain                             | Block Domain
     * -----|-----------------------------------------|-------------
     * qcow | preallocated : 1.1 * disk capacity      |1.1 * min(used ,capacity)
     *      | sparse: 1.1 * min(used ,capacity)       |
     * -----|-----------------------------------------|-------------
     * raw  | preallocated: disk capacity             |disk capacity
     *      | sparse: min(used,capacity)              |
     *
     * */
    private double getTotalSizeForClonedDisks(Collection<DiskImage> diskImages) {
        return getTotalSizeForDisksByMethod(diskImages, new SizeAssessment() {
            @Override
            public double getSizeForDisk(DiskImage diskImage) {
                double sizeForDisk = diskImage.getCapacity();
                if ((storageDomain.getStorageType().isFileDomain() && diskImage.getVolumeType() == VolumeType.Sparse) ||
                        storageDomain.getStorageType().isBlockDomain() && diskImage.getVolumeFormat() == VolumeFormat.COW) {
                    double usedSpace = diskImage.getActualDiskWithSnapshotsSizeInBytes();
                    sizeForDisk = Math.min(diskImage.getCapacity(), usedSpace);
                }

                if (diskImage.getVolumeFormat() == VolumeFormat.COW) {
                    sizeForDisk = Math.ceil(QCOW_OVERHEAD_FACTOR * sizeForDisk);
                }
                return sizeForDisk;
            }
        });
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
        return getTotalSizeForDisksByMethod(diskImages, new SizeAssessment() {
            @Override
            public double getSizeForDisk(DiskImage diskImage) {
                double sizeForDisk = diskImage.getCapacity();
                if ((storageDomain.getStorageType().isFileDomain() && diskImage.getVolumeType() == VolumeType.Sparse)
                    || diskImage.getVolumeFormat() == VolumeFormat.COW) {
                    sizeForDisk = diskImage.getActualDiskWithSnapshotsSizeInBytes();
                }

                if (diskImage.getVolumeFormat() == VolumeFormat.COW) {
                    sizeForDisk = Math.ceil(QCOW_OVERHEAD_FACTOR * sizeForDisk);
                }
                return sizeForDisk;
            }
        });
    }

    public ValidationResult hasSpaceForNewDisks(Collection<DiskImage> diskImages) {
        double availableSize = storageDomain.getAvailableDiskSizeInBytes();
        double totalSizeForDisks = getTotalSizeForNewDisks(diskImages);

        return validateRequiredSpace(availableSize, totalSizeForDisks);
    }

    public ValidationResult hasSpaceForClonedDisks(Collection<DiskImage> diskImages) {
        double availableSize = storageDomain.getAvailableDiskSizeInBytes();
        double totalSizeForDisks = getTotalSizeForClonedDisks(diskImages);

        return validateRequiredSpace(availableSize, totalSizeForDisks);
    }

    public ValidationResult hasSpaceForDisksWithSnapshots(Collection<DiskImage> diskImages) {
        double availableSize = storageDomain.getAvailableDiskSizeInBytes();
        double totalSizeForDisks = getTotalSizeForDisksWithSnapshots(diskImages);

        return validateRequiredSpace(availableSize, totalSizeForDisks);
    }

    public ValidationResult hasSpaceForAllDisks(Collection<DiskImage> newDiskImages, Collection<DiskImage> clonedDiskImages) {
        double availableSize = storageDomain.getAvailableDiskSizeInBytes();
        double totalSizeForNewDisks = getTotalSizeForNewDisks(newDiskImages);
        double totalSizeForClonedDisks = getTotalSizeForClonedDisks(clonedDiskImages);
        double totalSizeForDisks = totalSizeForNewDisks + totalSizeForClonedDisks;

        return validateRequiredSpace(availableSize, totalSizeForDisks);
    }

    public ValidationResult hasSpaceForDiskWithSnapshots(DiskImage diskImage) {
        return hasSpaceForDisksWithSnapshots(Collections.singleton(diskImage));
    }

    public ValidationResult hasSpaceForClonedDisk(DiskImage diskImage) {
        return hasSpaceForClonedDisks(Collections.singleton(diskImage));
    }

    public ValidationResult hasSpaceForNewDisk(DiskImage diskImage) {
        return hasSpaceForNewDisks(Collections.singleton(diskImage));
    }

    private ValidationResult validateRequiredSpace(double availableSize, double requiredSize) {
        if (availableSize >= requiredSize) {
            return ValidationResult.VALID;
        }

        return new ValidationResult(VdcBllMessages.ACTION_TYPE_FAILED_DISK_SPACE_LOW_ON_STORAGE_DOMAIN,
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

    private static interface SizeAssessment {
        public double getSizeForDisk(DiskImage diskImage);
    }
}
