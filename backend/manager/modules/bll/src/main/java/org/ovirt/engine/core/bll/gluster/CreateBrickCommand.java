package org.ovirt.engine.core.bll.gluster;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import javax.inject.Inject;

import org.ovirt.engine.core.bll.LockMessagesMatchUtil;
import org.ovirt.engine.core.bll.VdsCommand;
import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.bll.validator.HostValidator;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.action.LockProperties;
import org.ovirt.engine.core.common.action.LockProperties.Scope;
import org.ovirt.engine.core.common.action.gluster.CreateBrickParameters;
import org.ovirt.engine.core.common.businessentities.Cluster;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.gluster.StorageDevice;
import org.ovirt.engine.core.common.constants.gluster.GlusterConstants;
import org.ovirt.engine.core.common.errors.EngineError;
import org.ovirt.engine.core.common.errors.EngineException;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.common.locks.LockingGroup;
import org.ovirt.engine.core.common.utils.Pair;
import org.ovirt.engine.core.common.utils.ansible.AnsibleCommandBuilder;
import org.ovirt.engine.core.common.utils.ansible.AnsibleConstants;
import org.ovirt.engine.core.common.utils.ansible.AnsibleExecutor;
import org.ovirt.engine.core.common.utils.ansible.AnsibleReturnCode;
import org.ovirt.engine.core.common.utils.ansible.AnsibleReturnValue;
import org.ovirt.engine.core.dao.gluster.StorageDeviceDao;
import org.ovirt.engine.core.utils.JsonHelper;

public class CreateBrickCommand extends VdsCommand<CreateBrickParameters> {

    public static final String CREATE_BRICK_LOG_DIRECTORY = "brick-setup";
    private static final long DEFAULT_METADATA_SIZE_MB = 16777;
    private static final long MIN_VG_SIZE = 1048576;
    private static final double MIN_METADATA_PERCENT = 0.005;

    @Inject
    private AnsibleExecutor ansibleExecutor;
    @Inject
    private StorageDeviceDao storageDeviceDao;

    public CreateBrickCommand(CreateBrickParameters parameters, CommandContext cmdContext) {
        super(parameters, cmdContext);
    }

    @Override
    protected void setActionMessageParameters() {
        addValidationMessage(EngineMessage.VAR__ACTION__CREATE);
        addValidationMessage(EngineMessage.VAR__TYPE__GLUSTER_BRICK);
        addValidationMessageVariable("brickName", getParameters().getLvName());
    }

    @Override
    public Map<String, String> getCustomValues() {
        addCustomValue(GlusterConstants.BRICK_NAME, getParameters().getLvName());
        return super.getCustomValues();
    }

    @Override
    protected boolean validate() {
        Cluster cluster = getCluster();
        if (!cluster.supportsGlusterService()) {
            return failValidation(EngineMessage.ACTION_TYPE_FAILED_STORAGE_PROVISIONING_NOT_SUPPORTED_BY_CLUSTER);
        }

        HostValidator validator = HostValidator.createInstance(getVds());
        if (!validate(validator.isUp())) {
            return false;
        }

        String deviceType;
        if (getParameters().getDisks() == null || getParameters().getDisks().isEmpty()) {
            addValidationMessage(EngineMessage.ACTION_TYPE_FAILED_STORAGE_DEVICE_REQUIRED);
            return false;
        } else {
            deviceType = getParameters().getDisks().get(0).getDevType();
        }

        for (StorageDevice device : getParameters().getDisks()) {
            // Check that all the selected devices are of same type. Mixing device types in Brick creation is not
            // allowed
            // for performance reasons.
            if (!Objects.equals(deviceType, device.getDevType())) {
                addValidationMessage(EngineMessage.ACTION_TYPE_FAILED_DIFFERENT_STORAGE_DEVICE_TYPES_SELECTED);
                return false;
            }

            // Ensure that device is not already used by some other brick or LVM.
            if (!device.getCanCreateBrick()) {
                addValidationMessage(EngineMessage.ACTION_TYPE_FAILED_DEVICE_IS_ALREADY_IN_USE);
                addValidationMessageVariable("storageDevice", device.getName());
                return false;
            }
        }

        return true;
    }

    @Override
    protected void executeCommand() {
        try {
            runAnsibleCreateBrickPlaybook();
            setSucceeded(true);
        } catch (IOException | InterruptedException e) {
            setSucceeded(false);
            e.printStackTrace();
        }
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
                            EngineMessage.ACTION_TYPE_FAILED_STORAGE_DEVICE_LOCKED));
        }
        return locksMap;
    }

    @Override
    protected LockProperties applyLockProperties(LockProperties lockProperties) {
        return lockProperties.withScope(Scope.Execution).withNoWait();
    }

    @Override
    public AuditLogType getAuditLogTypeValue() {
        return getSucceeded() ? AuditLogType.CREATE_GLUSTER_BRICK : AuditLogType.CREATE_GLUSTER_BRICK_FAILED;
    }

    private void runAnsibleCreateBrickPlaybook() throws IOException, InterruptedException {

        List<String> disks = new ArrayList<>();
        Double totalSize = 0.0;
        for (StorageDevice device : getParameters().getDisks()) {
            disks.add(device.getDevPath());
            totalSize += device.getSize(); //size is returned in MiB
        }
        Double poolmetadatsize = 0.0;
        if (totalSize < MIN_VG_SIZE) {
            poolmetadatsize = MIN_METADATA_PERCENT * totalSize;
        } else {
            poolmetadatsize =  Double.valueOf(DEFAULT_METADATA_SIZE_MB);
        }
        totalSize = totalSize - poolmetadatsize;

        String ssdDevice = "";
        if (getParameters().getCacheDevice() != null) {
            ssdDevice = getParameters().getCacheDevice().getDevPath();
        }

        int diskCount = getParameters().getNoOfPhysicalDisksInRaidVolume() == null ? 1
                : getParameters().getNoOfPhysicalDisksInRaidVolume();

        AnsibleCommandBuilder command = new AnsibleCommandBuilder()
                .hostnames(getVds().getHostName())
                .variable("ssd", ssdDevice)
                .variable("disks", JsonHelper.objectToJson(disks, false))
                .variable("vgname", "RHGS_vg_" + getParameters().getLvName())
                .variable("size", totalSize.toString())
                .variable("diskcount", diskCount)
                .variable("stripesize", getParameters().getStripeSize())
                .variable("pool_metadatasize", poolmetadatsize)
                .variable("wipefs", "yes")
                .variable("disktype", getParameters().getRaidType().toString())
                .variable("lvname", getParameters().getLvName() + "_lv")
                .variable("cache_lvname", getParameters().getLvName() + "_cache_lv")
                .variable("cache_lvsize", getParameters().getCacheSize() + "G")
                .variable("cachemode", getParameters().getCacheMode())
                .variable("fstype", GlusterConstants.FS_TYPE_XFS)
                .variable("mntpath", getParameters().getMountPoint())
                // /var/log/ovirt-engine/brick-setup/ovirt-gluster-brick-ansible-{hostname}-{correlationid}-{timestamp}.log
                .logFileDirectory(CreateBrickCommand.CREATE_BRICK_LOG_DIRECTORY)
                .logFilePrefix("ovirt-gluster-brick-ansible")
                .logFileName(getVds().getHostName())
                .logFileSuffix(getCorrelationId())
                .playbook(AnsibleConstants.CREATE_BRICK_PLAYBOOK);

         AnsibleReturnValue ansibleReturnValue = ansibleExecutor.runCommand(command);
        if (ansibleReturnValue.getAnsibleReturnCode() != AnsibleReturnCode.OK) {
            log.error("Failed to execute Ansible create brick role. Please check logs for more details: {}",
                    command.logFile());
            throw new EngineException(EngineError.GeneralException,
                    String.format(
                            "Failed to execute Ansible create brick role. Please check logs for more details: %1$s",
                            command.logFile()));
        } else {
            // sync the storage devices, so mount point shows up
            for (StorageDevice storageDevice : getParameters().getDisks()) {
                storageDevice.setMountPoint(getParameters().getMountPoint());
                storageDevice.setGlusterBrick(true);
                saveStorageDevice(storageDevice);
            }
            resetIsFreeFlag(getParameters().getDisks());
        }
    }

    private void resetIsFreeFlag(List<StorageDevice> devices) {
        for (StorageDevice device : devices) {
            storageDeviceDao.updateIsFreeFlag(device.getId(), false);
        }
    }

    private void saveStorageDevice(StorageDevice storageDevice) {
        storageDeviceDao.update(storageDevice);
    }
}
