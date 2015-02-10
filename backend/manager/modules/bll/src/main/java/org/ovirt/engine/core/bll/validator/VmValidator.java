package org.ovirt.engine.core.bll.validator;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.Predicate;
import org.apache.commons.lang.StringUtils;
import org.ovirt.engine.core.bll.ImagesHandler;
import org.ovirt.engine.core.bll.ValidationResult;
import org.ovirt.engine.core.bll.validator.storage.DiskImagesValidator;
import org.ovirt.engine.core.common.VdcActionUtils;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.businessentities.Snapshot.SnapshotType;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VMStatus;
import org.ovirt.engine.core.common.businessentities.VmDevice;
import org.ovirt.engine.core.common.businessentities.VmDeviceGeneralType;
import org.ovirt.engine.core.common.businessentities.storage.BaseDisk;
import org.ovirt.engine.core.common.businessentities.storage.Disk;
import org.ovirt.engine.core.common.businessentities.storage.DiskImage;
import org.ovirt.engine.core.common.businessentities.storage.DiskInterface;
import org.ovirt.engine.core.common.businessentities.network.VmNetworkInterface;
import org.ovirt.engine.core.common.errors.VdcBllMessages;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;
import org.ovirt.engine.core.dao.DiskDao;
import org.ovirt.engine.core.utils.ReplacementUtils;
import org.ovirt.engine.core.utils.linq.LinqUtils;

/** A Validator for various VM canDoAction needs */
public class VmValidator {
    private Iterable<VM> vms;

    public VmValidator(VM vm) {
        this.vms = Collections.singletonList(vm);
    }

    public VmValidator(Iterable<VM> vms) {
        this.vms = vms;
    }

    /** @return Validation result that indicates if the VM is during migration or not. */
    public ValidationResult vmNotDuringMigration() {
        for (VM vm : vms) {
            if (vm.getStatus() == VMStatus.MigratingFrom || vm.getStatus() == VMStatus.MigratingTo) {
                return new ValidationResult(VdcBllMessages.ACTION_TYPE_FAILED_MIGRATION_IN_PROGRESS);
            }
        }

        return ValidationResult.VALID;
    }

    /** @return Validation result that indicates if the VM is down or not. */
    public ValidationResult vmDown() {
        for (VM vm : vms) {
            if (!vm.isDown()) {
                return new ValidationResult(VdcBllMessages.ACTION_TYPE_FAILED_VM_IS_NOT_DOWN);
            }
        }

        return ValidationResult.VALID;
    }

    /** @return Validation result that indicates if the VM is qualified to have its snapshots merged. */
    public ValidationResult vmQualifiedForSnapshotMerge() {
        for (VM vm : vms) {
            if (!vm.isQualifiedForSnapshotMerge()) {
                return new ValidationResult(VdcBllMessages.ACTION_TYPE_FAILED_VM_IS_NOT_DOWN_OR_UP,
                        String.format("$VmName %s", vm.getName()));
            }
        }

        return ValidationResult.VALID;
    }

    /**
     * @return Validation result indicating that if a host is running, then it is running on a host capable
     * of live merging snapshots.  Should be used in combination with vmQualifiedForSnapshotMerge().
     */
    public ValidationResult vmHostCanLiveMerge() {
        for (VM vm : vms) {
            if (!vm.isDown() &&
                    ((vm.getRunOnVds() == null ||
                    !DbFacade.getInstance().getVdsDao().get(vm.getRunOnVds()).getLiveMergeSupport()))) {
                    return new ValidationResult(VdcBllMessages.ACTION_TYPE_FAILED_VM_HOST_CANNOT_LIVE_MERGE,
                            String.format("$VmName %s", vm.getName()));
            }
        }

        return ValidationResult.VALID;
    }

    public ValidationResult vmNotLocked() {
        for (VM vm : vms) {
            if (vm.getStatus() == VMStatus.ImageLocked) {
                return new ValidationResult(VdcBllMessages.ACTION_TYPE_FAILED_VM_IS_LOCKED);
            }
        }

        return ValidationResult.VALID;
    }

    public ValidationResult vmNotSavingRestoring() {
        for (VM vm : vms) {
            if (vm.getStatus().isHibernating() || vm.getStatus() == VMStatus.RestoringState) {
                return new ValidationResult(VdcBllMessages.ACTION_TYPE_FAILED_VM_IS_SAVING_RESTORING);
            }
        }

        return ValidationResult.VALID;
    }

    public ValidationResult validateVmStatusUsingMatrix(VdcActionType actionType) {
        for (VM vm : vms) {
            if (!VdcActionUtils.canExecute(Arrays.asList(vm), VM.class,
                    actionType)) {
                return new ValidationResult(VdcBllMessages.ACTION_TYPE_FAILED_VM_STATUS_ILLEGAL,
                        LocalizedVmStatus.from(vm.getStatus()));
            }
        }

        return ValidationResult.VALID;
    }

    public ValidationResult vmNotIlegal() {
        for (VM vm : vms) {
            if (vm.getStatus() == VMStatus.ImageIllegal) {
                return new ValidationResult(VdcBllMessages.ACTION_TYPE_FAILED_VM_IMAGE_IS_ILLEGAL);
            }
        }

        return ValidationResult.VALID;
    }

    public ValidationResult vmNotRunningStateless() {
        for (VM vm : vms) {
            if (DbFacade.getInstance().getSnapshotDao().exists(vm.getId(), SnapshotType.STATELESS)) {
                VdcBllMessages message = vm.isRunning() ? VdcBllMessages.ACTION_TYPE_FAILED_VM_RUNNING_STATELESS :
                        VdcBllMessages.ACTION_TYPE_FAILED_VM_HAS_STATELESS_SNAPSHOT_LEFTOVER;
                return new ValidationResult(message);
            }
        }

        return ValidationResult.VALID;
    }

    /**
     * @return ValidationResult indicating whether snapshots of disks are attached to other vms.
     */
    public ValidationResult vmNotHavingDeviceSnapshotsAttachedToOtherVms(boolean onlyPlugged) {
        for (VM vm : vms) {
            List<Disk> vmDisks = getDbFacade().getDiskDao().getAllForVm(vm.getId());
            ValidationResult result =
                    (new DiskImagesValidator(ImagesHandler.filterImageDisks(vmDisks, true, false, true)))
                            .diskImagesSnapshotsNotAttachedToOtherVms(onlyPlugged);
            if (result != ValidationResult.VALID) {
                return result;
            }
        }

        return ValidationResult.VALID;
    }

    /**
     * @return ValidationResult indicating whether there are plugged disk snapshots
     */
    public ValidationResult vmNotHavingPluggedDiskSnapshots(VdcBllMessages message) {
        List<String> vmPluggedDiskSnapshotsInfo = null;
        for (VM vm : vms) {
            List<DiskImage> pluggedDiskSnapshots =
                    DbFacade.getInstance().getDiskImageDao().getAttachedDiskSnapshotsToVm(vm.getId(), Boolean.TRUE);
            if (!pluggedDiskSnapshots.isEmpty()) {
                if (vmPluggedDiskSnapshotsInfo == null) {
                    vmPluggedDiskSnapshotsInfo = new LinkedList<>();
                }
                List<String> pluggedDiskSnapshotAliases = new LinkedList<>();
                for (BaseDisk disk : pluggedDiskSnapshots) {
                    pluggedDiskSnapshotAliases.add(disk.getDiskAlias());
                }
                vmPluggedDiskSnapshotsInfo.add(
                                String.format("%s / %s",
                                        vm.getName(),
                                        StringUtils.join(pluggedDiskSnapshotAliases, ",")));
            }
        }

        if (vmPluggedDiskSnapshotsInfo != null) {
            return new ValidationResult(message,
                    String.format("$disksInfo %s",
                            String.format(StringUtils.join(vmPluggedDiskSnapshotsInfo, "%n"))));
        }

        return ValidationResult.VALID;
    }

    /**
     * Determines whether VirtIO-SCSI can be disabled for the VM
     * (can be disabled when no disk uses VirtIO-SCSI interface).
     */
    public ValidationResult canDisableVirtioScsi(Collection<? extends Disk> vmDisks) {
        if (vmDisks == null) {
            vmDisks = getDiskDao().getAllForVm(vms.iterator().next().getId(), true);
        }

        boolean isVirtioScsiDiskExist = CollectionUtils.exists(vmDisks, new Predicate() {
            @Override
            public boolean evaluate(Object disk) {
                return ((Disk) disk).getDiskInterface() == DiskInterface.VirtIO_SCSI;
            }
        });

        if (isVirtioScsiDiskExist) {
            return new ValidationResult(VdcBllMessages.CANNOT_DISABLE_VIRTIO_SCSI_PLUGGED_DISKS);
        }

        return ValidationResult.VALID;
    }

    public DiskDao getDiskDao() {
        return getDbFacade().getDiskDao();
    }

    public DbFacade getDbFacade() {
        return DbFacade.getInstance();
    }

    /**
     * @return ValidationResult indicating whether a vm contains passthrough vnics
     */
    public ValidationResult vmNotHavingPassthroughVnics() {
        for (VM vm : vms) {
            List<VmNetworkInterface> vnics =
                    getDbFacade().getVmNetworkInterfaceDao().getAllForVm(vm.getId());
            List<VmNetworkInterface> passthroughVnics =
                    LinqUtils.filter(vnics, new org.ovirt.engine.core.utils.linq.Predicate<VmNetworkInterface>() {
                        public boolean eval(VmNetworkInterface vnic) {
                            return vnic.isPassthrough();
                        }
                    });

            Collection<String> replacements = ReplacementUtils.replaceWithNameable("interfaces", passthroughVnics);
            replacements.add(String.format("$vmName %s", vm.getName()));

            if (!passthroughVnics.isEmpty()) {
                return new ValidationResult(VdcBllMessages.ACTION_TYPE_FAILED_MIGRATION_OF_PASSTHROUGH_VNICS_IS_NOT_SUPPORTED,
                        replacements);
            }
        }
        return ValidationResult.VALID;
    }

    /**
     * Checks whether VM uses lun with scsi reservation true.
     * @return If scsi lun with scsi reservation is plugged to VM
     */
    public ValidationResult isVmPluggedDiskNotUsingScsiReservation() {
        for (VM vm : vms) {
            List<VmDevice> devices = getDbFacade().getVmDeviceDao().getVmDeviceByVmIdAndType(vm.getId(), VmDeviceGeneralType.DISK);
            for (VmDevice device : devices) {
                if (device.getIsPlugged() && device.isUsingScsiReservation()) {
                    return new ValidationResult(VdcBllMessages.ACTION_TYPE_FAILED_VM_USES_SCSI_RESERVATION,
                            String.format("$VmName %s", vm.getName()));
                }
            }
        }
        return ValidationResult.VALID;
    }
}
