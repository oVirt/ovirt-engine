package org.ovirt.engine.core.bll;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.ovirt.engine.core.bll.utils.VmDeviceUtils;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.action.HotPlugDiskToVmParameters;
import org.ovirt.engine.core.common.businessentities.Disk;
import org.ovirt.engine.core.common.businessentities.VmDevice;
import org.ovirt.engine.core.common.businessentities.VmDeviceId;
import org.ovirt.engine.core.common.locks.LockingGroup;
import org.ovirt.engine.core.common.utils.Pair;
import org.ovirt.engine.core.common.vdscommands.VDSCommandType;
import org.ovirt.engine.core.dal.VdcBllMessages;
import org.ovirt.engine.core.utils.transaction.TransactionMethod;
import org.ovirt.engine.core.utils.transaction.TransactionSupport;

@NonTransactiveCommandAttribute
@LockIdNameAttribute
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

        return
                isVmExist() &&
                isVmInUpPausedDownStatus() &&
                isDiskExist(disk) &&
                checkCanPerformPlugUnPlugDisk() &&
                isVmNotInPreviewSnapshot();
    }

    private boolean checkCanPerformPlugUnPlugDisk() {
        boolean returnValue = true;
        if (getVm().getStatus().isUpOrPaused()) {
            setVdsId(getVm().getRunOnVds().getValue());
            returnValue =
                    isHotPlugSupported() && isOsSupportingHotPlug()
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
    protected void executeVmCommand() {
        if (getVm().getStatus().isUpOrPaused()) {
            performPlugCommand(getPlugAction(), disk, oldVmDevice);
        }

        //Update boot order and isPlugged fields
        final List<VmDevice> devices = VmDeviceUtils.updateBootOrderInVmDevice(getVm().getStaticData());
        for (VmDevice device:devices) {
            if (device.getDeviceId().equals(oldVmDevice.getDeviceId())) {
                device.setIsPlugged(!oldVmDevice.getIsPlugged());
                break;
            }
        }

        TransactionSupport.executeInNewTransaction(new TransactionMethod<Void>() {
            @Override
            public Void runInTransaction() {
                getVmStaticDAO().incrementDbGeneration(getVm().getId());
                getVmDeviceDao().updateAll("UpdateVmDeviceForHotPlugDisk", devices);
                VmHandler.updateDisksFromDb(getVm());
                return null;
            }
        });
        setSucceeded(true);
    }

    @Override
    protected Map<String, Pair<String, String>> getSharedLocks() {
        return Collections.singletonMap(getVmId().toString(),
                LockMessagesMatchUtil.makeLockingPair(LockingGroup.VM, VdcBllMessages.ACTION_TYPE_FAILED_OBJECT_LOCKED));
    }

    @Override
    public AuditLogType getAuditLogTypeValue() {
        return getSucceeded() ? AuditLogType.USER_HOTPLUG_DISK : AuditLogType.USER_FAILED_HOTPLUG_DISK;
    }

    @Override
    public String getDiskAlias() {
        return disk.getDiskAlias();
    }

}
