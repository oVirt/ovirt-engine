package org.ovirt.engine.core.bll.validator;

import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.Predicate;
import org.ovirt.engine.core.bll.Backend;
import org.ovirt.engine.core.bll.ValidationResult;
import org.ovirt.engine.core.bll.utils.VmDeviceUtils;
import org.ovirt.engine.core.common.FeatureSupported;
import org.ovirt.engine.core.common.businessentities.Disk;
import org.ovirt.engine.core.common.businessentities.Disk.DiskStorageType;
import org.ovirt.engine.core.common.businessentities.DiskInterface;
import org.ovirt.engine.core.common.businessentities.LUNs;
import org.ovirt.engine.core.common.businessentities.StorageType;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VMStatus;
import org.ovirt.engine.core.common.businessentities.VmDevice;
import org.ovirt.engine.core.common.constants.StorageConstants;
import org.ovirt.engine.core.common.errors.VdcBllMessages;
import org.ovirt.engine.core.common.interfaces.VDSBrokerFrontend;
import org.ovirt.engine.core.common.osinfo.OsRepository;
import org.ovirt.engine.core.common.utils.Pair;
import org.ovirt.engine.core.common.utils.SimpleDependecyInjector;
import org.ovirt.engine.core.common.vdscommands.GetDeviceListVDSCommandParameters;
import org.ovirt.engine.core.common.vdscommands.VDSCommandType;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;
import org.ovirt.engine.core.dao.VmDAO;

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

            if (!isVirtioScsiControllerAttached(vm.getId())) {
                return new ValidationResult(VdcBllMessages.CANNOT_PERFORM_ACTION_VIRTIO_SCSI_IS_DISABLED);
            }

            return isOsSupportedForVirtIoScsi(vm);
        }

        return ValidationResult.VALID;
    }

    /**
     * Validates that the OS is supported for Virtio-SCSI interface.
     */
    public ValidationResult isOsSupportedForVirtIoScsi(VM vm) {
        if (!VmValidationUtils.isDiskInterfaceSupportedByOs(vm.getOs(), vm.getVdsGroupCompatibilityVersion(), DiskInterface.VirtIO_SCSI)) {
            return new ValidationResult(VdcBllMessages.ACTION_TYPE_FAILED_GUEST_OS_VERSION_IS_NOT_SUPPORTED);
        }

        return ValidationResult.VALID;
    }

    public boolean isVirtioScsiControllerAttached(Guid vmId) {
        return VmDeviceUtils.isVirtioScsiControllerAttached(vmId);
    }

    public ValidationResult isDiskPluggedToVmsThatAreNotDown(boolean checkOnlyVmsSnapshotPluggedTo, List<Pair<VM, VmDevice>> vmsForDisk) {
        if (vmsForDisk == null) {
            vmsForDisk = getVmDAO().getVmsWithPlugInfo(disk.getId());
        }

        for (Pair<VM, VmDevice> pair : vmsForDisk) {
            VmDevice vmDevice = pair.getSecond();

            if (checkOnlyVmsSnapshotPluggedTo && vmDevice.getSnapshotId() == null) {
                continue;
            }

            VM currVm = pair.getFirst();
            if (VMStatus.Down != currVm.getStatus()) {
                if (vmDevice.getIsPlugged()) {
                    return new ValidationResult(VdcBllMessages.ACTION_TYPE_FAILED_VM_IS_NOT_DOWN);
                }
            }
        }

        return ValidationResult.VALID;

    }

    public ValidationResult isReadOnlyPropertyCompatibleWithInterface() {
        if (Boolean.TRUE.equals(disk.getReadOnly())) {
            DiskInterface diskInterface = disk.getDiskInterface();

            if (diskInterface == DiskInterface.IDE) {
                return new ValidationResult(VdcBllMessages.ACTION_TYPE_FAILED_INTERFACE_DOES_NOT_SUPPORT_READ_ONLY_ATTR,
                        String.format("$interface %1$s", diskInterface));
            }

            if (disk.isScsiPassthrough()) {
                return new ValidationResult(VdcBllMessages.SCSI_PASSTHROUGH_IS_NOT_SUPPORTED_FOR_READ_ONLY_DISK);
            }
        }
        return ValidationResult.VALID;
    }

    public ValidationResult isDiskUsedAsOvfStore() {
        if (disk.isOvfStore()) {
            return new ValidationResult((VdcBllMessages.ACTION_TYPE_FAILED_OVF_DISK_NOT_SUPPORTED));
        }
        return ValidationResult.VALID;
    }

    protected VmDAO getVmDAO() {
        return DbFacade.getInstance().getVmDao();
    }

    public ValidationResult isDiskInterfaceSupported(VM vm) {
        if (vm != null) {
            if (!VmValidationUtils.isDiskInterfaceSupportedByOs(vm.getOs(), vm.getVdsGroupCompatibilityVersion(), disk.getDiskInterface())) {
                return new ValidationResult(VdcBllMessages.ACTION_TYPE_DISK_INTERFACE_UNSUPPORTED,
                        String.format("$osName %s", getOsRepository().getOsName(vm.getOs())));
            }
        }

        return ValidationResult.VALID;
    }

    /**
     * Determines whether the specified LUN is visible to the specified host.
     *
     * @param lun the LUN to examine.
     * @param vds the host to query from.
     *
     * @return whether the specified lun is visible.
     */
    public ValidationResult isLunDiskVisible(final LUNs lun, VDS vds) {
        List<LUNs> luns = executeGetDeviceList(vds.getId(), lun.getLunType());

        // Search LUN in the device list
        boolean lunExists = CollectionUtils.exists(luns, new Predicate() {
            @Override
            public boolean evaluate(Object o) {
                return ((LUNs) o).getId().equals(lun.getId());
            }
        });

        return lunExists ? ValidationResult.VALID :
                new ValidationResult(VdcBllMessages.ACTION_TYPE_FAILED_DISK_LUN_INVALID);
    }

    @SuppressWarnings("unchecked")
    public List<LUNs> executeGetDeviceList(Guid vdsId, StorageType storageType) {
        GetDeviceListVDSCommandParameters parameters =
                new GetDeviceListVDSCommandParameters(vdsId, storageType);
        return (List<LUNs>) getVdsBroker().RunVdsCommand(VDSCommandType.GetDeviceList, parameters).getReturnValue();
    }

    protected VDSBrokerFrontend getVdsBroker() {
        return Backend.getInstance().getResourceManager();
    }

    private static OsRepository getOsRepository() {
        return SimpleDependecyInjector.getInstance().get(OsRepository.class);
    }


    public ValidationResult validateNotHostedEngineDisk() {
        boolean isHostedEngineDisk = disk.getDiskStorageType() == DiskStorageType.LUN &&
                StorageConstants.HOSTED_ENGINE_LUN_DISK_ALIAS.equals(disk.getDiskAlias());
        return isHostedEngineDisk ? new ValidationResult(VdcBllMessages.ACTION_TYPE_FAILED_HOSTED_ENGINE_DISK) :
                ValidationResult.VALID;
    }
}
