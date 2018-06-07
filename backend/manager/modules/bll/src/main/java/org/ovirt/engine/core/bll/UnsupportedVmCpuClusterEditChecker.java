package org.ovirt.engine.core.bll;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;

import org.apache.commons.lang.StringUtils;
import org.ovirt.engine.core.common.businessentities.Cluster;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.errors.EngineMessage;

@RequestScoped
public class UnsupportedVmCpuClusterEditChecker implements ClusterEditChecker<VM> {

    private String newCpuName;

    @Inject
    private CpuFlagsManagerHandler cpuFlagsManagerHandler;

    @Override
    public boolean isApplicable(Cluster oldCluster, Cluster newCluster) {
        newCpuName = newCluster.getCpuName();
        final String oldCpuName = oldCluster.getCpuName();
        return StringUtils.isNotEmpty(newCpuName) && !newCpuName.equals(oldCpuName);
    }

    @Override
    public boolean check(VM vm) {
        if (newCpuName == null) {
            throw new IllegalStateException("check() called before isApplicable()");
        }

        if (!StringUtils.isEmpty(vm.getCustomCpuName())) {
            String vmCpuName = cpuFlagsManagerHandler.getCpuNameByCpuId(vm.getCustomCpuName(), cpuFlagsManagerHandler.getLatestDictionaryVersion());
            if (vmCpuName == null || cpuFlagsManagerHandler.compareCpuLevels(vmCpuName, newCpuName, cpuFlagsManagerHandler.getLatestDictionaryVersion()) > 0) {
                return false;
            }
        }
        return true;
    }

    @Override
    public String getMainMessage() {
        return EngineMessage.CLUSTER_WARN_VM_DUE_TO_DECREASED_CLUSTER_CPU_LEVEL.name();
    }

    @Override
    public String getDetailMessage(VM vm) {
        return vm.getCustomCpuName();
    }
}
