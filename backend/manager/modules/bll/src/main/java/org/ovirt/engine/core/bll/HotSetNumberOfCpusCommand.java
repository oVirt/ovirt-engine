package org.ovirt.engine.core.bll;

import org.ovirt.engine.core.bll.scheduling.SlaValidator;
import org.ovirt.engine.core.bll.validator.LocalizedVmStatus;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.FeatureSupported;
import org.ovirt.engine.core.common.action.HotSetNumerOfCpusParameters;
import org.ovirt.engine.core.common.action.PlugAction;
import org.ovirt.engine.core.common.businessentities.VMStatus;
import org.ovirt.engine.core.common.errors.VdcBllMessages;
import org.ovirt.engine.core.common.errors.VdcFault;
import org.ovirt.engine.core.common.vdscommands.VDSCommandType;
import org.ovirt.engine.core.common.vdscommands.VDSReturnValue;
import org.ovirt.engine.core.vdsbroker.SetNumberOfCpusVDSCommand;

/**
 * Set the number of CPU of a running VM also called hot plug or hot unplug, hot add, hot remove.
 * This command behaviour varies between OS implementation. To that matter, the parameter of the desired
 * number of CPUs will manifest as a hot plug or unplug, depending on the current cpu count at the Guest level.
 *
 * The execute will never throw an exception. it will rather wrap a return value in case of failure.
 */
@NonTransactiveCommandAttribute
public class HotSetNumberOfCpusCommand<T extends HotSetNumerOfCpusParameters> extends VmManagementCommandBase<T> {

    public static final String LOGABLE_FIELD_NUMBER_OF_CPUS = "numberOfCpus";
    public static final String LOGABLE_FIELD_ERROR_MESSAGE = "ErrorMessage";

    public HotSetNumberOfCpusCommand(T parameters) {
        super(parameters);
    }

    @Override
    protected boolean canDoAction() {
        boolean canDo = true;
        if (getVm() == null) {
            return failCanDoAction(VdcBllMessages.ACTION_TYPE_FAILED_VM_NOT_EXIST);
        }
        if (getVm().getStatus() != VMStatus.Up) {
            canDo = failCanDoAction(VdcBllMessages.ACTION_TYPE_FAILED_VM_STATUS_ILLEGAL,
                    LocalizedVmStatus.from(getVm().getStatus()));
        }
        if (getParameters().getVm().getNumOfCpus() > SlaValidator.getEffectiveCpuCores(getVds())) {
            canDo = failCanDoAction(VdcBllMessages.ACTION_TYPE_FAILED_VDS_VM_CPUS);
        }
        if (getParameters().getPlugAction() == PlugAction.PLUG) {
            if (!FeatureSupported.hotPlugCpu(getVm().getVdsGroupCompatibilityVersion(), getVm().getClusterArch())) {
                canDo = failCanDoAction(VdcBllMessages.HOT_PLUG_CPU_IS_NOT_SUPPORTED);
            }
        } else if (!FeatureSupported.hotUnplugCpu(getVm().getVdsGroupCompatibilityVersion(), getVm().getClusterArch())) {
            canDo = failCanDoAction(VdcBllMessages.HOT_UNPLUG_CPU_IS_NOT_SUPPORTED);
        }

        return canDo;
    }

    /**
     * Execution shall perform a call to VDSM to set the number of CPUs.
     * The guest OS will plug/unplug CPUs if the current guest configuration is lower/higher than
     * the requested number respectively.
     */
    @Override
    protected void executeCommand() {
        VDSReturnValue vdsReturnValue = runVdsCommand(VDSCommandType.SetNumberOfCpus,
                new SetNumberOfCpusVDSCommand.Params(
                        getVm().getRunOnVds(),
                        getVm().getId(),
                        getParameters().getVm().getNumOfCpus()));

        if (vdsReturnValue.getSucceeded()) {
            setSucceeded(true);
        } else {
            VdcFault fault = new VdcFault();
            fault.setError(vdsReturnValue.getVdsError().getCode());
            fault.setMessage(vdsReturnValue.getVdsError().getMessage());
            getReturnValue().setFault(fault);
        }

    }

    @Override
    public AuditLogType getAuditLogTypeValue() {
        addCustomValue(LOGABLE_FIELD_NUMBER_OF_CPUS, String.valueOf(getParameters().getVm().getNumOfCpus()));

        if (getSucceeded()) {
            return AuditLogType.HOT_SET_NUMBER_OF_CPUS;
        } else {
            addCustomValue(LOGABLE_FIELD_ERROR_MESSAGE, getReturnValue().getFault().getMessage());
            return AuditLogType.FAILED_HOT_SET_NUMBER_OF_CPUS;
        }

    }

    @Override
    protected void setActionMessageParameters() {
        addCanDoActionMessage(VdcBllMessages.VAR__ACTION__HOT_SET_CPUS);
        addCanDoActionMessage(VdcBllMessages.VAR__TYPE__VM);
        addCanDoActionMessage(String.format("$clusterVersion %1$s", getVm().getVdsGroupCompatibilityVersion() ));
        addCanDoActionMessage(String.format("$architecture %1$s", getVm().getClusterArch()));
    }
}
