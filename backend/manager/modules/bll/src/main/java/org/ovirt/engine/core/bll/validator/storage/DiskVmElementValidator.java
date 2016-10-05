package org.ovirt.engine.core.bll.validator.storage;

import org.ovirt.engine.core.bll.ValidationResult;
import org.ovirt.engine.core.bll.utils.VmDeviceUtils;
import org.ovirt.engine.core.bll.validator.VmValidationUtils;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.storage.Disk;
import org.ovirt.engine.core.common.businessentities.storage.DiskInterface;
import org.ovirt.engine.core.common.businessentities.storage.DiskStorageType;
import org.ovirt.engine.core.common.businessentities.storage.DiskVmElement;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.common.osinfo.OsRepository;
import org.ovirt.engine.core.common.utils.SimpleDependencyInjector;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.di.Injector;

public class DiskVmElementValidator {

    private Disk disk;
    private DiskVmElement diskVmElement;

    public DiskVmElementValidator(Disk disk, DiskVmElement diskVmElement) {
        this.disk = disk;
        this.diskVmElement = diskVmElement;
    }

    public ValidationResult isReadOnlyPropertyCompatibleWithInterface() {
        if (Boolean.TRUE.equals(disk.getReadOnly())) {
            DiskInterface diskInterface = diskVmElement.getDiskInterface();

            if (diskInterface == DiskInterface.IDE) {
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
        if (vm != null && DiskInterface.VirtIO_SCSI != diskVmElement.getDiskInterface()) {
            return ValidationResult.VALID;
        }

        if (disk.getSgio() != null) {
            if (DiskStorageType.IMAGE == disk.getDiskStorageType()) {
                return new ValidationResult(EngineMessage.SCSI_GENERIC_IO_IS_NOT_SUPPORTED_FOR_IMAGE_DISK);
            }
        }

        if (vm != null) {
            if (!isVirtioScsiControllerAttached(vm.getId())) {
                return new ValidationResult(EngineMessage.CANNOT_PERFORM_ACTION_VIRTIO_SCSI_IS_DISABLED);
            }
            else {
                return isOsSupportedForVirtIoScsi(vm);

            }
        }

        return ValidationResult.VALID;
    }

    private boolean isVirtioScsiControllerAttached(Guid vmId) {
        VmDeviceUtils vmDeviceUtils = Injector.get(VmDeviceUtils.class);
        return vmDeviceUtils.hasVirtioScsiController(vmId);
    }

    /**
     * Validates that the OS is supported for Virtio-SCSI interface.
     */
    private ValidationResult isOsSupportedForVirtIoScsi(VM vm) {
        if (!VmValidationUtils.isDiskInterfaceSupportedByOs(
                vm.getOs(), vm.getCompatibilityVersion(), DiskInterface.VirtIO_SCSI)) {
            return new ValidationResult(EngineMessage.ACTION_TYPE_FAILED_GUEST_OS_VERSION_IS_NOT_SUPPORTED);
        }

        return ValidationResult.VALID;
    }

    public ValidationResult isDiskInterfaceSupported(VM vm) {
        if (vm != null) {
            if (!VmValidationUtils.isDiskInterfaceSupportedByOs(
                    vm.getOs(), vm.getCompatibilityVersion(), diskVmElement.getDiskInterface())) {
                return new ValidationResult(EngineMessage.ACTION_TYPE_DISK_INTERFACE_UNSUPPORTED,
                        String.format("$osName %s", getOsRepository().getOsName(vm.getOs())));
            }
        }

        return ValidationResult.VALID;
    }

    private static OsRepository getOsRepository() {
        return SimpleDependencyInjector.getInstance().get(OsRepository.class);
    }
}
