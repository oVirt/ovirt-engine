package org.ovirt.engine.core.bll;

import java.util.List;

import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.action.UpdateVmDiskParameters;
import org.ovirt.engine.core.common.businessentities.DiskImage;
import org.ovirt.engine.core.common.businessentities.DiskImageBase;
import org.ovirt.engine.core.common.businessentities.DiskInterface;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VMStatus;
import org.ovirt.engine.core.common.businessentities.VmDevice;
import org.ovirt.engine.core.common.businessentities.VmDeviceId;
import org.ovirt.engine.core.common.businessentities.VmNetworkInterface;
import org.ovirt.engine.core.common.config.Config;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.common.vdscommands.HotPlugDiskVDSParameters;
import org.ovirt.engine.core.common.vdscommands.VDSCommandType;
import org.ovirt.engine.core.dal.VdcBllMessages;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;
import org.ovirt.engine.core.dao.DiskImageDAO;
import org.ovirt.engine.core.dao.VmDeviceDAO;
import org.ovirt.engine.core.utils.linq.LinqUtils;
import org.ovirt.engine.core.utils.linq.Predicate;
import org.ovirt.engine.core.utils.transaction.TransactionMethod;
import org.ovirt.engine.core.utils.transaction.TransactionSupport;

@NonTransactiveCommandAttribute
public class UpdateVmDiskCommand<T extends UpdateVmDiskParameters> extends VmCommand<T> {

    private static final long serialVersionUID = 5915267156998835363L;

    public UpdateVmDiskCommand(T parameters) {
        super(parameters);
    }

    private DiskImage _oldDisk;
    private VDSCommandType plugAction;
    private VmDevice oldVmDevice;

    @Override
    protected void ExecuteVmCommand() {
        if (plugAction == null) {
            performRegularDiskUpdate();
        } else {
            performPlugCommnad(plugAction);
        }
    }

    @Override
    protected boolean canDoAction() {
        boolean retValue = true;
        if (getVm() == null) {
            retValue = false;
            addCanDoActionMessage(VdcBllMessages.ACTION_TYPE_FAILED_VM_NOT_FOUND);
        } else {
            _oldDisk = getDiskImageDao().get(getParameters().getImageId());
            if (_oldDisk == null || !_oldDisk.getactive() || !getVmId().equals(_oldDisk.getvm_guid())) {
                retValue = false;
                addCanDoActionMessage(VdcBllMessages.ACTION_TYPE_FAILED_VM_IMAGE_DOES_NOT_EXIST);
            } else {
                oldVmDevice =
                        getVmDeviceDao().get(new VmDeviceId(_oldDisk.getDisk().getId(), getVmId()));
                if (getVm().getstatus() != VMStatus.Up || getParameters().getDiskInfo().getPlugged() == null) {
                    retValue = checkCanPerformRegularUpdate();
                } else {
                    retValue = checkCanPerformPlugUnPlugDisk();
                }
            }
        }
        if (!retValue) {
            addCanDoActionMessage(VdcBllMessages.VAR__ACTION__UPDATE);
            addCanDoActionMessage(VdcBllMessages.VAR__TYPE__VM_DISK);
        }
        return retValue;
    }

    protected DiskImageDAO getDiskImageDao() {
        return DbFacade.getInstance().getDiskImageDAO();
    }

    private boolean checkCanPerformRegularUpdate() {
        boolean retValue = true;
        if (VM.isStatusUpOrPausedOrSuspended(getVm().getstatus())) {
            retValue = false;
            addCanDoActionMessage(VdcBllMessages.ACTION_TYPE_FAILED_VM_STATUS_ILLEGAL);
        } else if (_oldDisk.getdisk_interface() != getParameters().getDiskInfo().getdisk_interface()) {
            List<VmNetworkInterface> allVmInterfaces = DbFacade.getInstance()
                    .getVmNetworkInterfaceDAO().getAllForVm(getVmId());

            List allVmDisks = getDiskImageDao().getAllForVm(getVmId());
            allVmDisks.removeAll(LinqUtils.filter(allVmDisks, new Predicate<DiskImageBase>() {
                @Override
                public boolean eval(DiskImageBase o) {
                    return o.getinternal_drive_mapping().equals(
                            _oldDisk.getinternal_drive_mapping());
                }
            }));
            allVmDisks.add(getParameters().getDiskInfo());
            if (!CheckPCIAndIDELimit(getVm().getnum_of_monitors(),
                    allVmInterfaces,
                    allVmDisks,
                    getReturnValue().getCanDoActionMessages())) {
                retValue = false;
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
        return retValue;
    }

    private boolean checkCanPerformPlugUnPlugDisk() {
        boolean returnValue = true;
        if (!Config.<Boolean> GetValue(ConfigValues.HotPlugEnabled,
                getVds().getvds_group_compatibility_version().getValue())) {
            returnValue = false;
            addCanDoActionMessage(VdcBllMessages.HOT_PLUG_DISK_IS_NOT_SUPPORTED);
        } else if (!isOsSupported(getVm())) {
            returnValue = false;
            addCanDoActionMessage(VdcBllMessages.ACTION_TYPE_FAILED_GUEST_OS_VERSION_IS_NOT_SUPPORTED);
        } else if (!DiskInterface.VirtIO.equals(_oldDisk.getdisk_interface())) {
            returnValue = false;
            addCanDoActionMessage(VdcBllMessages.HOT_PLUG_DISK_IS_NOT_VIRTIO);
        } else if (oldVmDevice.getIsPlugged().equals(getParameters().getDiskInfo().getPlugged())) {
            if (oldVmDevice.getIsPlugged()) {
                returnValue = false;
                addCanDoActionMessage(VdcBllMessages.HOT_PLUG_DISK_IS_NOT_UNPLUGGED);
            } else {
                returnValue = false;
                addCanDoActionMessage(VdcBllMessages.HOT_UNPLUG_DISK_IS_NOT_PLUGGED);
            }
        }
        if (returnValue) {
            plugAction = oldVmDevice.getIsPlugged() ? VDSCommandType.HotUnPlugDisk : VDSCommandType.HotPlugDisk;
        }
        return returnValue;
    }

    protected VmDeviceDAO getVmDeviceDao() {
        return DbFacade.getInstance().getVmDeviceDAO();
    }

    private void performRegularDiskUpdate() {
        TransactionSupport.executeInNewTransaction(new TransactionMethod<Object>() {
            @Override
            public Object runInTransaction() {
                _oldDisk.setboot(getParameters().getDiskInfo().getboot());
                _oldDisk.setdisk_interface(getParameters().getDiskInfo().getdisk_interface());
                _oldDisk.setpropagate_errors(getParameters().getDiskInfo().getpropagate_errors());
                _oldDisk.setwipe_after_delete(getParameters().getDiskInfo().getwipe_after_delete());
                DbFacade.getInstance().getDiskDao().update(_oldDisk.getDisk());
                getDiskImageDao().update(_oldDisk);
                if (getParameters().getDiskInfo().getPlugged() != null
                        && !getParameters().getDiskInfo().getPlugged().equals(oldVmDevice.getIsPlugged())) {
                    oldVmDevice.setIsPlugged(getParameters().getDiskInfo().getPlugged());
                    getVmDeviceDao().update(oldVmDevice);
                }
                setSucceeded(UpdateVmInSpm(getVm().getstorage_pool_id(),
                        new java.util.ArrayList<VM>(java.util.Arrays.asList(new VM[] { getVm() }))));
                return null;
            }
        });
    }

    private void performPlugCommnad(VDSCommandType commandType) {
        Backend
                .getInstance()
                .getResourceManager()
                .RunVdsCommand(
                        commandType,
                        new HotPlugDiskVDSParameters(getVm().getrun_on_vds().getValue(),
                                getVm().getId(),
                                _oldDisk,
                                oldVmDevice));
        oldVmDevice.setIsPlugged(!oldVmDevice.getIsPlugged());
        getVmDeviceDao().update(oldVmDevice);
        setSucceeded(true);
    }

    @Override
    public AuditLogType getAuditLogTypeValue() {
        if (plugAction != null) {
            switch (plugAction) {
            case HotPlugDisk:
                return getSucceeded() ? AuditLogType.USER_HOTPLUG_DISK : AuditLogType.USER_FAILED_HOTPLUG_DISK;
            case HotUnPlugDisk:
                return getSucceeded() ? AuditLogType.USER_HOTUNPLUG_DISK : AuditLogType.USER_FAILED_HOTUNPLUG_DISK;
            }
        }
        return getSucceeded() ? AuditLogType.USER_UPDATE_VM_DISK : AuditLogType.USER_FAILED_UPDATE_VM_DISK;
    }
}
