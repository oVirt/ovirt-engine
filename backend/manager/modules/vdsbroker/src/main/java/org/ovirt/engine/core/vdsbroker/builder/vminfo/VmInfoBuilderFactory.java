package org.ovirt.engine.core.vdsbroker.builder.vminfo;

import java.util.Map;
import java.util.Objects;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.ClusterDao;
import org.ovirt.engine.core.dao.VdsNumaNodeDao;
import org.ovirt.engine.core.dao.VmDeviceDao;
import org.ovirt.engine.core.dao.VmNumaNodeDao;
import org.ovirt.engine.core.dao.network.NetworkDao;

@Singleton
public class VmInfoBuilderFactory {
    private final ClusterDao clusterDao;
    private final NetworkDao networkDao;
    private final VdsNumaNodeDao vdsNumaNodeDao;
    private final VmDeviceDao vmDeviceDao;
    private final VmNumaNodeDao vmNumaNodeDao;
    private final VmInfoBuildUtils vmInfoBuildUtils;

    @Inject
    VmInfoBuilderFactory(
            ClusterDao clusterDao,
            NetworkDao networkDao,
            VdsNumaNodeDao vdsNumaNodeDao,
            VmDeviceDao vmDeviceDao,
            VmNumaNodeDao vmNumaNodeDao,
            VmInfoBuildUtils vmInfoBuildUtils) {
        this.clusterDao = Objects.requireNonNull(clusterDao);
        this.networkDao = Objects.requireNonNull(networkDao);
        this.vdsNumaNodeDao = Objects.requireNonNull(vdsNumaNodeDao);
        this.vmDeviceDao = Objects.requireNonNull(vmDeviceDao);
        this.vmNumaNodeDao = Objects.requireNonNull(vmNumaNodeDao);
        this.vmInfoBuildUtils = Objects.requireNonNull(vmInfoBuildUtils);
    }

    public VmInfoBuilder createVmInfoBuilder(VM vm, Guid vdsId, Map<String, Object> createInfo) {
        return new VmInfoBuilderImpl(
                vm,
                vdsId,
                createInfo,
                clusterDao,
                networkDao,
                vdsNumaNodeDao,
                vmDeviceDao,
                vmNumaNodeDao,
                vmInfoBuildUtils);
    }
}
