package org.ovirt.engine.core.bll;

import static org.ovirt.engine.core.common.utils.VmDeviceCommonUtils.SPEC_PARAM_NODE;
import static org.ovirt.engine.core.common.utils.VmDeviceCommonUtils.SPEC_PARAM_SIZE;

import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;

import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.FeatureSupported;
import org.ovirt.engine.core.common.action.HotUnplugMemoryParameters;
import org.ovirt.engine.core.common.businessentities.VMStatus;
import org.ovirt.engine.core.common.businessentities.VmDevice;
import org.ovirt.engine.core.common.businessentities.VmDeviceId;
import org.ovirt.engine.core.common.errors.EngineFault;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.common.locks.LockingGroup;
import org.ovirt.engine.core.common.utils.Pair;
import org.ovirt.engine.core.common.utils.VmDeviceCommonUtils;
import org.ovirt.engine.core.common.vdscommands.VDSCommandType;
import org.ovirt.engine.core.common.vdscommands.VDSReturnValue;
import org.ovirt.engine.core.dao.VmDeviceDao;
import org.ovirt.engine.core.utils.ReplacementUtils;
import org.ovirt.engine.core.vdsbroker.HotUnplugMemoryVDSCommand;

/**
 * It tries to hot unplug memory device of a VM.
 */
public abstract class HotUnplugMemoryCommandBase<P extends HotUnplugMemoryParameters> extends VmCommand<P> {

    private static final String AUDIT_LOG_VAR_VM_NAME = "vmName";
    private static final String AUDIT_LOG_VAR_MEMORY_DEVICE_SIZE_MB = "memoryDeviceSizeMb";
    private static final String AUDIT_LOG_VAR_OLD_MIN_MEMORY_MB = "oldMinMemoryMb";
    private static final String AUDIT_LOG_VAR_NEW_MIN_MEMORY_MB = "newMinMemoryMb";
    private static final String AUDIT_LOG_VAR_ERROR_MESSAGE = "errorMessage";
    private static final String AUDIT_LOG_VAR_DEVICE_ID = "deviceId";

    protected VmDevice deviceToHotUnplug;

    @Inject
    private VmDeviceDao vmDeviceDao;

    public HotUnplugMemoryCommandBase(P parameters, CommandContext cmdContext) {
        super(parameters, cmdContext);
    }

    protected VmDevice getDeviceToHotUnplug() {
        if (deviceToHotUnplug == null) {
            deviceToHotUnplug = vmDeviceDao.get(new VmDeviceId(getParameters().getDeviceId(), getVmId()));
        }
        return deviceToHotUnplug;
    }

    protected int getUnpluggedDeviceSize() {
        return VmDeviceCommonUtils.getSizeOfMemoryDeviceMb(getDeviceToHotUnplug()).get();
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
        if (!VmDeviceCommonUtils.getSpecParamsIntValue(getDeviceToHotUnplug(), SPEC_PARAM_SIZE)
                .isPresent()) {
            return failValidation(
                    EngineMessage.ACTION_TYPE_FAILED_REQUIRED_SPEC_PARAM_IS_MISSING,
                    ReplacementUtils.createSetVariableString("deviceId", getDeviceToHotUnplug().getId()),
                    ReplacementUtils.createSetVariableString("specParamName", SPEC_PARAM_SIZE));
        }
        if (!VmDeviceCommonUtils.getSpecParamsIntValue(getDeviceToHotUnplug(), SPEC_PARAM_NODE)
                .isPresent()) {
            return failValidation(
                    EngineMessage.ACTION_TYPE_FAILED_REQUIRED_SPEC_PARAM_IS_MISSING,
                    ReplacementUtils.createSetVariableString("deviceId", getDeviceToHotUnplug().getId()),
                    ReplacementUtils.createSetVariableString("specParamName", SPEC_PARAM_NODE));
        }
        return true;
    }

    protected VDSReturnValue executeHotUnplug(int minMemoryMb) {
        addCustomValue(AUDIT_LOG_VAR_VM_NAME, getVmName());
        addCustomValue(AUDIT_LOG_VAR_NEW_MIN_MEMORY_MB, String.valueOf(minMemoryMb));
        addCustomValue(AUDIT_LOG_VAR_OLD_MIN_MEMORY_MB, String.valueOf(getVm().getMinAllocatedMem()));
        addCustomValue(AUDIT_LOG_VAR_MEMORY_DEVICE_SIZE_MB, String.valueOf(getUnpluggedDeviceSize()));
        addCustomValue(AUDIT_LOG_VAR_DEVICE_ID, getParameters().getDeviceId().toString());

        final VDSReturnValue vdsReturnValue = runVdsCommand(
                VDSCommandType.HotUnplugMemory,
                new HotUnplugMemoryVDSCommand.Params(
                        getVm().getRunOnVds(),
                        getDeviceToHotUnplug(),
                        minMemoryMb));
        if (vdsReturnValue.getSucceeded()) {
            getVmManager().setDeviceBeingHotUnlugged(getDeviceToHotUnplug().getDeviceId(), true);
        } else {
            addCustomValue(AUDIT_LOG_VAR_ERROR_MESSAGE, vdsReturnValue.getVdsError().getMessage());
            setReturnValueFailure(vdsReturnValue);
        }
        return vdsReturnValue;
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

    private void setReturnValueFailure(VDSReturnValue returnValueFailure) {
        final EngineFault engineFault = new EngineFault();
        engineFault.setError(returnValueFailure.getVdsError().getCode());
        engineFault.setMessage(returnValueFailure.getVdsError().getMessage());
        getReturnValue().setFault(engineFault);
    }
}
