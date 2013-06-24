package org.ovirt.engine.core.bll;

import org.ovirt.engine.core.common.action.VdsGroupOperationParameters;
import org.ovirt.engine.core.common.businessentities.VDSGroup;
import org.ovirt.engine.core.common.businessentities.VdsSelectionAlgorithm;
import org.ovirt.engine.core.common.config.Config;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.common.errors.VdcBllMessages;
import org.ovirt.engine.core.compat.Guid;

public abstract class VdsGroupOperationCommandBase<T extends VdsGroupOperationParameters> extends
        VdsGroupCommandBase<T> {

    // If the CPU thresholds are set to -1 then we should get the value from the configuration
    public static final int GET_CPU_THRESHOLDS_FROM_CONFIGURATION = -1;

    public VdsGroupOperationCommandBase(T parameters) {
        super(parameters);
    }

    protected VdsGroupOperationCommandBase(Guid commandId) {
        super(commandId);
    }

    @Override
    public VDSGroup getVdsGroup() {
        return getParameters().getVdsGroup();
    }

    protected void CheckMaxMemoryOverCommitValue() {
        if (getVdsGroup().getmax_vds_memory_over_commit() <= 0) {
            getVdsGroup().setmax_vds_memory_over_commit(
                    Config.<Integer> GetValue(ConfigValues.MaxVdsMemOverCommit));
        }
    }

    protected boolean isCpuUtilizationValid(int cpuUtilization) {
        return (cpuUtilization <= 100 && cpuUtilization >= 0)
                || cpuUtilization == GET_CPU_THRESHOLDS_FROM_CONFIGURATION;
    }

    protected boolean isCpuUtilizationExist(int cpuUtilization) {
        return cpuUtilization != GET_CPU_THRESHOLDS_FROM_CONFIGURATION;
    }

    protected boolean validateMetrics() {
        boolean result = true;

        VdsSelectionAlgorithm selectionAlgorithm = getVdsGroup().getselection_algorithm();

        if (result && selectionAlgorithm != null && !selectionAlgorithm.equals(VdsSelectionAlgorithm.None)) {

            if (result
                    && (!isCpuUtilizationValid(getVdsGroup().gethigh_utilization()) || !isCpuUtilizationValid(getVdsGroup().getlow_utilization()))) {
                addCanDoActionMessage(VdcBllMessages.VDS_GROUP_CPU_UTILIZATION_MUST_BE_IN_VALID_RANGE);
                result = false;
            }

            if (result && getVdsGroup().getlow_utilization() > getVdsGroup().gethigh_utilization()) {
                addCanDoActionMessage(VdcBllMessages.VDS_GROUP_CPU_LOW_UTILIZATION_PERCENTAGE_MUST_BE_LOWER_THAN_HIGH_PERCENTAGE);
                result = false;
            }

            if (result) {
                if (selectionAlgorithm.equals(VdsSelectionAlgorithm.EvenlyDistribute)) {
                    if (!isCpuUtilizationExist(getVdsGroup().gethigh_utilization())) {
                        addCanDoActionMessage(VdcBllMessages.VDS_GROUP_CPU_HIGH_UTILIZATION_PERCENTAGE_MUST_BE_DEFINED_WHEN_USING_EVENLY_DISTRIBUTED);
                        result = false;
                    }
                } else if (selectionAlgorithm.equals(VdsSelectionAlgorithm.PowerSave)) {
                    if (!isCpuUtilizationExist(getVdsGroup().gethigh_utilization())
                            || !isCpuUtilizationExist(getVdsGroup().getlow_utilization())) {
                        addCanDoActionMessage(VdcBllMessages.VDS_GROUP_BOTH_LOW_AND_HIGH_CPU_UTILIZATION_PERCENTAGE_MUST_BE_DEFINED_WHEN_USING_POWER_SAVING);
                        result = false;
                    }
                }
            }
        }

        return result;
    }

    protected boolean isAllowClusterWithVirtGluster() {
        Boolean allowVirGluster = Config.<Boolean> GetValue(ConfigValues.AllowClusterWithVirtGlusterEnabled);
        return allowVirGluster;
    }
}
