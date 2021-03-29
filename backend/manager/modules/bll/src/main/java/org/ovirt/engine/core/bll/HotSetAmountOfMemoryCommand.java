package org.ovirt.engine.core.bll;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

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
import org.ovirt.engine.core.dao.VmDeviceDao;
import org.ovirt.engine.core.utils.ReplacementUtils;
import org.ovirt.engine.core.vdsbroker.SetAmountOfMemoryVDSCommand;
import org.ovirt.engine.core.vdsbroker.libvirt.DomainXmlUtils;

@NonTransactiveCommandAttribute
public class HotSetAmountOfMemoryCommand<T extends HotSetAmountOfMemoryParameters> extends VmManagementCommandBase<T> implements QuotaVdsDependent {

    public static final String LOGABLE_FIELD_NEW_MEMORY = "newMem";
    public static final String LOGABLE_FIELD_PREVIOUS_MEMORY = "previousMem";
    public static final String LOGABLE_FIELD_ERROR_MESSAGE = "ErrorMessage";
    public static final String DEVICE_SIZE_FIELD_KEY = "size";
    public static final String DEVICE_NODE_FIELD_KEY = "node";

    @Inject
    private VmDeviceDao vmDeviceDao;

    public HotSetAmountOfMemoryCommand(T parameters, CommandContext commandContext) {
        super(parameters, commandContext);
    }

    @Override
    protected void init() {
        super.init();
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
            List<VmDevice> memDevices = vmDeviceDao.getVmDeviceByVmIdAndType(getVmId(), VmDeviceGeneralType.MEMORY);
            if (memDevices.size() == Config.<Integer>getValue(ConfigValues.MaxMemorySlots)) {
                return failValidation(EngineMessage.ACTION_TYPE_FAILED_NO_MORE_MEMORY_SLOTS,
                        "$maxMemSlots " + Config.getValue(ConfigValues.MaxMemorySlots).toString());
            }
            final int hotplugMemorySizeFactor = getVm().getClusterArch().getHotplugMemorySizeFactorMb();
            if (getParameters().getMemoryDeviceSizeMb() % hotplugMemorySizeFactor != 0) {
                return failValidation(EngineMessage.ACTION_TYPE_FAILED_NOT_PLUGGED_MEMORY_ON_ARCH_MUST_BE_DIVIDABLE_BY,
                        ReplacementUtils.createSetVariableString("architecture", getVm().getClusterArch()),
                        ReplacementUtils.createSetVariableString("memorySize", getParameters().getMemoryDeviceSizeMb()),
                        ReplacementUtils.createSetVariableString("factor", hotplugMemorySizeFactor));
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
                        createMemoryDevice(),
                        getParameters().getVmStaticData().getMinAllocatedMem()));

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
        Guid guid = Guid.newGuid();
        specParams.put(DEVICE_SIZE_FIELD_KEY, String.valueOf(getParameters().getMemoryDeviceSizeMb()));
        specParams.put(DEVICE_NODE_FIELD_KEY, String.valueOf(getParameters().getNumaNode()));
        return new VmDevice(new VmDeviceId(guid, getVmId()),
                VmDeviceGeneralType.MEMORY,
                VmDeviceType.MEMORY.getName(),
                "",
                specParams,
                true,
                true,
                false,
                String.format("%s%s", DomainXmlUtils.USER_ALIAS_PREFIX, guid),
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
        QuotaConsumptionParameter.QuotaAction quotaAction = (getParameters().getMemoryDeviceSizeMb() > 0) ?
                QuotaConsumptionParameter.QuotaAction.CONSUME :
                QuotaConsumptionParameter.QuotaAction.RELEASE;

        list.add(new QuotaClusterConsumptionParameter(getVm().getQuotaId(),
                quotaAction,
                getVm().getClusterId(),
                0,
                Math.abs(getParameters().getMemoryDeviceSizeMb())));
        return list;
    }

    @Override
    public AuditLogType getAuditLogTypeValue() {
        if (getSucceeded()) {
            int origMemSize = getVm().getMemSizeMb();
            addCustomValue(LOGABLE_FIELD_NEW_MEMORY,
                    String.valueOf(origMemSize + getParameters().getMemoryDeviceSizeMb()));
            addCustomValue(LOGABLE_FIELD_PREVIOUS_MEMORY, String.valueOf(origMemSize));
            return AuditLogType.HOT_SET_MEMORY;
        } else {
            addCustomValue(LOGABLE_FIELD_ERROR_MESSAGE, getReturnValue().getFault().getMessage());
            return AuditLogType.FAILED_HOT_SET_MEMORY;
        }

    }
}
