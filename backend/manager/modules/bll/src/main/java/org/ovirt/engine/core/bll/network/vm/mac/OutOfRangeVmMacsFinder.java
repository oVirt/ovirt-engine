package org.ovirt.engine.core.bll.network.vm.mac;

import java.util.Collection;
import java.util.Objects;

import org.ovirt.engine.core.bll.network.vm.ExternalVmMacsFinder;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.compat.Guid;

class OutOfRangeVmMacsFinder implements ProblematicVmMacsFinder {

    private final ExternalVmMacsFinder externalVmMacsFinder;
    private final Guid clusterId;

    OutOfRangeVmMacsFinder(
            ExternalVmMacsFinder externalVmMacsFinder,
            Guid clusterId) {
        this.externalVmMacsFinder = Objects.requireNonNull(externalVmMacsFinder);
        this.clusterId = Objects.requireNonNull(clusterId);
    }

    @Override
    public Collection<String> findProblematicMacs(VM vm) {
        vm.setClusterId(clusterId);
        return externalVmMacsFinder.findExternalMacAddresses(vm);
    }
}
