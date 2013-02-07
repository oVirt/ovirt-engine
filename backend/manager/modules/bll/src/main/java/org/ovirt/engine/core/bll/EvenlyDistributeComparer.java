package org.ovirt.engine.core.bll;

import org.ovirt.engine.core.common.businessentities.*;
import org.ovirt.engine.core.common.config.Config;
import org.ovirt.engine.core.common.config.ConfigValues;

public class EvenlyDistributeComparer extends VdsComparer {
    private double calcDistributeMetric(VDS vds, VM vm) {
        int vcpu = Config.<Integer> GetValue(ConfigValues.VcpuConsumptionPercentage);
        int spmCpu = (vds.getSpmStatus() == VdsSpmStatus.None) ? 0 : Config
                .<Integer> GetValue(ConfigValues.SpmVCpuConsumption);
        int hostCores = VdsSelector.getEffectiveCpuCores(vds);
        double hostCpu = vds.getUsageCpuPercent();
        double pendingVcpus = vds.getPendingVcpusCount();

        return (hostCpu / vcpu) + (pendingVcpus + vm.getNumOfCpus() + spmCpu) / hostCores;
    }

    @Override
    public boolean IsBetter(VDS x, VDS y, VM vm) {
        if (VdsSelector.getEffectiveCpuCores(x) == null
                || VdsSelector.getEffectiveCpuCores(y) == null
                || x.getUsageCpuPercent() == null
                || y.getUsageCpuPercent() == null
                || x.getPendingVcpusCount() == null
                || y.getPendingVcpusCount() == null) {
            return false;
        }

        return calcDistributeMetric(x, vm) > calcDistributeMetric(y, vm);

    }

    @Override
    public void BestVdsProcedure(VDS x) {
    }
}
