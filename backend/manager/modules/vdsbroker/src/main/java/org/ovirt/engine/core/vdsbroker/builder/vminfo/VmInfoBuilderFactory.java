package org.ovirt.engine.core.vdsbroker.builder.vminfo;

import java.util.Map;
import java.util.Objects;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.osinfo.OsRepository;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.VmDeviceDao;
import org.ovirt.engine.core.dao.network.NetworkDao;

@Singleton
public class VmInfoBuilderFactory {
    private final NetworkDao networkDao;
    private final VmDeviceDao vmDeviceDao;
    private final VmInfoBuildUtils vmInfoBuildUtils;
    private final OsRepository osRepository;

    @Inject
    VmInfoBuilderFactory(
            NetworkDao networkDao,
            VmDeviceDao vmDeviceDao,
            VmInfoBuildUtils vmInfoBuildUtils,
            OsRepository osRepository) {
        this.networkDao = Objects.requireNonNull(networkDao);
        this.vmDeviceDao = Objects.requireNonNull(vmDeviceDao);
        this.vmInfoBuildUtils = Objects.requireNonNull(vmInfoBuildUtils);
        this.osRepository = Objects.requireNonNull(osRepository);
    }

    public VmInfoBuilder createVmInfoBuilder(VM vm, Guid vdsId, Map<String, Object> createInfo) {
        return new VmInfoBuilderImpl(
                vm,
                vdsId,
                createInfo,
                networkDao,
                vmDeviceDao,
                vmInfoBuildUtils,
                osRepository);
    }
}
