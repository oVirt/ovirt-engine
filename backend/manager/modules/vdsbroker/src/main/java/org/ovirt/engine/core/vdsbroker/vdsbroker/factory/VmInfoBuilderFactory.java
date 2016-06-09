package org.ovirt.engine.core.vdsbroker.vdsbroker.factory;

import java.util.Map;
import java.util.Objects;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;
import org.ovirt.engine.core.vdsbroker.vdsbroker.VmInfoBuildUtils;
import org.ovirt.engine.core.vdsbroker.vdsbroker.VmInfoBuilder;

@Singleton
public class VmInfoBuilderFactory {
    private final DbFacade dbFacade;
    private final VmInfoBuildUtils vmInfoBuildUtils;

    @Inject
    VmInfoBuilderFactory(DbFacade dbFacade, VmInfoBuildUtils vmInfoBuildUtils) {
        this.dbFacade = Objects.requireNonNull(dbFacade);
        this.vmInfoBuildUtils = Objects.requireNonNull(vmInfoBuildUtils);
    }

    public VmInfoBuilder createVmInfoBuilder(VM vm, Guid vdsId, Map createInfo) {
        return new VmInfoBuilder(vm, vdsId, createInfo, dbFacade, vmInfoBuildUtils);
    }
}
