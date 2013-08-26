package org.ovirt.engine.core.bll;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.ovirt.engine.core.bll.utils.VmDeviceUtils;
import org.ovirt.engine.core.bll.validator.DiskValidator;
import org.ovirt.engine.core.bll.validator.StorageDomainValidator;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.action.HotPlugDiskToVmParameters;
import org.ovirt.engine.core.common.businessentities.Disk;
import org.ovirt.engine.core.common.businessentities.Disk.DiskStorageType;
import org.ovirt.engine.core.common.businessentities.DiskImage;
import org.ovirt.engine.core.common.businessentities.StorageDomain;
import org.ovirt.engine.core.common.businessentities.VmDevice;
import org.ovirt.engine.core.common.businessentities.VmDeviceId;
import org.ovirt.engine.core.common.errors.VdcBllMessages;
import org.ovirt.engine.core.common.locks.LockingGroup;
import org.ovirt.engine.core.common.utils.Pair;
import org.ovirt.engine.core.common.vdscommands.VDSCommandType;
import org.ovirt.engine.core.utils.transaction.TransactionMethod;
import org.ovirt.engine.core.utils.transaction.TransactionSupport;

@NonTransactiveCommandAttribute
@LockIdNameAttribute
public class HotPlugDiskToVmCommand<T extends HotPlugDiskToVmParameters> extends AbstractDiskVmCommand<T> {

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
        performDbLoads();

        return
                isVmExist() &&
                isVmInUpPausedDownStatus() &&
                canRunActionOnNonManagedVm() &&
                isDiskExist(getDisk()) &&
                checkCanPerformPlugUnPlugDisk() &&
                isVmNotInPreviewSnapshot() &&
                imageStorageValidation() &&
                virtIoScsiDiskValidation();
    }

    private boolean virtIoScsiDiskValidation() {
        DiskValidator diskValidator = getDiskValidator(disk);
        return validate(diskValidator.isVirtIoScsiValid(getVm()));
    }

    private boolean imageStorageValidation() {
        // If the VM is not an image then it does not use the storage domain.
        // If the VM is not in UP or PAUSED status, then we know that there is no running qemu process,
        // so we don't need to check the storage domain activity.
        if (getDisk().getDiskStorageType() != DiskStorageType.IMAGE || !getVm().getStatus().isRunningOrPaused()) {
            return true;
        }
        DiskImage diskImage = (DiskImage) getDisk();
        StorageDomain storageDomain = getStorageDomainDAO().getForStoragePool(
                diskImage.getStorageIds().get(0), diskImage.getStoragePoolId());
        StorageDomainValidator storageDomainValidator = getStorageDomainValidator(storageDomain);
        return validate(storageDomainValidator.isDomainExistAndActive());
    }

    protected StorageDomainValidator getStorageDomainValidator(StorageDomain storageDomain) {
        StorageDomainValidator storageDomainValidator = new StorageDomainValidator(storageDomain);
        return storageDomainValidator;
    }

    protected void performDbLoads() {
        oldVmDevice =
                getVmDeviceDao().get(new VmDeviceId(getParameters().getDiskId(), getVmId()));
        if (oldVmDevice != null) {
            if (oldVmDevice.getSnapshotId() != null) {
                disk = getDiskImageDao().getDiskSnapshotForVmSnapshot(getParameters().getDiskId(), oldVmDevice.getSnapshotId());
            } else {
                disk = getDiskDao().get(getParameters().getDiskId());
            }
        }
    }

    private boolean checkCanPerformPlugUnPlugDisk() {
        boolean returnValue = true;
        if (getVm().getStatus().isUpOrPaused()) {
            setVdsId(getVm().getRunOnVds());
            if (!canPerformHotPlug() || !isInterfaceSupportedForPlugUnPlug(disk)) {
                return false;
            }
        }

        if (getPlugAction() == VDSCommandType.HotPlugDisk && oldVmDevice.getIsPlugged()) {
            return failCanDoAction(VdcBllMessages.HOT_PLUG_DISK_IS_NOT_UNPLUGGED);
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
            performPlugCommand(getPlugAction(), getDisk(), oldVmDevice);
        }

        // At this point disk is already plugged to or unplugged from VM (depends on the command),
        // so device's 'isPlugged' property should be updated accordingly in DB
        updateDeviceIsPluggedProperty();

        // Now after updating 'isPlugged' property of the plugged/unplugged device, its time to
        // update the boot order for all VM devices. Failure to do that doesn't change the fact that
        // device is already plugged to or unplugged from VM.
        if (disk.isBoot()) {
            updateBootOrder();
        }

        getVmStaticDAO().incrementDbGeneration(getVm().getId());
        setSucceeded(true);
    }

    private void updateDeviceIsPluggedProperty() {
        VmDevice device = getVmDeviceDao().get(oldVmDevice.getId());
        device.setIsPlugged(!oldVmDevice.getIsPlugged());
        getVmDeviceDao().updateHotPlugDisk(device);
    }

    private void updateBootOrder() {
        TransactionSupport.executeInNewTransaction(new TransactionMethod<Void>() {
            @Override
            public Void runInTransaction() {
                VmDeviceUtils.updateBootOrderInVmDeviceAndStoreToDB(getVm().getStaticData());
                return null;
            }
        });
    }

    @Override
    protected Map<String, Pair<String, String>> getSharedLocks() {
        return Collections.singletonMap(getVmId().toString(),
                LockMessagesMatchUtil.makeLockingPair(LockingGroup.VM, VdcBllMessages.ACTION_TYPE_FAILED_OBJECT_LOCKED));
    }

    @Override
    protected Map<String, Pair<String, String>> getExclusiveLocks() {
        Map<String, Pair<String, String>> exclusiveLock = new HashMap<>();
        exclusiveLock.put(getDisk().getId().toString(),
                LockMessagesMatchUtil.makeLockingPair(LockingGroup.DISK,
                        VdcBllMessages.ACTION_TYPE_FAILED_DISKS_LOCKED));
        if (getDisk().isBoot()) {
            exclusiveLock.put(getVmId().toString(),
                LockMessagesMatchUtil.makeLockingPair(LockingGroup.VM_DISK_BOOT,
                        VdcBllMessages.ACTION_TYPE_FAILED_OBJECT_LOCKED));

        }
        return exclusiveLock;
    }

    @Override
    public AuditLogType getAuditLogTypeValue() {
        return getSucceeded() ? AuditLogType.USER_HOTPLUG_DISK : AuditLogType.USER_FAILED_HOTPLUG_DISK;
    }

    @Override
    public String getDiskAlias() {
        return disk.getDiskAlias();
    }

    protected Disk getDisk() {
        if (disk == null) {
            disk = getDiskDao().get(getParameters().getDiskId());
        }
        return disk;
    }
}
