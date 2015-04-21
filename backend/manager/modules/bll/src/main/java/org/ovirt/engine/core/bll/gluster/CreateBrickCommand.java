package org.ovirt.engine.core.bll.gluster;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.ovirt.engine.core.bll.LockMessagesMatchUtil;
import org.ovirt.engine.core.bll.VdsCommand;
import org.ovirt.engine.core.bll.VdsValidator;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.action.LockProperties;
import org.ovirt.engine.core.common.action.LockProperties.Scope;
import org.ovirt.engine.core.common.action.gluster.CreateBrickParameters;
import org.ovirt.engine.core.common.businessentities.RaidType;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VDSGroup;
import org.ovirt.engine.core.common.businessentities.gluster.StorageDevice;
import org.ovirt.engine.core.common.constants.gluster.GlusterConstants;
import org.ovirt.engine.core.common.errors.VdcBllMessages;
import org.ovirt.engine.core.common.gluster.GlusterFeatureSupported;
import org.ovirt.engine.core.common.locks.LockingGroup;
import org.ovirt.engine.core.common.utils.ObjectUtils;
import org.ovirt.engine.core.common.utils.Pair;
import org.ovirt.engine.core.common.vdscommands.VDSCommandType;
import org.ovirt.engine.core.common.vdscommands.VDSReturnValue;
import org.ovirt.engine.core.common.vdscommands.gluster.CreateBrickVDSParameters;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;

public class CreateBrickCommand extends VdsCommand<CreateBrickParameters> {

    public CreateBrickCommand(CreateBrickParameters parameters) {
        super(parameters);
    }

    @Override
    protected void setActionMessageParameters() {
        addCanDoActionMessage(VdcBllMessages.VAR__ACTION__CREATE);
        addCanDoActionMessage(VdcBllMessages.VAR__TYPE__GLUSTER_BRICK);
        addCanDoActionMessageVariable("brickName", getParameters().getLvName());
    }

    @Override
    public Map<String, String> getCustomValues() {
        addCustomValue(GlusterConstants.BRICK_NAME, getParameters().getLvName());
        return super.getCustomValues();
    }

    @Override
    protected boolean canDoAction() {
        VDSGroup cluster = getVdsGroup();
        if (!cluster.supportsGlusterService()
                || !GlusterFeatureSupported.glusterBrickProvisioning(cluster.getCompatibilityVersion())) {
            return failCanDoAction(VdcBllMessages.ACTION_TYPE_FAILED_STORAGE_PROVISIONING_NOT_SUPPORTED_BY_CLUSTER);
        }

        VdsValidator validator = new VdsValidator(getVds());
        if (!validate(validator.isUp())) {
            return false;
        }

        String deviceType;
        if (getParameters().getDisks() == null || getParameters().getDisks().isEmpty()) {
            addCanDoActionMessage(VdcBllMessages.ACTION_TYPE_FAILED_STORAGE_DEVICE_REQUIRED);
            return false;
        } else {
            deviceType = getParameters().getDisks().get(0).getDevType();
        }

        for (StorageDevice device : getParameters().getDisks()) {
            // Check that all the selected devices are of same type. Mixing device types in Brick creation is not
            // allowed
            // for performance reasons.
            if (!ObjectUtils.objectsEqual(deviceType, device.getDevType())) {
                addCanDoActionMessage(VdcBllMessages.ACTION_TYPE_FAILED_DIFFERENT_STORAGE_DEVICE_TYPES_SELECTED);
                return false;
            }

            // Ensure that device is not already used by some other brick or LVM.
            if (!device.getCanCreateBrick()) {
                addCanDoActionMessage(VdcBllMessages.ACTION_TYPE_FAILED_DEVICE_IS_ALREADY_IN_USE);
                addCanDoActionMessageVariable("storageDevice", device.getName());
                return false;
            }
        }

        return true;
    }

    @Override
    protected void executeCommand() {

        Map<String, Object> raidParams = new HashMap<>();
        if (!getParameters().getRaidType().equals(RaidType.None)
                && !getParameters().getRaidType().equals(RaidType.Raid0)) {
            raidParams.put("type", getParameters().getRaidType().getValue()); //$NON-NLS-1$
            raidParams.put("pdCount", getParameters().getNoOfPhysicalDisksInRaidVolume()); //$NON-NLS-1$
            raidParams.put("stripSize", getParameters().getStripeSize()); //$NON-NLS-1$
        }

        VDSReturnValue returnValue = runVdsCommand(
                VDSCommandType.CreateBrick,
                new CreateBrickVDSParameters(getVdsId(),
                        getParameters().getLvName(),
                        getParameters().getMountPoint(),
                        raidParams,
                        GlusterConstants.FS_TYPE_XFS,
                        getParameters().getDisks()));
        setSucceeded(returnValue.getSucceeded());
        if (getSucceeded()) {
            StorageDevice storageDevice = (StorageDevice) returnValue.getReturnValue();
            storageDevice.setMountPoint(getParameters().getMountPoint());
            storageDevice.setGlusterBrick(true);
            saveStoageDevice(storageDevice);
            // Reset the isFree flag on all the devices which are used for brick creation
            resetIsFreeFlag(getParameters().getDisks());
        } else {
            handleVdsError(returnValue);
        }
    }

    private void resetIsFreeFlag(List<StorageDevice> devices) {
        for (StorageDevice device : devices) {
            DbFacade.getInstance().getStorageDeviceDao().updateIsFreeFlag(device.getId(), false);
        }
    }

    private void saveStoageDevice(StorageDevice storageDevice) {
        DbFacade.getInstance().getStorageDeviceDao().save(storageDevice);
    }

    @Override
    protected VDS getVds() {
        return super.getVds();
    }

    @Override
    protected Map<String, Pair<String, String>> getExclusiveLocks() {
        Map<String, Pair<String, String>> locksMap = new HashMap<>();
        for (StorageDevice disk : getParameters().getDisks()) {
            locksMap.put(disk.getId().toString(),
                    LockMessagesMatchUtil.makeLockingPair(LockingGroup.HOST_STORAGE_DEVICES,
                            VdcBllMessages.ACTION_TYPE_FAILED_STORAGE_DEVICE_LOCKED));
        }
        return locksMap;
    }

    @Override
    protected LockProperties applyLockProperties(LockProperties lockProperties) {
        return lockProperties.withScope(Scope.Execution).withWait(false);
    }

    @Override
    public AuditLogType getAuditLogTypeValue() {
        return getSucceeded() ? AuditLogType.CREATE_GLUSTER_BRICK : AuditLogType.CREATE_GLUSTER_BRICK_FAILED;
    }
}
