package org.ovirt.engine.core.bll;

import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;

import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.bll.snapshots.SnapshotVmConfigurationHelper;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.FeatureSupported;
import org.ovirt.engine.core.common.action.HotUnplugMemoryParameters;
import org.ovirt.engine.core.common.businessentities.Snapshot;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VMStatus;
import org.ovirt.engine.core.common.businessentities.VmDevice;
import org.ovirt.engine.core.common.businessentities.VmDeviceId;
import org.ovirt.engine.core.common.businessentities.VmStatic;
import org.ovirt.engine.core.common.errors.EngineFault;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.common.locks.LockingGroup;
import org.ovirt.engine.core.common.utils.Pair;
import org.ovirt.engine.core.common.utils.VmDeviceCommonUtils;
import org.ovirt.engine.core.common.vdscommands.VDSCommandType;
import org.ovirt.engine.core.common.vdscommands.VDSReturnValue;
import org.ovirt.engine.core.dao.SnapshotDao;
import org.ovirt.engine.core.dao.VmDeviceDao;
import org.ovirt.engine.core.utils.ReplacementUtils;
import org.ovirt.engine.core.utils.transaction.TransactionSupport;
import org.ovirt.engine.core.vdsbroker.HotUnplugMemoryVDSCommand;

/**
 * It tries to hot unplug memory device of a VM.
 */
@NonTransactiveCommandAttribute
public class HotUnplugMemoryCommand<P extends HotUnplugMemoryParameters> extends VmCommand<P> {

    private static final String AUDIT_LOG_VAR_VM_NAME = "vmName";
    private static final String AUDIT_LOG_VAR_MEMORY_SIZE_MB = "memorySizeMb";
    private static final String AUDIT_LOG_VAR_ERROR_MESSAGE = "errorMessage";
    private static final String AUDIT_LOG_VAR_DEVICE_ID = "deviceId";

    protected VmDevice deviceToHotUnplug;

    @Inject
    private VmDeviceDao vmDeviceDao;
    @Inject
    private VmHandler vmHandler;
    @Inject
    private SnapshotVmConfigurationHelper snapshotVmConfigurationHelper;
    @Inject
    private SnapshotDao snapshotDao;

    public HotUnplugMemoryCommand(P parameters, CommandContext cmdContext) {
        super(parameters, cmdContext);
    }

    protected VmDevice getDeviceToHotUnplug() {
        if (deviceToHotUnplug == null) {
            deviceToHotUnplug = vmDeviceDao.get(new VmDeviceId(getParameters().getDeviceId(), getVmId()));
        }
        return deviceToHotUnplug;
    }

    protected int getUnpluggedDeviceSize() {
        return VmDeviceCommonUtils.getSizeOfMemoryDeviceMb(getDeviceToHotUnplug());
    }

    @Override
    protected boolean validate() {
        if (!super.validate()) {
            return false;
        }
        if (!FeatureSupported.hotUnplugMemory(getVm().getCompatibilityVersion(), getVm().getClusterArch())) {
            return failValidation(EngineMessage.ACTION_TYPE_FAILED_MEMORY_HOT_UNPLUG_NOT_SUPPORTED_FOR_COMPAT_VERSION_AND_ARCH,
                    ReplacementUtils.createSetVariableString(
                            "compatibilityVersion", getVm().getCompatibilityVersion()),
                    ReplacementUtils.createSetVariableString(
                            "architecture", getVm().getClusterArch()));
        }
        if (getVm().getStatus() != VMStatus.Up) {
            return failValidation(EngineMessage.ACTION_TYPE_FAILED_VM_IS_NOT_UP);
        }
        if (getDeviceToHotUnplug() == null) {
            return failValidation(
                    EngineMessage.ACTION_TYPE_FAILED_VM_MEMORY_DEVICE_DOESNT_EXIST,
                    ReplacementUtils.createSetVariableString(
                            "deviceId", getParameters().getDeviceId()));
        }
        return true;
    }

    @Override
    protected void executeCommand() {
        addCustomValue(AUDIT_LOG_VAR_VM_NAME, getVmName());
        addCustomValue(AUDIT_LOG_VAR_MEMORY_SIZE_MB, String.valueOf(getUnpluggedDeviceSize()));
        addCustomValue(AUDIT_LOG_VAR_DEVICE_ID, getParameters().getDeviceId().toString());

        final VDSReturnValue vdsReturnValue = runVdsCommand(
                VDSCommandType.HotUnplugMemory,
                new HotUnplugMemoryVDSCommand.Params(getVm().getRunOnVds(), getDeviceToHotUnplug()));
        if (!vdsReturnValue.getSucceeded()) {
            addCustomValue(AUDIT_LOG_VAR_ERROR_MESSAGE, vdsReturnValue.getVdsError().getMessage());
            setReturnValueFailure(vdsReturnValue);
            return;
        }

        if (getVm().getMemSizeMb() - getUnpluggedDeviceSize() >= getVm().getMinAllocatedMem()) {
            /*
             * If memory hot unplug fails on guest with a delay (not in synchronous way when calling
             * HotUnplugMemoryVDSCommand), we can get out of sync between what vm devices are reported and
             * value of getVm().getMemSizeMb(). This `if` branch tries to mitigate such situations. It uses
             * the fact that getVm().getMinAllocatedMem() can't be changed when VM is running.
             * Example:
             * | getVm().getMemorySizeMb() | getVm().getMinAllocatedMem() | real VM memory |
             * +---------------------------+------------------------------+----------------+
             * | 8 GB                      | 8 GB                         | 8 BG           |
             * successful hot plug of 2GB
             * | 10 GB                     | 8 GB                         | 10 BG          |
             * asynchronously unsuccessful hot unplug of 2GB
             * | 8 GB                      | 8 GB                         | 10 BG          |
             * second try to hot unplug 2GB, successful, getVm().getMemorySizeMb() not decrease because
             * of this `if`
             * | 8 GB                      | 8 GB                         | 8 BG           |
             */
            updateVm();
        }
        setSucceeded(true);
    }

    private void updateVm() {
        updateNextRunConfiguration();
        updateCurrentConfiguration();
    }

    private void updateCurrentConfiguration() {
        final VmStatic updatedVmStatic = new VmStatic(getVm().getStaticData());
        updatedVmStatic.setMemSizeMb(updatedVmStatic.getMemSizeMb() - getUnpluggedDeviceSize());
        getVmManager().update(updatedVmStatic);
    }

    private void updateNextRunConfiguration() {
        final VmStatic nextRunConfigurationStatic = getNextRunConfiguration();
        if (nextRunConfigurationStatic == null) {
            return;
        }
        final int newMemorySize = nextRunConfigurationStatic.getMemSizeMb() - getUnpluggedDeviceSize();
        if (newMemorySize <= 0) {
            return;
        }
        // Update next run snapshot only if old memory state matches old next run snapshot state
        if (getVm().getMemSizeMb() != nextRunConfigurationStatic.getMemSizeMb()) {
            return;
        }
        nextRunConfigurationStatic.setMemSizeMb(newMemorySize);
        if (newMemorySize < nextRunConfigurationStatic.getMinAllocatedMem()) {
            nextRunConfigurationStatic.setMinAllocatedMem(newMemorySize);
        }
        TransactionSupport.executeInNewTransaction(() -> {
            vmHandler.createNextRunSnapshot(
                    getVm(), nextRunConfigurationStatic, null, getCompensationContext());
            return null;
        });
    }

    private VmStatic getNextRunConfiguration() {
        final Snapshot snapshot = snapshotDao.get(getVmId(), Snapshot.SnapshotType.NEXT_RUN);
        if (snapshot == null) {
            return null;
        }
        final VM vm = snapshotVmConfigurationHelper.getVmFromConfiguration(
                snapshot.getVmConfiguration(), snapshot.getVmId(), snapshot.getId());
        return vm.getStaticData();
    }

    @Override
    protected Map<String, Pair<String, String>> getExclusiveLocks() {
        final Map<String, Pair<String, String>> result = new HashMap<>(super.getExclusiveLocks());
        result.put(getParameters().getDeviceId().toString(),
                LockMessagesMatchUtil.makeLockingPair(
                        LockingGroup.VM_DEVICE,
                        new LockMessage(EngineMessage.ACTION_TYPE_FAILED_MEMORY_DEVICE_IS_BEING_HOT_UNPLUGGED)
                                .with("deviceId", getParameters().getDeviceId().toString())
                                .with("vmId", getParameters().getVmId().toString())));
        return result;
    }

    @Override
    public AuditLogType getAuditLogTypeValue() {
        return getReturnValue().getSucceeded()
                ? AuditLogType.MEMORY_HOT_UNPLUG_SUCCESSFULLY_REQUESTED
                : AuditLogType.MEMORY_HOT_UNPLUG_FAILED;
    }

    @Override
    protected void setActionMessageParameters() {
        addValidationMessage(EngineMessage.VAR__ACTION__HOT_UNPLUG);
        addValidationMessage(EngineMessage.VAR__TYPE__MEMORY_DEVICE);
    }

    public void setReturnValueFailure(VDSReturnValue returnValueFailure) {
        final EngineFault engineFault = new EngineFault();
        engineFault.setError(returnValueFailure.getVdsError().getCode());
        engineFault.setMessage(returnValueFailure.getVdsError().getMessage());
        getReturnValue().setFault(engineFault);
    }
}
