package org.ovirt.engine.core.bll;

import java.util.Collections;
import java.util.List;

import org.ovirt.engine.core.common.action.VmDiskOperatinParameterBase;
import org.ovirt.engine.core.common.businessentities.DiskImage;
import org.ovirt.engine.core.common.businessentities.DiskImageBase;
import org.ovirt.engine.core.common.businessentities.DiskInterface;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VMStatus;
import org.ovirt.engine.core.common.businessentities.VmDevice;
import org.ovirt.engine.core.common.businessentities.VmNetworkInterface;
import org.ovirt.engine.core.common.config.Config;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.common.vdscommands.HotPlugDiskVDSParameters;
import org.ovirt.engine.core.common.vdscommands.VDSCommandType;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.StringHelper;
import org.ovirt.engine.core.dal.VdcBllMessages;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;
import org.ovirt.engine.core.dao.DiskImageDAO;
import org.ovirt.engine.core.dao.VmDeviceDAO;
import org.ovirt.engine.core.dao.VmNetworkInterfaceDAO;
import org.ovirt.engine.core.utils.linq.Function;
import org.ovirt.engine.core.utils.linq.LinqUtils;

public abstract class AbstractDiskVmCommand<T extends VmDiskOperatinParameterBase> extends VmCommand<T> {

    private static final long serialVersionUID = -4596432908703489958L;
    private static final String[] oses = Config.<String> GetValue(ConfigValues.HotPlugSupportedOsList).split(",");

    public AbstractDiskVmCommand(T parameters) {
        super(parameters);
    }

    protected AbstractDiskVmCommand(Guid commandId) {
        super(commandId);
    }

    protected void performPlugCommnad(VDSCommandType commandType,
                DiskImage diskImage, VmDevice vmDevice) {
        runVdsCommand(commandType, new HotPlugDiskVDSParameters(getVm().getrun_on_vds().getValue(),
                                 getVm().getId(), diskImage, vmDevice));
        setSucceeded(true);
    }

    protected boolean isDiskPassPCIAndIDELimit(DiskImageBase diskInfo) {
        List<VmNetworkInterface> vmInterfaces = getVmNetworkInterfaceDao().getAllForVm(getVmId());
        List<DiskImageBase> allVmDisks = LinqUtils.foreach(getVm().getDiskMap().values(),
                    new Function<DiskImage, DiskImageBase>() {
                        @Override
                        public DiskImageBase eval(DiskImage diskImage) {
                            return diskImage;
                        }
                    });
        allVmDisks.add(diskInfo);

        return CheckPCIAndIDELimit(getVm().getnum_of_monitors(),
                                vmInterfaces,
                                allVmDisks,
                                getReturnValue().getCanDoActionMessages());
    }

    protected boolean isDiskCanBeAddedToVm(DiskImageBase diskInfo) {
        boolean returnValue = true;
        // update disks from db
        VmHandler.updateDisksFromDb(getVm());
        if (!StringHelper.isNullOrEmpty(diskInfo.getinternal_drive_mapping())
                && getVm().getDiskMap().containsKey(diskInfo.getinternal_drive_mapping())) {
            returnValue = false;
            addCanDoActionMessage(VdcBllMessages.ACTION_TYPE_FAILED_DISK_LETTER_ALREADY_IN_USE);
        } else {
            diskInfo.setinternal_drive_mapping(getCorrectDriveForDisk());
            if (diskInfo.getinternal_drive_mapping() == null) {
                returnValue = false;
                addCanDoActionMessage(VdcBllMessages.ACTION_TYPE_FAILED_DISK_LIMITATION_EXCEEDED);
            }
        }

        if (returnValue && diskInfo.getboot()) {
            for (DiskImageBase disk : getVm().getDiskMap().values()) {
                if (disk.getboot()) {
                    returnValue = false;
                    addCanDoActionMessage(VdcBllMessages.ACTION_TYPE_FAILED_DISK_BOOT_IN_USE);
                    getReturnValue().getCanDoActionMessages().add(
                            String.format("$DiskName %1$s", disk.getinternal_drive_mapping()));
                    break;
                }
            }
        }
        return returnValue;
    }

    protected boolean isVmUpOrDown() {
        if (getVm().getstatus() != VMStatus.Up && getVm().getstatus() != VMStatus.Down) {
            addCanDoActionMessage(VdcBllMessages.ACTION_TYPE_FAILED_VM_STATUS_ILLEGAL);
            return false;
        }
        return true;
    }

    protected boolean isHotPlugEnabled() {
        if (!Config.<Boolean> GetValue(ConfigValues.HotPlugEnabled,
                getVds().getvds_group_compatibility_version().getValue())) {
            addCanDoActionMessage(VdcBllMessages.HOT_PLUG_DISK_IS_NOT_SUPPORTED);
            return false;
        }
        return true;
    }

    protected boolean isVmExist() {
        if (getVm() == null) {
            addCanDoActionMessage(VdcBllMessages.ACTION_TYPE_FAILED_VM_NOT_FOUND);
            return false;
        }
        return true;
    }

    protected boolean isDiskExist(DiskImage disk) {
        if (disk == null || !disk.getactive() || !getVmId().equals(disk.getvm_guid())) {
            addCanDoActionMessage(VdcBllMessages.ACTION_TYPE_FAILED_VM_IMAGE_DOES_NOT_EXIST);
            return false;
        }
        return true;
    }

    /**
     * The following method should check if os of guest is supported for hot plug/unplug operation
     * @param vm
     * @return
     */
    protected boolean isOsSupportingPluggableDisks(VM vm) {
        for (String os : oses) {
            if (os.equalsIgnoreCase(vm.getos().name())) {
                return true;
            }
        }
        addCanDoActionMessage(VdcBllMessages.ACTION_TYPE_FAILED_GUEST_OS_VERSION_IS_NOT_SUPPORTED);
        return false;
    }

    protected boolean isInterfaceSupportedForPlugUnPlug(DiskImageBase diskImage) {
        if (!DiskInterface.VirtIO.equals(diskImage.getdisk_interface())) {
            addCanDoActionMessage(VdcBllMessages.HOT_PLUG_DISK_IS_NOT_VIRTIO);
            return false;
        }
        return true;
    }

    /**
     * @return The VmNetworkInterfaceDAO
     */
    protected VmNetworkInterfaceDAO getVmNetworkInterfaceDao() {
        return DbFacade.getInstance().getVmNetworkInterfaceDAO();
    }

    protected DiskImageDAO getDiskImageDao() {
        return DbFacade.getInstance().getDiskImageDAO();
    }

    protected VmDeviceDAO getVmDeviceDao() {
        return DbFacade.getInstance().getVmDeviceDAO();
    }

}
