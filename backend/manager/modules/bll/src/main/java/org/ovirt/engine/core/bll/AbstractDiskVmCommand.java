package org.ovirt.engine.core.bll;

import java.util.ArrayList;
import java.util.List;

import org.ovirt.engine.core.bll.storage.StorageHelperDirector;
import org.ovirt.engine.core.common.action.VmDiskOperatinParameterBase;
import org.ovirt.engine.core.common.businessentities.Disk;
import org.ovirt.engine.core.common.businessentities.DiskImage;
import org.ovirt.engine.core.common.businessentities.DiskInterface;
import org.ovirt.engine.core.common.businessentities.LunDisk;
import org.ovirt.engine.core.common.businessentities.VMStatus;
import org.ovirt.engine.core.common.businessentities.VmDevice;
import org.ovirt.engine.core.common.businessentities.VmNetworkInterface;
import org.ovirt.engine.core.common.businessentities.Disk.DiskStorageType;
import org.ovirt.engine.core.common.errors.VdcBLLException;
import org.ovirt.engine.core.common.errors.VdcBllErrors;
import org.ovirt.engine.core.common.vdscommands.HotPlugDiskVDSParameters;
import org.ovirt.engine.core.common.vdscommands.VDSCommandType;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dal.VdcBllMessages;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;
import org.ovirt.engine.core.dao.DiskDao;
import org.ovirt.engine.core.dao.DiskImageDAO;
import org.ovirt.engine.core.dao.ImageDao;
import org.ovirt.engine.core.dao.VmNetworkInterfaceDAO;

public abstract class AbstractDiskVmCommand<T extends VmDiskOperatinParameterBase> extends VmCommand<T> {

    private static final long serialVersionUID = -4596432908703489958L;

    public AbstractDiskVmCommand(T parameters) {
        super(parameters);
        if (parameters.getDiskInfo() != null && DiskStorageType.IMAGE == parameters.getDiskInfo().getDiskStorageType()) {
            setQuotaId(((DiskImage) parameters.getDiskInfo()).getQuotaId());
        }
    }

    protected AbstractDiskVmCommand(Guid commandId) {
        super(commandId);
    }

    protected void performPlugCommnad(VDSCommandType commandType,
                Disk disk, VmDevice vmDevice) {
        if (disk.getDiskStorageType() == DiskStorageType.LUN) {
            LunDisk lunDisk = (LunDisk) disk;
            if (commandType == VDSCommandType.HotPlugDisk
                    && !StorageHelperDirector.getInstance().getItem(lunDisk.getLun().getLunType())
                            .ConnectStorageToLunByVdsId(null, getVm().getrun_on_vds().getValue(), lunDisk.getLun())) {
                throw new VdcBLLException(VdcBllErrors.StorageServerConnectionError);
            }
        }
        runVdsCommand(commandType, new HotPlugDiskVDSParameters(getVm().getrun_on_vds().getValue(),
                                 getVm().getId(), disk, vmDevice));
    }

    protected boolean isDiskPassPCIAndIDELimit(Disk diskInfo) {
        List<VmNetworkInterface> vmInterfaces = getVmNetworkInterfaceDao().getAllForVm(getVmId());
        List<Disk> allVmDisks = new ArrayList<Disk>(getVm().getDiskMap().values());
        allVmDisks.add(diskInfo);

        return CheckPCIAndIDELimit(getVm().getnum_of_monitors(),
                                vmInterfaces,
                                allVmDisks,
                                getReturnValue().getCanDoActionMessages());
    }

    protected boolean isDiskCanBeAddedToVm(Disk diskInfo) {
        boolean returnValue = true;
        // update disks from db
        VmHandler.updateDisksFromDb(getVm());
        if (getVm().getDiskMap().containsKey(Integer.toString(diskInfo.getInternalDriveMapping()))) {
            returnValue = false;
            addCanDoActionMessage(VdcBllMessages.ACTION_TYPE_FAILED_DISK_LETTER_ALREADY_IN_USE);
        } else {
            diskInfo.setinternal_drive_mapping(VmHandler.getCorrectDriveForDisk(getVm()));
            if (diskInfo.getinternal_drive_mapping() == null) {
                returnValue = false;
                addCanDoActionMessage(VdcBllMessages.ACTION_TYPE_FAILED_DISK_LIMITATION_EXCEEDED);
            }
        }

        if (returnValue && diskInfo.isBoot()) {
            for (Disk disk : getVm().getDiskMap().values()) {
                if (disk.isBoot()) {
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

    protected boolean isVmExist() {
        if (getVm() == null) {
            addCanDoActionMessage(VdcBllMessages.ACTION_TYPE_FAILED_VM_NOT_FOUND);
            return false;
        }
        return true;
    }

    protected boolean isDiskExist(Disk disk) {
        if (disk == null || !getVmId().equals(disk.getvm_guid())) {
            addCanDoActionMessage(VdcBllMessages.ACTION_TYPE_FAILED_DISK_NOT_EXIST);
            return false;
        }
        return true;
    }


    protected boolean isInterfaceSupportedForPlugUnPlug(Disk disk) {
        if (!DiskInterface.VirtIO.equals(disk.getDiskInterface())) {
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

    protected ImageDao getImageDao() {
        return DbFacade.getInstance().getImageDao();
    }

    protected DiskDao getDiskDao() {
        return DbFacade.getInstance().getDiskDao();
    }

    protected DiskImageDAO getDiskImageDao() {
        return DbFacade.getInstance().getDiskImageDAO();
    }

}
