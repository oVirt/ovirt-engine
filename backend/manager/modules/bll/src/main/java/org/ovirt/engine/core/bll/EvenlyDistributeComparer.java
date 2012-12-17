package org.ovirt.engine.core.bll;

import org.ovirt.engine.core.common.businessentities.*;
import org.ovirt.engine.core.common.config.Config;
import org.ovirt.engine.core.common.config.ConfigValues;

public class EvenlyDistributeComparer extends VdsComparer {
    private double calcDistributeMetric(VDS vds, VM vm) {
        int vcpu = Config.<Integer> GetValue(ConfigValues.VcpuConsumptionPercentage);
        int spmCpu = (vds.getspm_status() == VdsSpmStatus.None) ? 0 : Config
                .<Integer> GetValue(ConfigValues.SpmVCpuConsumption);
        int hostCores = VdsSelector.getEffectiveCpuCores(vds);
        double hostCpu = vds.getusage_cpu_percent();
        double pendingVcpus = vds.getpending_vcpus_count();

        return (hostCpu / vcpu) + (pendingVcpus + vm.getNumOfCpus() + spmCpu) / hostCores;
    }

    @Override
    public boolean IsBetter(VDS x, VDS y, VM vm) {
        if (VdsSelector.getEffectiveCpuCores(x) == null
                || VdsSelector.getEffectiveCpuCores(y) == null
                || x.getusage_cpu_percent() == null
                || y.getusage_cpu_percent() == null
                || x.getpending_vcpus_count() == null
                || y.getpending_vcpus_count() == null) {
            return false;
        }

        return calcDistributeMetric(x, vm) > calcDistributeMetric(y, vm);

    }

    @Override
    public void BestVdsProcedure(VDS x) {
    }
}
