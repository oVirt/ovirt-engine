package org.ovirt.engine.core.bll;

import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.action.HotPlugDiskToVmParameters;
import org.ovirt.engine.core.common.businessentities.DiskImage;
import org.ovirt.engine.core.common.businessentities.VMStatus;
import org.ovirt.engine.core.common.businessentities.VmDevice;
import org.ovirt.engine.core.common.businessentities.VmDeviceId;
import org.ovirt.engine.core.common.vdscommands.VDSCommandType;
import org.ovirt.engine.core.dal.VdcBllMessages;

@NonTransactiveCommandAttribute
public class HotPlugDiskToVmCommand<T extends HotPlugDiskToVmParameters> extends AbstractDiskVmCommand<T> {

    private static final long serialVersionUID = 2022232044279588022L;

    protected DiskImage diskImage;
    private VmDevice oldVmDevice;

    public HotPlugDiskToVmCommand(T parameters) {
        super(parameters);
    }

    @Override
    protected void setActionMessageParameters() {
        addCanDoActionMessage(VdcBllMessages.VAR__ACTION__HOT_PLUG);
        addCanDoActionMessage(VdcBllMessages.VAR__TYPE__VM_DISK);
    }

    @Override
    protected boolean canDoAction() {
        diskImage = getDiskImageDao().get(getParameters().getDiskId());
        return isVmExist() && isVmUpOrDown() && isDiskExist(diskImage) && checkCanPerformPlugUnPlugDisk();
    }

    private boolean checkCanPerformPlugUnPlugDisk() {
        boolean returnValue = true;
        if (getVm().getstatus() == VMStatus.Up) {
            setVdsId(getVm().getrun_on_vds().getValue());
            returnValue =
                    isHotPlugEnabled() && isOsSupportingPluggableDisks(getVm())
                            && isInterfaceSupportedForPlugUnPlug(diskImage);
        }
        if (returnValue) {
            oldVmDevice =
                    getVmDeviceDao().get(new VmDeviceId(diskImage.getDisk().getId(), getVmId()));
            if (getPlugAction() == VDSCommandType.HotPlugDisk && oldVmDevice.getIsPlugged()) {
                returnValue = false;
                addCanDoActionMessage(VdcBllMessages.HOT_PLUG_DISK_IS_NOT_UNPLUGGED);
            }
            if (getPlugAction() == VDSCommandType.HotUnPlugDisk && !oldVmDevice.getIsPlugged()) {
                returnValue = false;
                addCanDoActionMessage(VdcBllMessages.HOT_UNPLUG_DISK_IS_NOT_PLUGGED);
            }
        }
        return returnValue;
    }

    protected VDSCommandType getPlugAction() {
        return VDSCommandType.HotPlugDisk;
    }

    @Override
    protected void ExecuteVmCommand() {
        if (getVm().getstatus() == VMStatus.Up) {
            performPlugCommnad(getPlugAction(), diskImage, oldVmDevice);
        }
        oldVmDevice.setIsPlugged(!oldVmDevice.getIsPlugged());
        getVmDeviceDao().update(oldVmDevice);
        setSucceeded(true);
    }

    @Override
    public AuditLogType getAuditLogTypeValue() {
        return getSucceeded() ? AuditLogType.USER_HOTPLUG_DISK : AuditLogType.USER_FAILED_HOTPLUG_DISK;
    }
}
