package org.ovirt.engine.core.bll;

import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.ovirt.engine.core.bll.validator.LocalizedVmStatus;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.FeatureSupported;
import org.ovirt.engine.core.common.action.VmSlaPolicyParameters;
import org.ovirt.engine.core.common.businessentities.qos.StorageQos;
import org.ovirt.engine.core.common.businessentities.storage.DiskImage;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.common.vdscommands.UpdateVmPolicyVDSParams;
import org.ovirt.engine.core.common.vdscommands.VDSCommandType;
import org.ovirt.engine.core.common.vdscommands.VDSReturnValue;
import org.ovirt.engine.core.utils.linq.Function;
import org.ovirt.engine.core.utils.linq.LinqUtils;
import org.ovirt.engine.core.vdsbroker.vdsbroker.IoTuneUtils;

/**
 * VmSlaPolicyCommand, This command will push SLA parameters such as CPU, RAM and IO
 * tuning to the VM. This Command runs as a hot plug (when the VM is running).
 *
 * The execute will never throw an exception. it will rather wrap a return value in case
 * of failure.
 */
@NonTransactiveCommandAttribute
public class VmSlaPolicyCommand<T extends VmSlaPolicyParameters> extends VmCommand<T> {

    public static final String LOGABLE_FIELD_CPU_LIMIT = "cpuLimit";
    public static final String LOGABLE_FIELD_DISK_LIST = "diskList";

    public VmSlaPolicyCommand(T parameters) {
        super(parameters);
    }

    @Override
    protected boolean canDoAction() {
        if (getVm() == null) {
            return failCanDoAction(EngineMessage.ACTION_TYPE_FAILED_VM_NOT_FOUND);
        }

        if (!getVm().getStatus().isQualifiedForQosChange()) {
            return failCanDoAction(EngineMessage.ACTION_TYPE_FAILED_VM_STATUS_ILLEGAL,
                    LocalizedVmStatus.from(getVm().getStatus()));
        }

        if (!FeatureSupported.vmSlaPolicy(getVm().getVdsGroupCompatibilityVersion())) {
            return failCanDoAction(EngineMessage.VM_SLA_POLICY_NOT_SUPPORTED);
        }
        if (getParameters().isEmpty()) {
            return failCanDoAction(EngineMessage.VM_SLA_POLICY_UNCHANGED);
        }
        return true;
    }

    /**
     * Execution shall perform a call to VDSM to set the SLA parameters.
     */
    @Override
    protected void executeCommand() {
        Integer cpuLimit = null;
        if (getParameters().getCpuQos() != null) {
            cpuLimit = getParameters().getCpuQos().getCpuLimit();
            cpuLimit = (cpuLimit != null) ? cpuLimit : 100;
        }

        UpdateVmPolicyVDSParams params = new UpdateVmPolicyVDSParams(getVm().getRunOnVds(), getVmId(), cpuLimit);

        for (Map.Entry<DiskImage, StorageQos> entry : getParameters().getStorageQos().entrySet()) {
            DiskImage diskImage = entry.getKey();
            Map<String, Long> ioTuneStruct = IoTuneUtils.ioTuneMapFrom(entry.getValue());
            params.addIoTuneParams(diskImage, ioTuneStruct);
        }

        VDSReturnValue vdsReturnValue = runVdsCommand(VDSCommandType.UpdateVmPolicy, params);
        setSucceeded(vdsReturnValue.getSucceeded());
    }

    @Override
    public AuditLogType getAuditLogTypeValue() {
        // It can't happen that this will stay 0, the validation prevents running the command without changed CPU or
        // storage QoS.
        int logId = 0;
        if (getParameters().getCpuQos() != null) {
            Integer limit = getParameters().getCpuQos().getCpuLimit();
            addCustomValue(LOGABLE_FIELD_CPU_LIMIT, (limit != null) ? String.valueOf(limit): "unlimited");
            logId += 1;
        }

        if (getParameters().getStorageQos() != null && !getParameters().getStorageQos().isEmpty()) {
            Set<DiskImage> diskImages = getParameters().getStorageQos().keySet();
            final String diskNames = StringUtils.join(LinqUtils.transformToList(diskImages, new Function<DiskImage,
                    String>() {
                @Override public String eval(DiskImage diskImage) {
                    return diskImage.getDiskAlias();
                }
            }), ", ");

            addCustomValue(LOGABLE_FIELD_DISK_LIST, diskNames);
            logId += 2;
        }

        final AuditLogType[] logTypes = {
                AuditLogType.UNASSIGNED, // This can't happen
                AuditLogType.VM_SLA_POLICY_CPU,
                AuditLogType.VM_SLA_POLICY_STORAGE,
                AuditLogType.VM_SLA_POLICY_CPU_STORAGE
        };

        return getSucceeded() ? logTypes[logId] : AuditLogType.FAILED_VM_SLA_POLICY;
    }

    @Override
    protected void setActionMessageParameters() {
        addCanDoActionMessage(EngineMessage.VAR__ACTION__UPDATE_SLA_POLICY);
        addCanDoActionMessage(EngineMessage.VAR__TYPE__VM);
    }
}
