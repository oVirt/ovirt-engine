package org.ovirt.engine.core.bll.network.vm;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import javax.inject.Inject;

import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.bll.network.macpool.MacPool;
import org.ovirt.engine.core.bll.network.macpool.MacPoolPerDc;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.network.VmNetworkInterface;

public class ExternalVmMacsFinder {

    private final MacPoolPerDc macPoolPerDc;

    @Inject
    ExternalVmMacsFinder(MacPoolPerDc macPoolPerDc) {
        this.macPoolPerDc = Objects.requireNonNull(macPoolPerDc, "macPoolPerDc cannot be null");
    }

    public Set<String> findExternalMacAddresses(VM vm, CommandContext commandContext) {
        final List<VmNetworkInterface> interfaces = vm.getInterfaces();
        if (interfaces == null) {
            return Collections.emptySet();
        }
        final MacPool macPool = macPoolPerDc.getMacPoolForDataCenter(vm.getStoragePoolId(), commandContext);
        return interfaces
                .stream()
                .map(VmNetworkInterface::getMacAddress)
                .filter(Objects::nonNull)
                .filter(((Predicate<String>) macPool::isMacInRange).negate())
                .collect(Collectors.toSet());
    }

}
