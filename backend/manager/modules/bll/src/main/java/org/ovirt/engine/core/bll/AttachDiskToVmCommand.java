package org.ovirt.engine.core.bll;

import org.ovirt.engine.core.common.action.UpdateVmDiskParameters;
import org.ovirt.engine.core.common.businessentities.DiskImage;
import org.ovirt.engine.core.common.businessentities.VMStatus;
import org.ovirt.engine.core.common.businessentities.VmDevice;
import org.ovirt.engine.core.common.businessentities.VmDeviceId;
import org.ovirt.engine.core.common.utils.VmDeviceType;
import org.ovirt.engine.core.common.vdscommands.VDSCommandType;
import org.ovirt.engine.core.dal.VdcBllMessages;

public class AttachDiskToVmCommand<T extends UpdateVmDiskParameters> extends AbstractDiskVmCommand<T> {

    private static final long serialVersionUID = -1686587389737849288L;
    private DiskImage diskImage;

    public AttachDiskToVmCommand(T parameters) {
        super(parameters);
        if (getParameters().getDiskInfo().getPlugged() == null) {
            getParameters().getDiskInfo().setPlugged(false);
        }
    }

    @Override
    protected boolean canDoAction() {
        boolean retValue = isVmExist() && isVmUpOrDown();
        diskImage = getDiskImageDao().get(getParameters().getImageId());
        if (retValue && diskImage == null) {
            retValue = false;
            addCanDoActionMessage(VdcBllMessages.ACTION_TYPE_FAILED_VM_IMAGE_DOES_NOT_EXIST);
        }
        retValue =
                retValue && isDiskCanBeAddedToVm(getParameters().getDiskInfo())
                        && isDiskPassPCIAndIDELimit(getParameters().getDiskInfo());
        if (retValue && getVmDeviceDao().exists(new VmDeviceId(getParameters().getImageId(), getVmId()))) {
            retValue = false;
            addCanDoActionMessage(VdcBllMessages.ACTION_TYPE_FAILED_DISK_ALREADY_ATTACHED);
        }
        if (retValue && Boolean.TRUE.equals(getParameters().getDiskInfo().getPlugged())
                && getVm().getstatus() != VMStatus.Down) {
            retValue = isOsSupportingPluggableDisks(getVm()) && isHotPlugEnabled()
                            && isInterfaceSupportedForPlug(getParameters().getDiskInfo());
        }

        if (!retValue) {
            addCanDoActionMessage(VdcBllMessages.VAR__ACTION__ATTACH_ACTION_TO);
            addCanDoActionMessage(VdcBllMessages.VAR__TYPE__VM_DISK);
        }

        return retValue;
    }

    @Override
    protected void ExecuteVmCommand() {
        final VmDevice vmDevice =
                new VmDevice(new VmDeviceId(getParameters().getImageId(), getVmId()),
                        VmDeviceType.DISK.getName(),
                        VmDeviceType.DISK.getName(),
                        "",
                        0,
                        "",
                        true,
                        Boolean.TRUE.equals(getParameters().getDiskInfo().getPlugged()),
                        false);
        diskImage.setinternal_drive_mapping(getParameters().getDiskInfo().getinternal_drive_mapping());
        diskImage.setboot(getParameters().getDiskInfo().getboot());
        diskImage.setdisk_interface(getParameters().getDiskInfo().getdisk_interface());
        diskImage.setvm_guid(getVmId());
        getDiskImageDao().update(diskImage);
        getVmDeviceDao().save(vmDevice);
        if (!Boolean.FALSE.equals(getParameters().getDiskInfo().getPlugged()) && getVm().getstatus() != VMStatus.Down) {
            performPlugCommnad(VDSCommandType.HotPlugDisk, diskImage, vmDevice);
        }
        setSucceeded(true);
    }
}
