package org.ovirt.engine.core.bll;

import org.ovirt.engine.core.common.action.UpdateVmDiskParameters;
import org.ovirt.engine.core.common.businessentities.DiskImage;
import org.ovirt.engine.core.common.businessentities.VMStatus;
import org.ovirt.engine.core.common.businessentities.VmDevice;
import org.ovirt.engine.core.common.businessentities.VmDeviceId;
import org.ovirt.engine.core.common.vdscommands.VDSCommandType;
import org.ovirt.engine.core.dal.VdcBllMessages;

public class DetachDiskFromVmCommand<T extends UpdateVmDiskParameters> extends AbstractDiskVmCommand<T> {

    private static final long serialVersionUID = -4424772106319982885L;
    private DiskImage diskImage;
    private VmDevice vmDevice;

    public DetachDiskFromVmCommand(T parameters) {
        super(parameters);
        if (getParameters().getDiskInfo().getPlugged() == null) {
            getParameters().getDiskInfo().setPlugged(false);
        }
    }

    @Override
    protected boolean canDoAction() {
        boolean retValue = isVmExist();
        if (retValue && getVm().getstatus() != VMStatus.Up && getVm().getstatus() != VMStatus.Down) {
            retValue = false;
            addCanDoActionMessage(VdcBllMessages.ACTION_TYPE_FAILED_VM_STATUS_ILLEGAL);
        }

        if (retValue) {
            diskImage = getDiskImageDao().get(getParameters().getImageId());
            retValue = isDiskExist(diskImage);
        }
        if (retValue) {
            vmDevice = getVmDeviceDao().get(new VmDeviceId(diskImage.getimage_group_id(), getVmId()));
            if (vmDevice == null) {
                retValue = false;
                addCanDoActionMessage(VdcBllMessages.ACTION_TYPE_FAILED_DISK_ALREADY_DETACHED);
            }
        }
        if (retValue && Boolean.TRUE.equals(getParameters().getDiskInfo().getPlugged())
                && getVm().getstatus() != VMStatus.Down) {
            retValue = isHotPlugSupported() && isOSSupportingHotPlug()
                            && isInterfaceSupportedForPlugUnPlug(diskImage);
        }

        if (retValue && Boolean.FALSE.equals(getParameters().getDiskInfo().getPlugged())
                && getVm().getstatus() != VMStatus.Down) {
            addCanDoActionMessage(VdcBllMessages.ACTION_TYPE_FAILED_VM_IS_NOT_DOWN);
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
    protected void ExecuteVmCommand() {
        if (Boolean.TRUE.equals(getParameters().getDiskInfo().getPlugged()) && getVm().getstatus() != VMStatus.Down) {
            performPlugCommnad(VDSCommandType.HotUnPlugDisk, diskImage, vmDevice);
        }
        diskImage.setinternal_drive_mapping(null);
        getDiskImageDao().update(diskImage);
        getVmDeviceDao().remove(vmDevice.getId());
        setSucceeded(true);
    }
}
