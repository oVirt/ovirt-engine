package org.ovirt.engine.core.vdsbroker.vdsbroker;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.ovirt.engine.core.common.businessentities.Cluster;
import org.ovirt.engine.core.common.businessentities.SerialNumberPolicy;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.config.Config;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.ClusterDao;

@Singleton
public class VmSerialNumberBuilder {
    @Inject
    private ClusterDao clusterDao;

    public String buildVmSerialNumber(VM vm) {
        return vm.getSerialNumberPolicy() != null ?
                getSerialNumberFromPolicy(vm.getSerialNumberPolicy(), vm.getCustomSerialNumber(), vm.getId())
                : buildVmSerialNumberFromCluster(vm);
    }

    private String buildVmSerialNumberFromCluster(VM vm) {
        Cluster cluster = clusterDao.get(vm.getClusterId());
        return cluster.getSerialNumberPolicy() != null ?
                getSerialNumberFromPolicy(cluster.getSerialNumberPolicy(), cluster.getCustomSerialNumber(), vm.getId())
                : buildVmSerialNumberFromConfig(vm);
    }

    private String buildVmSerialNumberFromConfig(VM vm) {
        final SerialNumberPolicy policy = Config.getValue(ConfigValues.DefaultSerialNumberPolicy);
        final String customSerialNumber = Config.getValue(ConfigValues.DefaultCustomSerialNumber);
        return getSerialNumberFromPolicy(policy, customSerialNumber, vm.getId());
    }

    private String getSerialNumberFromPolicy(SerialNumberPolicy policy, String customSerialNumber, Guid vmId) {
        switch (policy) {
            default:
            case HOST_ID:
                return null;
            case VM_ID:
                return vmId.toString();
            case CUSTOM:
                return customSerialNumber;
        }
    }
}
