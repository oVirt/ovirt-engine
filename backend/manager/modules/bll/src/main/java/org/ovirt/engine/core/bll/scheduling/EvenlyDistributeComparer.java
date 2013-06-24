package org.ovirt.engine.core.bll.scheduling;

import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VdsSpmStatus;
import org.ovirt.engine.core.common.config.Config;
import org.ovirt.engine.core.common.config.ConfigValues;

public class EvenlyDistributeComparer extends VdsComparer {
    private double calcDistributeMetric(VDS vds, VM vm) {
        int vcpu = Config.<Integer> GetValue(ConfigValues.VcpuConsumptionPercentage);
        int spmCpu = (vds.getSpmStatus() == VdsSpmStatus.None) ? 0 : Config
                .<Integer> GetValue(ConfigValues.SpmVCpuConsumption);
        int hostCores = SlaValidator.getEffectiveCpuCores(vds);
        double hostCpu = vds.getUsageCpuPercent();
        double pendingVcpus = vds.getPendingVcpusCount();

        return (hostCpu / vcpu) + (pendingVcpus + vm.getNumOfCpus() + spmCpu) / hostCores;
    }

    @Override
    public boolean isBetter(VDS x, VDS y, VM vm) {
        if (SlaValidator.getEffectiveCpuCores(x) == null
                || SlaValidator.getEffectiveCpuCores(y) == null
                || x.getUsageCpuPercent() == null
                || y.getUsageCpuPercent() == null
                || x.getPendingVcpusCount() == null
                || y.getPendingVcpusCount() == null) {
            return false;
        }

        return calcDistributeMetric(x, vm) > calcDistributeMetric(y, vm);

    }

    @Override
    public void bestVdsProcedure(VDS x) {
    }
}
