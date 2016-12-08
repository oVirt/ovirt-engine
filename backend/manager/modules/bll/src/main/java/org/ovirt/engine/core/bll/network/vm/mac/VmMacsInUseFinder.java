package org.ovirt.engine.core.bll.network.vm.mac;

import java.util.Collection;
import java.util.Collections;
import java.util.Objects;
import java.util.stream.Collectors;

import org.ovirt.engine.core.bll.network.macpool.ReadMacPool;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.network.VmNetworkInterface;

class VmMacsInUseFinder implements ProblematicVmMacsFinder {
    private final ReadMacPool readMacPool;

    VmMacsInUseFinder(ReadMacPool readMacPool) {
        this.readMacPool = Objects.requireNonNull(readMacPool);
    }

    @Override
    public Collection<String> findProblematicMacs(VM vm) {
        if (readMacPool.isDuplicateMacAddressesAllowed()) {
            return Collections.emptyList();
        } else {
            return vm.getInterfaces()
                    .stream()
                    .map(VmNetworkInterface::getMacAddress)
                    .filter(readMacPool::isMacInUse)
                    .collect(Collectors.toList());
        }
    }
}
