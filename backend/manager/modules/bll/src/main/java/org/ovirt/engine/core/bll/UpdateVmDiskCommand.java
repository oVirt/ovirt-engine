package org.ovirt.engine.core.bll;

import java.util.List;

import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.action.UpdateVmDiskParameters;
import org.ovirt.engine.core.common.businessentities.DiskImage;
import org.ovirt.engine.core.common.businessentities.DiskImageBase;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VmNetworkInterface;
import org.ovirt.engine.core.common.businessentities.image_vm_map;
import org.ovirt.engine.core.dal.VdcBllMessages;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;
import org.ovirt.engine.core.utils.linq.LinqUtils;
import org.ovirt.engine.core.utils.linq.Predicate;

public class UpdateVmDiskCommand<T extends UpdateVmDiskParameters> extends VmCommand<T> {
    public UpdateVmDiskCommand(T parameters) {
        super(parameters);
        setVmId(parameters.getVmId());
    }

    private DiskImage _oldDisk;

    @Override
    protected void ExecuteVmCommand() {
        _oldDisk.setboot(getParameters().getDiskInfo().getboot());
        _oldDisk.setdisk_interface(getParameters().getDiskInfo().getdisk_interface());
        _oldDisk.setpropagate_errors(getParameters().getDiskInfo().getpropagate_errors());
        _oldDisk.setwipe_after_delete(getParameters().getDiskInfo().getwipe_after_delete());
        DbFacade.getInstance().getDiskDao().update(_oldDisk.getDisk());
        DbFacade.getInstance().getDiskImageDAO().update(_oldDisk);
        setSucceeded(UpdateVmInSpm(getVm().getstorage_pool_id(),
                new java.util.ArrayList<VM>(java.util.Arrays.asList(new VM[] { getVm() }))));
    }

    @Override
    protected boolean canDoAction() {
        boolean retValue = true;
        if (getVm() == null) {
            retValue = false;
            addCanDoActionMessage(VdcBllMessages.ACTION_TYPE_FAILED_VM_NOT_FOUND);
        } else if (VM.isStatusUpOrPausedOrSuspended(getVm().getstatus())) {
            retValue = false;
            addCanDoActionMessage(VdcBllMessages.ACTION_TYPE_FAILED_VM_STATUS_ILLEGAL);
        } else {
            image_vm_map map = DbFacade.getInstance().getImageVmMapDAO().getByImageId(getParameters().getImageId());
            _oldDisk = DbFacade.getInstance().getDiskImageDAO().get(getParameters().getImageId());
            if (map == null || !getVmId().equals(map.getvm_id()) || _oldDisk == null) {
                retValue = false;
                addCanDoActionMessage(VdcBllMessages.ACTION_TYPE_FAILED_VM_IMAGE_DOES_NOT_EXIST);
            } else if (_oldDisk.getdisk_interface() != getParameters().getDiskInfo().getdisk_interface()) {
                List<VmNetworkInterface> allVmInterfaces = DbFacade.getInstance()
                        .getVmNetworkInterfaceDAO().getAllForVm(getVmId());
                // LINQ 29456
                // List<DiskImageBase> allVmDisks =
                // DbFacade.Instance.GetImagesByVmGuid(VmId).Select(a =>
                // (DiskImageBase)a).ToList();
                // allVmDisks.RemoveAll(a => a.internal_drive_mapping ==
                // _oldDisk.internal_drive_mapping);
                // allVmDisks.Add(UpdateParameters.DiskInfo);
                // if (!CheckPCIAndIDELimit(Vm.num_of_monitors, allVmInterfaces,
                // allVmDisks))

                List allVmDisks = DbFacade.getInstance().getDiskImageDAO().getAllForVm(getVmId());
                allVmDisks.removeAll(LinqUtils.filter(allVmDisks, new Predicate() {
                    @Override
                    public boolean eval(Object o) {
                        return ((DiskImageBase) o).getinternal_drive_mapping().equals(
                                _oldDisk.getinternal_drive_mapping());
                    }
                }));
                allVmDisks.add(getParameters().getDiskInfo());
                if (!CheckPCIAndIDELimit(getVm().getnum_of_monitors(), allVmInterfaces, allVmDisks, getReturnValue().getCanDoActionMessages())) {
                    retValue = false;
                }
            }
        }
        if (retValue && getParameters().getDiskInfo().getboot()) {
            VmHandler.updateDisksFromDb(getVm());
            for (DiskImage disk : getVm().getDiskMap().values()) {
                if (disk.getboot() && !getParameters().getImageId().equals(disk.getId())) {
                    retValue = false;
                    addCanDoActionMessage(VdcBllMessages.ACTION_TYPE_FAILED_DISK_BOOT_IN_USE);
                    getReturnValue().getCanDoActionMessages().add(
                            String.format("$DiskName %1$s", disk.getinternal_drive_mapping()));
                    break;
                }
            }
        }
        if (!retValue) {
            addCanDoActionMessage(VdcBllMessages.VAR__ACTION__UPDATE);
            addCanDoActionMessage(VdcBllMessages.VAR__TYPE__VM_DISK);
        }
        return retValue;
    }

    @Override
    public AuditLogType getAuditLogTypeValue() {
        return getSucceeded() ? AuditLogType.USER_UPDATE_VM_DISK : AuditLogType.USER_FAILED_UPDATE_VM_DISK;
    }
}
