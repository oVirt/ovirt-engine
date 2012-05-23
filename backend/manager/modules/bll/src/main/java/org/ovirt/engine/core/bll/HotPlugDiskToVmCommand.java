package org.ovirt.engine.core.bll;

import org.ovirt.engine.core.bll.utils.VmDeviceUtils;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.action.HotPlugDiskToVmParameters;
import org.ovirt.engine.core.common.businessentities.Disk;
import org.ovirt.engine.core.common.businessentities.VMStatus;
import org.ovirt.engine.core.common.businessentities.VmDevice;
import org.ovirt.engine.core.common.businessentities.VmDeviceId;
import org.ovirt.engine.core.common.vdscommands.VDSCommandType;
import org.ovirt.engine.core.dal.VdcBllMessages;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.CustomLogField;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.CustomLogFields;
import org.ovirt.engine.core.utils.transaction.TransactionMethod;
import org.ovirt.engine.core.utils.transaction.TransactionSupport;

@CustomLogFields({ @CustomLogField("DiskAlias") })
@NonTransactiveCommandAttribute
public class HotPlugDiskToVmCommand<T extends HotPlugDiskToVmParameters> extends AbstractDiskVmCommand<T> {

    private static final long serialVersionUID = 2022232044279588022L;

    protected Disk disk;
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
        disk = getDiskDao().get(getParameters().getDiskId());
        return isVmExist() && isVmUpOrDown() && isDiskExist(disk) && checkCanPerformPlugUnPlugDisk();
    }

    private boolean checkCanPerformPlugUnPlugDisk() {
        boolean returnValue = true;
        if (getVm().getstatus() == VMStatus.Up) {
            setVdsId(getVm().getrun_on_vds().getValue());
            returnValue =
                    isHotPlugSupported() && isOSSupportingHotPlug()
                            && isInterfaceSupportedForPlugUnPlug(disk);
        }
        if (returnValue) {
            oldVmDevice =
                    getVmDeviceDao().get(new VmDeviceId(disk.getId(), getVmId()));
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
            performPlugCommnad(getPlugAction(), disk, oldVmDevice);
        }
        oldVmDevice.setIsPlugged(!oldVmDevice.getIsPlugged());
        TransactionSupport.executeInNewTransaction(new TransactionMethod<Void>() {
            @Override
            public Void runInTransaction() {
                getVmDeviceDao().update(oldVmDevice);
                // update cached image
                VmHandler.updateDisksFromDb(getVm());
                // update vm device boot order
                VmDeviceUtils.updateBootOrderInVmDevice(getVm().getStaticData());
                return null;
            }
        });
        setSucceeded(true);
    }

    @Override
    public AuditLogType getAuditLogTypeValue() {
        return getSucceeded() ? AuditLogType.USER_HOTPLUG_DISK : AuditLogType.USER_FAILED_HOTPLUG_DISK;
    }

    public String getDiskAlias() {
        return disk.getDiskAlias();
    }
}
