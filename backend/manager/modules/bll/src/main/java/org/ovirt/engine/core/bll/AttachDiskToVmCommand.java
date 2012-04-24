package org.ovirt.engine.core.bll;

import java.util.ArrayList;
import java.util.List;

import org.ovirt.engine.core.bll.utils.VmDeviceUtils;
import org.ovirt.engine.core.common.PermissionSubject;
import org.ovirt.engine.core.common.VdcObjectType;
import org.ovirt.engine.core.common.action.UpdateVmDiskParameters;
import org.ovirt.engine.core.common.businessentities.ActionGroup;
import org.ovirt.engine.core.common.businessentities.Disk;
import org.ovirt.engine.core.common.businessentities.Disk.DiskStorageType;
import org.ovirt.engine.core.common.businessentities.DiskImage;
import org.ovirt.engine.core.common.businessentities.VMStatus;
import org.ovirt.engine.core.common.businessentities.VmDevice;
import org.ovirt.engine.core.common.businessentities.VmDeviceId;
import org.ovirt.engine.core.common.utils.VmDeviceType;
import org.ovirt.engine.core.common.vdscommands.VDSCommandType;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dal.VdcBllMessages;

public class AttachDiskToVmCommand<T extends UpdateVmDiskParameters> extends AbstractDiskVmCommand<T> {

    private static final long serialVersionUID = -1686587389737849288L;
    private List<PermissionSubject> permsList = null;
    private Disk disk;

    public AttachDiskToVmCommand(T parameters) {
        super(parameters);
        if (getParameters().getDiskInfo().getPlugged() == null) {
            getParameters().getDiskInfo().setPlugged(false);
        }
        disk = getDiskDao().get(getParameters().getDiskId());
    }

    @Override
    protected boolean canDoAction() {
        boolean retValue = isVmExist() && isVmUpOrDown();
        if (retValue && disk == null) {
            retValue = false;
            addCanDoActionMessage(VdcBllMessages.ACTION_TYPE_FAILED_VM_IMAGE_DOES_NOT_EXIST);
        }
        retValue =
                retValue && isDiskCanBeAddedToVm(getParameters().getDiskInfo())
                        && isDiskPassPCIAndIDELimit(getParameters().getDiskInfo());
        if (retValue && getVmDeviceDao().exists(new VmDeviceId(disk.getId(), getVmId()))) {
            retValue = false;
            addCanDoActionMessage(VdcBllMessages.ACTION_TYPE_FAILED_DISK_ALREADY_ATTACHED);
        }
        if (retValue && Boolean.TRUE.equals(getParameters().getDiskInfo().getPlugged())
                && getVm().getstatus() != VMStatus.Down) {
            retValue = isOSSupportingHotPlug() && isHotPlugSupported()
                            && isInterfaceSupportedForPlugUnPlug(getParameters().getDiskInfo());
        }

        return retValue;
    }

    @Override
    protected void ExecuteVmCommand() {
        final VmDevice vmDevice =
                new VmDevice(new VmDeviceId(disk.getId(), getVmId()),
                        VmDeviceType.DISK.getName(),
                        VmDeviceType.DISK.getName(),
                        "",
                        0,
                        null,
                        true,
                        Boolean.TRUE.equals(getParameters().getDiskInfo().getPlugged()),
                        false);
        disk.setInternalDriveMapping(getParameters().getDiskInfo().getInternalDriveMapping());
        disk.setboot(getParameters().getDiskInfo().getboot());
        disk.setDiskInterface(getParameters().getDiskInfo().getDiskInterface());
        if (DiskStorageType.IMAGE.equals(disk.getDiskStorageType())) {
            getImageDao().update(((DiskImage) disk).getImage());
        }
        getVmDeviceDao().save(vmDevice);
        // update cached image
        List<Disk> imageList = new ArrayList<Disk>();
        imageList.add(disk);
        VmHandler.updateDisksForVm(getVm(), imageList);
        // update vm device boot order
        VmDeviceUtils.updateBootOrderInVmDevice(getVm().getStaticData());
        if (Boolean.TRUE.equals(getParameters().getDiskInfo().getPlugged()) && getVm().getstatus() != VMStatus.Down) {
            performPlugCommnad(VDSCommandType.HotPlugDisk, disk, vmDevice);
        }
        setSucceeded(true);
    }

    @Override
    protected void setActionMessageParameters() {
        addCanDoActionMessage(VdcBllMessages.VAR__ACTION__ATTACH_ACTION_TO);
        addCanDoActionMessage(VdcBllMessages.VAR__TYPE__VM_DISK);
    }

    @Override
    public List<PermissionSubject> getPermissionCheckSubjects() {
        if (permsList == null) {
            permsList = super.getPermissionCheckSubjects();
            Guid diskId = disk == null ? null : disk.getId();
            permsList.add(new PermissionSubject(diskId, VdcObjectType.Disk, ActionGroup.ATTACH_DISK));
        }
        return permsList;
    }

}
