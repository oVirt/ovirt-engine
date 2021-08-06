package org.ovirt.engine.core.bll.validator.storage;

import org.ovirt.engine.core.bll.ValidationResult;
import org.ovirt.engine.core.bll.utils.VmDeviceUtils;
import org.ovirt.engine.core.bll.validator.VmValidationUtils;
import org.ovirt.engine.core.common.businessentities.StorageDomain;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.storage.Disk;
import org.ovirt.engine.core.common.businessentities.storage.DiskInterface;
import org.ovirt.engine.core.common.businessentities.storage.DiskStorageType;
import org.ovirt.engine.core.common.businessentities.storage.DiskVmElement;
import org.ovirt.engine.core.common.businessentities.storage.LunDisk;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.common.osinfo.OsRepository;
import org.ovirt.engine.core.common.utils.VmDeviceCommonUtils;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.StorageDomainDao;
import org.ovirt.engine.core.di.Injector;

public class DiskVmElementValidator {

    private Disk disk;
    private DiskVmElement diskVmElement;

    public DiskVmElementValidator(Disk disk, DiskVmElement diskVmElement) {
        this.disk = disk;
        this.diskVmElement = diskVmElement;
    }

    public Guid getDiskId() {
        return disk.getId();
    }

    public ValidationResult isReadOnlyPropertyCompatibleWithInterface() {
        if (Boolean.TRUE.equals(diskVmElement.isReadOnly())) {
            DiskInterface diskInterface = diskVmElement.getDiskInterface();

            if (diskInterface == DiskInterface.IDE || diskInterface == DiskInterface.SATA) {
                return new ValidationResult(EngineMessage.ACTION_TYPE_FAILED_INTERFACE_DOES_NOT_SUPPORT_READ_ONLY_ATTR,
                        String.format("$interface %1$s", diskInterface));
            }

            if (disk.isScsiPassthrough()) {
                return new ValidationResult(EngineMessage.SCSI_PASSTHROUGH_IS_NOT_SUPPORTED_FOR_READ_ONLY_DISK);
            }
        }
        return ValidationResult.VALID;
    }

    /**
     * Verifies Virtio-SCSI interface validity.
     */
    public ValidationResult isVirtIoScsiValid(VM vm) {
        // VM is null, so there is no reason to validate if VM has virtio-scsi device
        if (vm == null) {
            return ValidationResult.VALID;
        }

        if (diskVmElement != null && DiskInterface.VirtIO_SCSI != diskVmElement.getDiskInterface()) {
            return ValidationResult.VALID;
        }

        if (disk.getSgio() != null && DiskStorageType.IMAGE == disk.getDiskStorageType()) {
            return new ValidationResult(EngineMessage.SCSI_GENERIC_IO_IS_NOT_SUPPORTED_FOR_IMAGE_DISK);
        }

        if (!isVirtioScsiControllerAttached(vm)) {
            return new ValidationResult(EngineMessage.CANNOT_PERFORM_ACTION_VIRTIO_SCSI_IS_DISABLED);
        }

        return isDiskInterfaceSupported(vm);
    }

    private boolean isVirtioScsiControllerAttached(VM vm) {
        // First check the vm.getManagedVmDeviceMap() for the VIRTIOSCSI controller
        if (VmDeviceCommonUtils.isVirtIoScsiDeviceExists(vm.getManagedVmDeviceMap().values())) {
            return true;
        }

        // If it does not exist in the map, check in the DB
        VmDeviceUtils vmDeviceUtils = Injector.get(VmDeviceUtils.class);
        return vmDeviceUtils.hasVirtioScsiController(vm.getId());
    }

    public ValidationResult isDiskInterfaceSupported(VM vm) {
        if (vm == null) {
            return ValidationResult.VALID;
        }

        if (!Injector.get(VmValidationUtils.class).isDiskInterfaceSupportedByOs(
                vm.getOs(), vm.getCompatibilityVersion(), vm.getBiosType().getChipsetType(),
                diskVmElement.getDiskInterface())) {
            return new ValidationResult(EngineMessage.ACTION_TYPE_DISK_INTERFACE_UNSUPPORTED,
                    String.format("$osName %s", getOsRepository().getOsName(vm.getOs())));
        }

        return ValidationResult.VALID;
    }

    private static OsRepository getOsRepository() {
        return Injector.get(OsRepository.class);
    }

    public ValidationResult isPassDiscardSupported(Guid storageDomainId) {
        if (!diskVmElement.isPassDiscard()) {
            return ValidationResult.VALID;
        }

        DiskInterface diskInterface = diskVmElement.getDiskInterface();
        if (diskInterface != DiskInterface.VirtIO_SCSI
                && diskInterface != DiskInterface.IDE
                && diskInterface != DiskInterface.SATA) {
            return new ValidationResult(
                    EngineMessage.ACTION_TYPE_FAILED_PASS_DISCARD_NOT_SUPPORTED_BY_DISK_INTERFACE,
                    getDiskAliasVarReplacement());
        }

        if (disk.getDiskStorageType() == DiskStorageType.LUN) {
            return isPassDiscardSupportedByUnderlyingStorageForDirectLun();
        }
        if (disk.getDiskStorageType() == DiskStorageType.IMAGE) {
            return isPassDiscardSupportedByUnderlyingStorageForDiskImage(storageDomainId);
        }
        return new ValidationResult(EngineMessage.ACTION_TYPE_FAILED_PASS_DISCARD_NOT_SUPPORTED_BY_DISK_STORAGE_TYPE,
                getDiskAliasVarReplacement(), String.format("$diskStorageType %s", disk.getDiskStorageType()));
    }

    private ValidationResult isPassDiscardSupportedByUnderlyingStorageForDirectLun() {
        if (((LunDisk) disk).getLun().supportsDiscard()) {
            return ValidationResult.VALID;
        }
        return new ValidationResult(
                EngineMessage.ACTION_TYPE_FAILED_PASS_DISCARD_NOT_SUPPORTED_FOR_DIRECT_LUN_BY_UNDERLYING_STORAGE,
                getDiskAliasVarReplacement());
    }

    private ValidationResult isPassDiscardSupportedByUnderlyingStorageForDiskImage(Guid storageDomainId) {
        StorageDomain diskStorageDomain = Injector.get(StorageDomainDao.class).get(storageDomainId);
        if (diskStorageDomain.getStorageType().isFileDomain()) {
            return ValidationResult.VALID;
        } else if (diskStorageDomain.getStorageType().isBlockDomain()) {
            if (!diskStorageDomain.getSupportsDiscard()) {
                return new ValidationResult(EngineMessage
                        .ACTION_TYPE_FAILED_PASS_DISCARD_NOT_SUPPORTED_FOR_DISK_IMAGE_BY_UNDERLYING_STORAGE,
                        getDiskAliasVarReplacement(), getStorageDomainNameVarReplacement(diskStorageDomain));
            }
            if (disk.isWipeAfterDelete()) {
                return new ValidationResult(EngineMessage
                        .ACTION_TYPE_FAILED_PASS_DISCARD_NOT_SUPPORTED_BY_UNDERLYING_STORAGE_WHEN_WAD_IS_ENABLED,
                        getStorageDomainNameVarReplacement(diskStorageDomain), getDiskAliasVarReplacement());
            }
            return ValidationResult.VALID;
        }
        return new ValidationResult(EngineMessage.ACTION_TYPE_FAILED_PASS_DISCARD_NOT_SUPPORTED_BY_STORAGE_TYPE,
                getDiskAliasVarReplacement(), getStorageDomainNameVarReplacement(diskStorageDomain),
                String.format("$storageType %s", diskStorageDomain.getStorageType()));
    }

    private String getDiskAliasVarReplacement() {
        return String.format("$diskAlias %s", disk.getDiskAlias());
    }

    private String getStorageDomainNameVarReplacement(StorageDomain storageDomain) {
        return String.format("$storageDomainName %s", storageDomain.getName());
    }
}
