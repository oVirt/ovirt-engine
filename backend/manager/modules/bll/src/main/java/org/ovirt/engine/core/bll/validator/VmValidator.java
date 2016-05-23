package org.ovirt.engine.core.bll.validator;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.ovirt.engine.core.bll.ValidationResult;
import org.ovirt.engine.core.bll.hostdev.HostDeviceManager;
import org.ovirt.engine.core.bll.storage.disk.image.ImagesHandler;
import org.ovirt.engine.core.bll.validator.storage.DiskImagesValidator;
import org.ovirt.engine.core.common.VdcActionUtils;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.businessentities.MigrationSupport;
import org.ovirt.engine.core.common.businessentities.Snapshot.SnapshotType;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VMStatus;
import org.ovirt.engine.core.common.businessentities.VmDevice;
import org.ovirt.engine.core.common.businessentities.VmDeviceGeneralType;
import org.ovirt.engine.core.common.businessentities.VmDeviceId;
import org.ovirt.engine.core.common.businessentities.VmStatic;
import org.ovirt.engine.core.common.businessentities.network.VmNetworkInterface;
import org.ovirt.engine.core.common.businessentities.network.VmNic;
import org.ovirt.engine.core.common.businessentities.storage.Disk;
import org.ovirt.engine.core.common.businessentities.storage.DiskInterface;
import org.ovirt.engine.core.common.businessentities.storage.DiskVmElement;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;
import org.ovirt.engine.core.dao.DiskDao;
import org.ovirt.engine.core.dao.DiskVmElementDao;
import org.ovirt.engine.core.di.Injector;
import org.ovirt.engine.core.utils.ReplacementUtils;

/** A Validator for various VM validate needs */
public class VmValidator {
    private VM vm;

    public VmValidator(VM vm) {
        this.vm = vm;
    }

    /** @return Validation result that indicates if the VM is during migration or not. */
    public ValidationResult vmNotDuringMigration() {
        if (vm.getStatus() == VMStatus.MigratingFrom || vm.getStatus() == VMStatus.MigratingTo) {
            return new ValidationResult(EngineMessage.ACTION_TYPE_FAILED_MIGRATION_IN_PROGRESS);
        }

        return ValidationResult.VALID;
    }

    /** @return Validation result that indicates if the VM is down or not. */
    public ValidationResult vmDown() {
        if (!vm.isDown()) {
            return new ValidationResult(EngineMessage.ACTION_TYPE_FAILED_VM_IS_NOT_DOWN);
        }

        return ValidationResult.VALID;
    }

    /** @return Validation result that indicates if the VM is qualified to have its snapshots merged. */
    public ValidationResult vmQualifiedForSnapshotMerge() {
        if (!vm.isQualifiedForSnapshotMerge()) {
            return new ValidationResult(EngineMessage.ACTION_TYPE_FAILED_VM_IS_NOT_DOWN_OR_UP,
                    String.format("$VmName %s", vm.getName()));
        }

        return ValidationResult.VALID;
    }

    public ValidationResult vmNotLocked() {
        if (vm.getStatus() == VMStatus.ImageLocked) {
            return new ValidationResult(EngineMessage.ACTION_TYPE_FAILED_VM_IS_LOCKED);
        }

        return ValidationResult.VALID;
    }

    public ValidationResult vmNotSavingRestoring() {
        if (vm.getStatus().isHibernating() || vm.getStatus() == VMStatus.RestoringState) {
            return new ValidationResult(EngineMessage.ACTION_TYPE_FAILED_VM_IS_SAVING_RESTORING);
        }

        return ValidationResult.VALID;
    }

    public ValidationResult validateVmStatusUsingMatrix(VdcActionType actionType) {
        if (!VdcActionUtils.canExecute(Arrays.asList(vm), VM.class,
                actionType)) {
            return new ValidationResult(EngineMessage.ACTION_TYPE_FAILED_VM_STATUS_ILLEGAL,
                    LocalizedVmStatus.from(vm.getStatus()));
        }

        return ValidationResult.VALID;
    }

    public ValidationResult vmNotIlegal() {
        if (vm.getStatus() == VMStatus.ImageIllegal) {
            return new ValidationResult(EngineMessage.ACTION_TYPE_FAILED_VM_IMAGE_IS_ILLEGAL);
        }

        return ValidationResult.VALID;
    }

    public ValidationResult vmNotRunningStateless() {
        if (DbFacade.getInstance().getSnapshotDao().exists(vm.getId(), SnapshotType.STATELESS)) {
            EngineMessage message = vm.isRunning() ? EngineMessage.ACTION_TYPE_FAILED_VM_RUNNING_STATELESS :
                    EngineMessage.ACTION_TYPE_FAILED_VM_HAS_STATELESS_SNAPSHOT_LEFTOVER;
            return new ValidationResult(message);
        }

        return ValidationResult.VALID;
    }

    /**
     * @return ValidationResult indicating whether snapshots of disks are attached to other vms.
     */
    public ValidationResult vmNotHavingDeviceSnapshotsAttachedToOtherVms(boolean onlyPlugged) {
        List<Disk> vmDisks = getDbFacade().getDiskDao().getAllForVm(vm.getId());
        ValidationResult result =
                new DiskImagesValidator(ImagesHandler.filterImageDisks(vmDisks, true, false, true))
                        .diskImagesSnapshotsNotAttachedToOtherVms(onlyPlugged);
        if (result != ValidationResult.VALID) {
            return result;
        }

        return ValidationResult.VALID;
    }

    /**
     * Determines whether VirtIO-SCSI can be disabled for the VM
     * (can be disabled when no disk uses VirtIO-SCSI interface).
     */
    public ValidationResult canDisableVirtioScsi(Collection<? extends Disk> vmDisks) {
        if (vmDisks == null) {
            vmDisks = getDiskDao().getAllForVm(vm.getId(), true);
            populateDisksWithVmData(vmDisks, vm.getId());
        }

        boolean isVirtioScsiDiskExist =
                vmDisks.stream().anyMatch(d -> d.getDiskVmElementForVm(vm.getId()).getDiskInterface() == DiskInterface.VirtIO_SCSI);

        if (isVirtioScsiDiskExist) {
            return new ValidationResult(EngineMessage.CANNOT_DISABLE_VIRTIO_SCSI_PLUGGED_DISKS);
        }

        return ValidationResult.VALID;
    }

    private void populateDisksWithVmData(Collection<? extends Disk> disks, Guid vmId) {
        for (Disk disk : disks) {
            DiskVmElement dve = getDiskVmElementDao().get(new VmDeviceId(disk.getId(), vmId));
            disk.setDiskVmElements(Collections.singletonList(dve));
        }
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
        List<VmNetworkInterface> vnics =
                getDbFacade().getVmNetworkInterfaceDao().getAllForVm(vm.getId());

        List<String> passthroughVnicNames =
                vnics.stream().filter(VmNic::isPassthrough).map(VmNic::getName).collect(Collectors.toList());

        if (!passthroughVnicNames.isEmpty()) {
            Collection<String> replacements = ReplacementUtils.replaceWith("interfaces", passthroughVnicNames);
            replacements.add(String.format("$vmName %s", vm.getName()));
            return new ValidationResult(EngineMessage.ACTION_TYPE_FAILED_MIGRATION_OF_PASSTHROUGH_VNICS_IS_NOT_SUPPORTED,
                    replacements);
        }
        return ValidationResult.VALID;
    }

    /**
     * Checks whether VM uses lun with scsi reservation true.
     * @return If scsi lun with scsi reservation is plugged to VM
     */
    public ValidationResult isVmPluggedDiskNotUsingScsiReservation() {
        List<VmDevice> devices = getDbFacade().getVmDeviceDao().getVmDeviceByVmIdAndType(vm.getId(), VmDeviceGeneralType.DISK);
        for (VmDevice device : devices) {
            if (device.getIsPlugged() && device.isUsingScsiReservation()) {
                return new ValidationResult(EngineMessage.ACTION_TYPE_FAILED_VM_USES_SCSI_RESERVATION,
                        String.format("$VmName %s", vm.getName()));
            }
        }
        return ValidationResult.VALID;
    }

    public ValidationResult vmNotHavingPciPassthroughDevices() {
        if (getHostDeviceManager().checkVmNeedsPciDevices(vm.getId())) {
            return new ValidationResult(EngineMessage.ACTION_TYPE_FAILED_VM_HAS_ATTACHED_PCI_HOST_DEVICES);
        }
        return ValidationResult.VALID;
    }

    private HostDeviceManager getHostDeviceManager() {
        return Injector.get(HostDeviceManager.class);
    }

    public ValidationResult isPinnedVmRunningOnDedicatedHost(VM recentVm, VmStatic paramVm){
        boolean isPinned = paramVm.getMigrationSupport() == MigrationSupport.PINNED_TO_HOST;
        Guid vdsId = recentVm.getRunOnVds();
        List<Guid> hostList = paramVm.getDedicatedVmForVdsList();

        // If hostList is empty -> all hosts are allowed
        if (isPinned && vdsId != null && !hostList.isEmpty() && !hostList.contains(vdsId)){
            // VM is NOT running on a dedicated host
            // fail with error message
            String hostName = String.format("$hostName %1$s", recentVm.getRunOnVdsName());
            return new ValidationResult(EngineMessage.ACTION_TYPE_FAILED_PINNED_VM_NOT_RUNNING_ON_DEDICATED_HOST, hostName, hostName);
        }

        return ValidationResult.VALID;
    }

    public ValidationResult isVmExists() {
        if (vm == null) {
            return new ValidationResult(EngineMessage.ACTION_TYPE_FAILED_VM_NOT_FOUND);
        }
        return ValidationResult.VALID;
    }

    public DiskVmElementDao getDiskVmElementDao() {
        return DbFacade.getInstance().getDiskVmElementDao();
    }

}
