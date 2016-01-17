package org.ovirt.engine.core.bll;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.bll.quota.QuotaClusterConsumptionParameter;
import org.ovirt.engine.core.bll.quota.QuotaConsumptionParameter;
import org.ovirt.engine.core.bll.quota.QuotaVdsDependent;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.FeatureSupported;
import org.ovirt.engine.core.common.action.HotSetAmountOfMemoryParameters;
import org.ovirt.engine.core.common.action.PlugAction;
import org.ovirt.engine.core.common.businessentities.VMStatus;
import org.ovirt.engine.core.common.businessentities.VmDevice;
import org.ovirt.engine.core.common.businessentities.VmDeviceGeneralType;
import org.ovirt.engine.core.common.businessentities.VmDeviceId;
import org.ovirt.engine.core.common.config.Config;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.common.errors.EngineFault;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.common.utils.VmDeviceType;
import org.ovirt.engine.core.common.vdscommands.VDSCommandType;
import org.ovirt.engine.core.common.vdscommands.VDSReturnValue;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.vdsbroker.SetAmountOfMemoryVDSCommand;

@NonTransactiveCommandAttribute
public class HotSetAmountOfMemoryCommand<T extends HotSetAmountOfMemoryParameters> extends VmManagementCommandBase<T> implements QuotaVdsDependent {

    public static final String LOGABLE_FIELD_NEW_MEMORY = "newMem";
    public static final String LOGABLE_FIELD_PREVIOUS_MEMORY = "previousMem";
    public static final String LOGABLE_FIELD_ERROR_MESSAGE = "ErrorMessage";
    public static final String DEVICE_SIZE_FIELD_KEY = "size";
    public static final String DEVICE_NODE_FIELD_KEY = "node";

    private int memoryToConsume;

    public HotSetAmountOfMemoryCommand(T parameters, CommandContext commandContext) {
        super(parameters, commandContext);
        memoryToConsume = getParameters().getVm().getMemSizeMb() - getVm().getMemSizeMb();
    }

    @Override
    protected void setActionMessageParameters() {
        addValidationMessage(EngineMessage.VAR__ACTION__HOT_SET_MEMORY);
        addValidationMessage(EngineMessage.VAR__TYPE__VM);
        addValidationMessageVariable("clusterVersion", getVm().getCompatibilityVersion());
        addValidationMessageVariable("architecture", getVm().getClusterArch());
    }

    @Override
    protected boolean validate() {
        if (getVm() == null) {
            return failValidation(EngineMessage.ACTION_TYPE_FAILED_VM_NOT_FOUND);
        }

        if (getVm().getStatus() != VMStatus.Up) {
            return failVmStatusIllegal();
        }

        if (getParameters().getPlugAction() == PlugAction.PLUG) {
            if (!FeatureSupported.hotPlugMemory(getVm().getCompatibilityVersion(), getVm().getClusterArch())) {
                return failValidation(EngineMessage.HOT_PLUG_MEMORY_IS_NOT_SUPPORTED);
            }
            // check max slots
            List<VmDevice> memDevices = getVmDeviceDao().getVmDeviceByVmIdAndType(getVmId(), VmDeviceGeneralType.MEMORY);
            if (memDevices.size() == Config.<Integer>getValue(ConfigValues.MaxMemorySlots)) {
                return failValidation(EngineMessage.ACTION_TYPE_FAILED_NO_MORE_MEMORY_SLOTS,
                        "$maxMemSlots " + Config.getValue(ConfigValues.MaxMemorySlots).toString());
            }
            // plugged memory should be multiply of 256mb
            if (memoryToConsume > 0 && memoryToConsume % Config.<Integer>getValue(ConfigValues.HotPlugMemoryMultiplicationSizeMb) != 0) {
                return failValidation(EngineMessage.ACTION_TYPE_FAILED_MEMORY_MUST_BE_MULTIPLICATION,
                        "$multiplicationSize " + Config.getValue(ConfigValues.HotPlugMemoryMultiplicationSizeMb).toString());
            }
        } else if (!FeatureSupported.hotUnplugMemory(getVm().getCompatibilityVersion(), getVm().getClusterArch())) {
            return failValidation(EngineMessage.HOT_UNPLUG_MEMORY_IS_NOT_SUPPORTED);
        }

        return true;
    }

    @Override
    protected void executeCommand() {
        VDSReturnValue vdsReturnValue = runVdsCommand(VDSCommandType.SetAmountOfMemory,
                new SetAmountOfMemoryVDSCommand.Params(
                        getVm().getRunOnVds(),
                        getVm().getId(),
                        createMemoryDevice()));

        if (vdsReturnValue.getSucceeded()) {
            setSucceeded(true);
        } else {
            EngineFault fault = new EngineFault();
            fault.setError(vdsReturnValue.getVdsError().getCode());
            fault.setMessage(vdsReturnValue.getVdsError().getMessage());
            getReturnValue().setFault(fault);
        }
    }

    private VmDevice createMemoryDevice() {
        Map<String, Object> specParams = new HashMap<>();
        specParams.put(DEVICE_SIZE_FIELD_KEY, memoryToConsume);
        specParams.put(DEVICE_NODE_FIELD_KEY, getParameters().getNumaNode());
        return new VmDevice(new VmDeviceId(Guid.newGuid(), getVmId()),
                VmDeviceGeneralType.MEMORY,
                VmDeviceType.MEMORY.getName(),
                "",
                0,
                specParams,
                true,
                true,
                false,
                "",
                null,
                null,
                null);
    }

    @Override
    public List<QuotaConsumptionParameter> getQuotaVdsConsumptionParameters() {
        List<QuotaConsumptionParameter> list = new ArrayList<>();

        // Calculate the change in memory consumption,
        // result above Zero means we add memory to the VM (consume)
        // result bellow Zero means we subtracted memory from the VM (release)
        QuotaConsumptionParameter.QuotaAction quotaAction = (memoryToConsume > 0) ?
                QuotaConsumptionParameter.QuotaAction.CONSUME :
                QuotaConsumptionParameter.QuotaAction.RELEASE;

        list.add(new QuotaClusterConsumptionParameter(getVm().getQuotaId(),
                null,
                quotaAction,
                getVm().getClusterId(),
                0,
                Math.abs(memoryToConsume)));
        return list;
    }

    @Override
    public AuditLogType getAuditLogTypeValue() {
        if (getSucceeded()) {
            addCustomValue(LOGABLE_FIELD_NEW_MEMORY, String.valueOf(getParameters().getVm().getMemSizeMb()));
            addCustomValue(LOGABLE_FIELD_PREVIOUS_MEMORY, String.valueOf(getVm().getMemSizeMb()));
            return AuditLogType.HOT_SET_MEMORY;
        } else {
            addCustomValue(LOGABLE_FIELD_ERROR_MESSAGE, getReturnValue().getFault().getMessage());
            return AuditLogType.FAILED_HOT_SET_MEMORY;
        }

    }
}
