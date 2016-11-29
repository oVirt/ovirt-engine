package org.ovirt.engine.core.bll.network.vm.mac;

import java.util.Collection;
import java.util.Collections;
import java.util.Objects;
import java.util.stream.Collectors;

import org.ovirt.engine.core.bll.network.macpool.MacPool;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.network.VmNetworkInterface;

class VmMacsInUseFinder implements ProblematicVmMacsFinder {
    private final MacPool macPool;

    VmMacsInUseFinder(MacPool macPool) {
        this.macPool = Objects.requireNonNull(macPool);
    }

    @Override
    public Collection<String> findProblematicMacs(VM vm) {
        if (macPool.isDuplicateMacAddressesAllowed()) {
            return Collections.emptyList();
        } else {
            return vm.getInterfaces()
                    .stream()
                    .map(VmNetworkInterface::getMacAddress)
                    .filter(macPool::isMacInUse)
                    .collect(Collectors.toList());
        }
    }
}
