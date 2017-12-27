package org.ovirt.engine.core.bll.validator.storage;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.lang.StringUtils;
import org.ovirt.engine.core.bll.ValidationResult;
import org.ovirt.engine.core.common.businessentities.StorageDomain;
import org.ovirt.engine.core.common.businessentities.StorageFormatType;
import org.ovirt.engine.core.common.businessentities.StorageServerConnections;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VMStatus;
import org.ovirt.engine.core.common.businessentities.storage.Disk;
import org.ovirt.engine.core.common.businessentities.storage.DiskImage;
import org.ovirt.engine.core.common.businessentities.storage.DiskStorageType;
import org.ovirt.engine.core.common.businessentities.storage.DiskVmElement;
import org.ovirt.engine.core.common.businessentities.storage.LUNs;
import org.ovirt.engine.core.common.businessentities.storage.LunDisk;
import org.ovirt.engine.core.common.businessentities.storage.ScsiGenericIO;
import org.ovirt.engine.core.common.businessentities.storage.StorageType;
import org.ovirt.engine.core.common.businessentities.storage.VolumeType;
import org.ovirt.engine.core.common.constants.StorageConstants;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;
import org.ovirt.engine.core.dao.DiskImageDao;
import org.ovirt.engine.core.dao.DiskLunMapDao;
import org.ovirt.engine.core.dao.StorageDomainDao;
import org.ovirt.engine.core.dao.VmDao;
import org.ovirt.engine.core.di.Injector;
import org.ovirt.engine.core.utils.ReplacementUtils;

/**
 * A validator for the {@link Disk} class.
 *
 */
public class DiskValidator {

    private final Disk disk;

    protected static final String DISK_NAME_VARIABLE = "DiskName";
    protected static final String VM_LIST = "VmList";
    protected static final String VM_NAME_VARIABLE = "VmName";

    public DiskValidator(Disk disk) {
        this.disk = disk;
    }

    protected VmDao getVmDao() {
        return DbFacade.getInstance().getVmDao();
    }

    protected DiskImageDao getDiskImageDao() {
        return DbFacade.getInstance().getDiskImageDao();
    }

    public ValidationResult validateUnsupportedDiskStorageType(DiskStorageType... diskStorageTypes) {
        List<DiskStorageType> diskStorageTypeList = Arrays.asList(diskStorageTypes);
        if (diskStorageTypeList.contains(disk.getDiskStorageType())) {
            return new ValidationResult(EngineMessage.ACTION_TYPE_FAILED_NOT_SUPPORTED_DISK_STORAGE_TYPE,
                    String.format("$diskStorageType %s", disk.getDiskStorageType()));
        }
        return ValidationResult.VALID;
    }

    public ValidationResult validateNotHostedEngineDisk() {
        return isHostedEngineDirectLunDisk() ? new ValidationResult(EngineMessage.ACTION_TYPE_FAILED_HOSTED_ENGINE_DISK) :
                ValidationResult.VALID;
    }

    private boolean isHostedEngineDirectLunDisk() {
        return disk.getDiskStorageType() == DiskStorageType.LUN &&
                    StorageConstants.HOSTED_ENGINE_LUN_DISK_ALIAS.equals(disk.getDiskAlias());
    }

    public ValidationResult isUsingScsiReservationValid(VM vm, DiskVmElement dve, LunDisk lunDisk) {
        if (vm != null) {
            // scsi reservation can be enabled only when sgio is unfiltered
            if (dve.isUsingScsiReservation() && lunDisk.getSgio() == ScsiGenericIO.FILTERED) {
                return new ValidationResult(EngineMessage.ACTION_TYPE_FAILED_SGIO_IS_FILTERED);
            }
        }
        return  ValidationResult.VALID;
    }

    public ValidationResult validateConnectionsInLun(StorageType storageType) {
        if (disk.getDiskStorageType() == DiskStorageType.LUN) {
            switch (storageType) {
                case UNKNOWN:
                    return new ValidationResult(EngineMessage.ACTION_TYPE_FAILED_DISK_LUN_HAS_NO_VALID_TYPE);
                case ISCSI:
                    LUNs luns = ((LunDisk)disk).getLun();
                    if (luns.getLunConnections() == null || luns.getLunConnections().isEmpty()) {
                        return new ValidationResult(EngineMessage.ACTION_TYPE_FAILED_DISK_LUN_ISCSI_MISSING_CONNECTION_PARAMS);
                    }

                    for (StorageServerConnections conn : luns.getLunConnections()) {
                        if (StringUtils.isEmpty(conn.getIqn()) || StringUtils.isEmpty(conn.getConnection())
                                || StringUtils.isEmpty(conn.getPort())) {
                            return new ValidationResult(EngineMessage.ACTION_TYPE_FAILED_DISK_LUN_ISCSI_MISSING_CONNECTION_PARAMS);
                        }
                    }
                    break;
                default:
                    break;
            }
        }
        return ValidationResult.VALID;
    }

    public ValidationResult validateLunAlreadyInUse() {
        if (disk.getDiskStorageType() == DiskStorageType.LUN) {
            if (Injector.get(DiskLunMapDao.class).getDiskIdByLunId(((LunDisk) disk).getLun().getLUNId()) != null) {
                return new ValidationResult(EngineMessage.ACTION_TYPE_FAILED_DISK_LUN_IS_ALREADY_IN_USE);
            }
        }
        return ValidationResult.VALID;
    }

    public ValidationResult validRemovableHostedEngineDisks(VM vm) {
        return isHostedEngineDirectLunDisk() || !vm.isHostedEngine()
                ? ValidationResult.VALID
                : new ValidationResult(EngineMessage.ACTION_TYPE_FAILED_HOSTED_ENGINE_DISK);
    }

    public ValidationResult isDiskExists() {
        if (disk == null) {
            return new ValidationResult(EngineMessage.ACTION_TYPE_FAILED_DISK_NOT_EXIST);
        }
        return ValidationResult.VALID;
    }

    public ValidationResult isDiskAttachedToVm(VM vm) {
        List<VM> vms = getVmDao().getVmsListForDisk(disk.getId(), true);
        String[] replacements = {ReplacementUtils.createSetVariableString(DISK_NAME_VARIABLE, disk.getDiskAlias()),
                ReplacementUtils.createSetVariableString(VM_NAME_VARIABLE, vm.getName())};
        return ValidationResult.failWith(EngineMessage.ACTION_TYPE_FAILED_DISK_NOT_ATTACHED_TO_VM, replacements).
                when(vms.stream().noneMatch(vm1 -> vm1.getId().equals(vm.getId())));
    }

    public ValidationResult isDiskPluggedToAnyNonDownVm(boolean checkOnlyVmsSnapshotPluggedTo) {
        String vmNames = getVmDao()
                .getVmsWithPlugInfo(disk.getId())
                .stream()
                .filter(p -> p.getFirst().getStatus() != VMStatus.Down)
                .filter(p -> p.getSecond().isPlugged())
                .filter(p -> !(checkOnlyVmsSnapshotPluggedTo && p.getSecond().getSnapshotId() == null))
                .map(p -> p.getFirst().getName())
                .sorted()
                .collect(Collectors.joining(","));

        if (!vmNames.isEmpty()) {
            return new ValidationResult(EngineMessage.ACTION_TYPE_FAILED_DISK_PLUGGED_TO_NON_DOWN_VMS,
                    ReplacementUtils.createSetVariableString(DISK_NAME_VARIABLE, disk.getDiskAlias()),
                    ReplacementUtils.createSetVariableString(VM_LIST, vmNames));

        }
        return ValidationResult.VALID;
    }

    public ValidationResult isVmNotContainsBootDisk(VM vm) {
        Disk bootDisk = DbFacade.getInstance().getDiskDao().getVmBootActiveDisk(vm.getId());
        if (bootDisk != null) {
            return new ValidationResult(EngineMessage.ACTION_TYPE_FAILED_DISK_BOOT_IN_USE,
                    ReplacementUtils.createSetVariableString("VmName", vm.getName()),
                    ReplacementUtils.createSetVariableString("DiskName", bootDisk.getDiskAlias()));
        }
        return ValidationResult.VALID;
    }

    public ValidationResult isSparsifySupported() {
        if (disk.getDiskStorageType() != DiskStorageType.IMAGE) {
            return new ValidationResult(
                    EngineMessage.ACTION_TYPE_FAILED_DISK_SPARSIFY_NOT_SUPPORTED_BY_DISK_STORAGE_TYPE,
                    getDiskAliasVarReplacement(),
                    ReplacementUtils.createSetVariableString("diskStorageType", disk.getDiskStorageType()));
        }

        if (((DiskImage) disk).getImage().getVolumeType() == VolumeType.Preallocated) {
            return new ValidationResult(
                    EngineMessage.ACTION_TYPE_FAILED_DISK_SPARSIFY_NOT_SUPPORTED_FOR_PREALLOCATED,
                    getDiskAliasVarReplacement());
        }

        StorageDomain diskStorageDomain =
                Injector.get(StorageDomainDao.class).get(((DiskImage) disk).getStorageIds().get(0));
        StorageType domainStorageType = diskStorageDomain.getStorageType();

        if (!domainStorageType.isFileDomain() && !domainStorageType.isBlockDomain()) {
            return new ValidationResult(EngineMessage.ACTION_TYPE_FAILED_DISK_SPARSIFY_NOT_SUPPORTED_BY_STORAGE_TYPE,
                    getDiskAliasVarReplacement(),
                    getStorageDomainNameVarReplacement(diskStorageDomain),
                    ReplacementUtils.createSetVariableString("storageType", domainStorageType));
        }

        if (domainStorageType.isBlockDomain() && disk.isWipeAfterDelete()) {
            return new ValidationResult(
                    EngineMessage.ACTION_TYPE_FAILED_DISK_SPARSIFY_NOT_SUPPORTED_BY_UNDERLYING_STORAGE_WHEN_WAD_IS_ENABLED,
                    getStorageDomainNameVarReplacement(diskStorageDomain),
                    getDiskAliasVarReplacement());
        }

        return ValidationResult.VALID;
    }

    /***
     * Block the move/copy of a disk that has snapshots created before it's extension
     * on block pre-V4 domains.
     * @see <a href="https://bugzilla.redhat.com/1523614">https://bugzilla.redhat.com/1523614</a>
     */
    public ValidationResult diskWasExtendedAfterSnapshotWasTaken(StorageDomain storageDomain) {
        if (StorageFormatType.V4.compareTo(storageDomain.getStorageFormat()) > 0 &&
                storageDomain.getStorageType().isBlockDomain()) {
            List<DiskImage> diskImages = getDiskImageDao().getAllSnapshotsForImageGroup(disk.getId());
            boolean badSnapshotsPresent = diskImages.stream().anyMatch(d -> d.getSize() < disk.getSize());

            if (badSnapshotsPresent) {
                return new ValidationResult(EngineMessage.CANNOT_MOVE_DISK,
                        ReplacementUtils.createSetVariableString("Snapshots",
                                diskImages.stream()
                                        .filter(d -> !d.getActive())
                                        .map(DiskImage::getDescription)
                                        .collect(Collectors.joining(", "))));
            }
        }

        return ValidationResult.VALID;
    }

    private String getDiskAliasVarReplacement() {
        return ReplacementUtils.createSetVariableString("diskAlias", disk.getDiskAlias());
    }

    private String getStorageDomainNameVarReplacement(StorageDomain storageDomain) {
        return ReplacementUtils.createSetVariableString("storageDomainName", storageDomain.getName());
    }
}
