package org.ovirt.engine.core.bll.validator;

import java.util.List;

import org.ovirt.engine.core.bll.ValidationResult;
import org.ovirt.engine.core.common.FeatureSupported;
import org.ovirt.engine.core.common.businessentities.Disk;
import org.ovirt.engine.core.common.businessentities.Disk.DiskStorageType;
import org.ovirt.engine.core.common.businessentities.DiskInterface;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.config.Config;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.common.errors.VdcBllMessages;
import org.ovirt.engine.core.common.osinfo.OsRepository;
import org.ovirt.engine.core.common.utils.SimpleDependecyInjector;

/**
 * A validator for the {@link Disk} class.
 *
 */
public class DiskValidator {

    private Disk disk;

    public DiskValidator(Disk disk) {
        this.disk = disk;
    }

    /**
     * Verifies Virtio-SCSI interface validity.
     */
    public ValidationResult isVirtIoScsiValid(VM vm) {
        if (DiskInterface.VirtIO_SCSI != disk.getDiskInterface()) {
            return ValidationResult.VALID;
        }

        if (disk.getSgio() != null) {
            if (DiskStorageType.IMAGE == disk.getDiskStorageType()) {
                return new ValidationResult(VdcBllMessages.SCSI_GENERIC_IO_IS_NOT_SUPPORTED_FOR_IMAGE_DISK);
            }
        }

        if (vm != null) {
            if (!FeatureSupported.virtIoScsi(vm.getVdsGroupCompatibilityVersion())) {
                return new ValidationResult(VdcBllMessages.VIRTIO_SCSI_INTERFACE_IS_NOT_AVAILABLE_FOR_CLUSTER_LEVEL);
            }

            return isOsSupportedForVirtIoScsi(vm);
        }

        return ValidationResult.VALID;
    }

    /**
     * Validates that the OS is supported for Virtio-SCSI interface.
     */
    public ValidationResult isOsSupportedForVirtIoScsi(VM vm) {
        //TODO move this config val to osinfo
        final List<String> unsupportedOSs = Config.<List<String>> GetValue(ConfigValues.VirtIoScsiUnsupportedOsList);
        String vmOs = SimpleDependecyInjector.getInstance().get(OsRepository.class).getUniqueOsNames().get(vm.getVmOsId());
        for (String os : unsupportedOSs) {
            if (os.equalsIgnoreCase(vmOs)) {
                return new ValidationResult(VdcBllMessages.ACTION_TYPE_FAILED_GUEST_OS_VERSION_IS_NOT_SUPPORTED);
            }
        }
        return ValidationResult.VALID;
    }
}
