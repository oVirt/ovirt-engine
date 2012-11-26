package org.ovirt.engine.core.bll;

import org.ovirt.engine.core.bll.utils.VmDeviceUtils;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.action.AttachDettachVmDiskParameters;
import org.ovirt.engine.core.common.businessentities.Disk;
import org.ovirt.engine.core.common.businessentities.Disk.DiskStorageType;
import org.ovirt.engine.core.common.businessentities.VMStatus;
import org.ovirt.engine.core.common.businessentities.VmDevice;
import org.ovirt.engine.core.common.businessentities.VmDeviceId;
import org.ovirt.engine.core.common.vdscommands.VDSCommandType;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dal.VdcBllMessages;

public class DetachDiskFromVmCommand<T extends AttachDettachVmDiskParameters> extends AbstractDiskVmCommand<T> {

    private static final long serialVersionUID = -4424772106319982885L;
    private Disk disk;
    private VmDevice vmDevice;

    public DetachDiskFromVmCommand(T parameters) {
        super(parameters);
    }

    @Override
    protected boolean canDoAction() {
        boolean retValue = isVmExist();
        if (retValue && getVm().getStatus() != VMStatus.Up && getVm().getStatus() != VMStatus.Down) {
            retValue = false;
            addCanDoActionMessage(VdcBllMessages.ACTION_TYPE_FAILED_VM_STATUS_ILLEGAL);
        }

        if (retValue) {
            disk = getDiskDao().get((Guid)getParameters().getEntityId());
            retValue = isDiskExist(disk);
        }
        if (retValue) {
            vmDevice = getVmDeviceDao().get(new VmDeviceId(disk.getId(), getVmId()));
            if (vmDevice == null) {
                retValue = false;
                addCanDoActionMessage(VdcBllMessages.ACTION_TYPE_FAILED_DISK_ALREADY_DETACHED);
            }
        }
        if (retValue && Boolean.TRUE.equals(getParameters().isPlugUnPlug())
                && getVm().getStatus() != VMStatus.Down) {
            retValue = isHotPlugSupported() && isOsSupportingHotPlug()
                            && isInterfaceSupportedForPlugUnPlug(disk);
        }

        if (retValue && Boolean.FALSE.equals(getParameters().isPlugUnPlug())
                && getVm().getStatus() != VMStatus.Down) {
            addCanDoActionMessage(VdcBllMessages.ACTION_TYPE_FAILED_VM_IS_NOT_DOWN);
            retValue = false;
        }

        // Check if disk has no snapshots before detaching it.
        if (retValue && DiskStorageType.IMAGE == disk.getDiskStorageType()
                && getDiskImageDao().getAllSnapshotsForImageGroup(disk.getId()).size() > 1) {
            addCanDoActionMessage(VdcBllMessages.ERROR_CANNOT_DETACH_DISK_WITH_SNAPSHOT);
            retValue = false;
        }
        return retValue;
    }

    @Override
    protected void setActionMessageParameters() {
        addCanDoActionMessage(VdcBllMessages.VAR__ACTION__DETACH_ACTION_TO);
        addCanDoActionMessage(VdcBllMessages.VAR__TYPE__VM_DISK);
    }

    @Override
    protected void executeVmCommand() {
        if (diskShouldBeUnPlugged()) {
            performPlugCommnad(VDSCommandType.HotUnPlugDisk, disk, vmDevice);
        }
        getVmDeviceDao().remove(vmDevice.getId());
        // update cached image
        VmHandler.updateDisksFromDb(getVm());
        // update vm device boot order
        VmDeviceUtils.updateBootOrderInVmDeviceAndStoreToDB(getVm().getStaticData());
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
}
