package org.ovirt.engine.core.bll.storage.disk;

import javax.inject.Inject;

import org.ovirt.engine.core.bll.VmHandler;
import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.bll.utils.VmDeviceUtils;
import org.ovirt.engine.core.bll.validator.VmValidator;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.action.AttachDetachVmDiskParameters;
import org.ovirt.engine.core.common.businessentities.VMStatus;
import org.ovirt.engine.core.common.businessentities.VmDevice;
import org.ovirt.engine.core.common.businessentities.VmDeviceId;
import org.ovirt.engine.core.common.businessentities.storage.Disk;
import org.ovirt.engine.core.common.businessentities.storage.DiskImage;
import org.ovirt.engine.core.common.businessentities.storage.DiskVmElement;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.common.vdscommands.VDSCommandType;

public class DetachDiskFromVmCommand<T extends AttachDetachVmDiskParameters> extends AbstractDiskVmCommand<T> {

    @Inject
    private DiskHandler diskHandler;

    private Disk disk;
    private VmDevice vmDevice;
    private DiskVmElement dveFromDb;

    public DetachDiskFromVmCommand(T parameters, CommandContext cmdContext) {
        super(parameters, cmdContext);
    }

    @Override
    protected boolean validate() {
        if (!validate(new VmValidator(getVm()).isVmExists()) || !canRunActionOnNonManagedVm()) {
            return false;
        }

        if (getVm().getStatus() != VMStatus.Up && getVm().getStatus() != VMStatus.Down) {
            return failVmStatusIllegal();
        }

        disk = diskHandler.loadDiskFromSnapshot(getDiskVmElement().getDiskId(), getParameters().getSnapshotId());
        if (!isDiskExistAndAttachedToVm(disk)) {
            return false;
        }

        vmDevice = getVmDeviceDao().get(new VmDeviceId(disk.getId(), getVmId()));
        if (vmDevice == null) {
            return failValidation(EngineMessage.ACTION_TYPE_FAILED_DISK_ALREADY_DETACHED);
        }

        if (vmDevice.getSnapshotId() != null) {
            disk = diskHandler.loadDiskFromSnapshot(disk.getId(), vmDevice.getSnapshotId());
        }

        if (vmDevice.getIsPlugged() && getVm().getStatus() != VMStatus.Down) {
            if (!isDiskSupportedForPlugUnPlug(getDiskVmElement(), disk.getDiskAlias())) {
                return false;
            }
        }

        // Check if disk has no snapshots before detaching it.
        if (disk.getDiskStorageType().isInternal()) {
            // A "regular" disk cannot be detached if it's part of the vm snapshots
            // when a disk snapshot is being detached, it will always be part of snapshots - but of it's "original" vm,
            // therefore for attached disk snapshot it shouldn't be checked whether it has snapshots or not.
            if (vmDevice.getSnapshotId() == null
                    && getDiskImageDao().getAllSnapshotsForImageGroup(disk.getId()).size() > 1) {
                return failValidation(EngineMessage.ERROR_CANNOT_DETACH_DISK_WITH_SNAPSHOT);
            }
        }
        return true;
    }

    @Override
    protected void setActionMessageParameters() {
        addValidationMessage(EngineMessage.VAR__ACTION__DETACH_ACTION_TO);
        addValidationMessage(EngineMessage.VAR__TYPE__DISK);
    }

    @Override
    protected void executeVmCommand() {
        if (diskShouldBeUnPlugged()) {
            performPlugCommand(VDSCommandType.HotUnPlugDisk, disk, vmDevice);
        }
        getVmDeviceDao().remove(vmDevice.getId());
        getDiskVmElementDao().remove(vmDevice.getId());

        if (!disk.isDiskSnapshot() && disk.getDiskStorageType().isInternal()) {
            // clears snapshot ID
            getImageDao().updateImageVmSnapshotId(((DiskImage) disk).getImageId(), null);
        }

        // update cached image
        VmHandler.updateDisksFromDb(getVm());
        // update vm device boot order
        VmDeviceUtils.updateBootOrder(getVm().getId());
        getVmStaticDao().incrementDbGeneration(getVm().getId());
        setSucceeded(true);
    }

    private boolean diskShouldBeUnPlugged() {
        return Boolean.TRUE.equals(getParameters().isPlugUnPlug() && vmDevice.getIsPlugged()
                && getVm().getStatus() != VMStatus.Down);
    }

    @Override
    public AuditLogType getAuditLogTypeValue() {
        return getSucceeded() ? AuditLogType.USER_DETACH_DISK_FROM_VM : AuditLogType.USER_FAILED_DETACH_DISK_FROM_VM;
    }

    @Override
    public String getDiskAlias() {
        return disk.getDiskAlias();
    }


    @Override
    protected DiskVmElement getDiskVmElement() {
        // When detaching a disk from running VMs a hot unplug is needed, in that case we need the interface info from
        // the DB, since the parameters pass only the VM and disk ID we need to load the rest of the data from the DB
        if (dveFromDb == null) {
            dveFromDb = getDiskVmElementDao().get(super.getDiskVmElement().getId());
        }
        return dveFromDb;
    }
}
