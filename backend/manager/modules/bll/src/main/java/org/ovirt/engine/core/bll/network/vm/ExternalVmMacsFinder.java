package org.ovirt.engine.core.bll.network.vm;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import javax.inject.Inject;

import org.apache.commons.lang.StringUtils;
import org.ovirt.engine.core.bll.network.macpool.MacPoolPerCluster;
import org.ovirt.engine.core.bll.network.macpool.ReadMacPool;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.network.VmNetworkInterface;

public class ExternalVmMacsFinder {

    private final MacPoolPerCluster macPoolPerCluster;

    @Inject
    ExternalVmMacsFinder(MacPoolPerCluster macPoolPerCluster) {
        this.macPoolPerCluster = Objects.requireNonNull(macPoolPerCluster, "macPoolPerCluster cannot be null");
    }

    public Set<String> findExternalMacAddresses(VM vm) {
        final List<VmNetworkInterface> interfaces = vm.getInterfaces();
        if (interfaces == null) {
            return Collections.emptySet();
        }
        final ReadMacPool readMacPool = macPoolPerCluster.getMacPoolForCluster(vm.getClusterId());
        return interfaces
                .stream()
                .map(VmNetworkInterface::getMacAddress)
                .filter(StringUtils::isNotEmpty)
                .filter(((Predicate<String>) readMacPool::isMacInRange).negate())
                .collect(Collectors.toSet());
    }

}
