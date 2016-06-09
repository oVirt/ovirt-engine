package org.ovirt.engine.core.vdsbroker.vdsbroker.factory;

import java.util.Map;
import java.util.Objects;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.VdsNumaNodeDao;
import org.ovirt.engine.core.dao.VmDeviceDao;
import org.ovirt.engine.core.dao.VmNumaNodeDao;
import org.ovirt.engine.core.vdsbroker.vdsbroker.VmInfoBuildUtils;
import org.ovirt.engine.core.vdsbroker.vdsbroker.VmInfoBuilder;

@Singleton
public class VmInfoBuilderFactory {
    private final VdsNumaNodeDao vdsNumaNodeDao;
    private final VmDeviceDao vmDeviceDao;
    private final VmNumaNodeDao vmNumaNodeDao;
    private final VmInfoBuildUtils vmInfoBuildUtils;

    @Inject
    VmInfoBuilderFactory(
            VdsNumaNodeDao vdsNumaNodeDao,
            VmDeviceDao vmDeviceDao,
            VmNumaNodeDao vmNumaNodeDao,
            VmInfoBuildUtils vmInfoBuildUtils) {
        this.vdsNumaNodeDao = Objects.requireNonNull(vdsNumaNodeDao);
        this.vmDeviceDao = Objects.requireNonNull(vmDeviceDao);
        this.vmNumaNodeDao = Objects.requireNonNull(vmNumaNodeDao);
        this.vmInfoBuildUtils = Objects.requireNonNull(vmInfoBuildUtils);
    }

    public VmInfoBuilder createVmInfoBuilder(VM vm, Guid vdsId, Map createInfo) {
        return new VmInfoBuilder(
                vm,
                vdsId,
                createInfo,
                vdsNumaNodeDao,
                vmDeviceDao,
                vmNumaNodeDao,
                vmInfoBuildUtils);
    }
}
