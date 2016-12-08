package org.ovirt.engine.core.bll.network.vm.mac;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import javax.inject.Inject;

import org.ovirt.engine.core.bll.network.macpool.ReadMacPool;
import org.ovirt.engine.core.bll.network.vm.ExternalVmMacsFinder;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.compat.Guid;

public class VmMacsValidationsFactory {

    private final ExternalVmMacsFinder externalVmMacsFinder;

    @Inject
    VmMacsValidationsFactory(ExternalVmMacsFinder externalVmMacsFinder) {
        this.externalVmMacsFinder = Objects.requireNonNull(externalVmMacsFinder);
    }

    private VmMacsValidation createOutOfRangeValidation(Guid clusterId) {
        return new VmMacsValidation(
                EngineMessage.NETWORK_OUT_OF_RANGE_MACS,
                new OutOfRangeVmMacsFinder(externalVmMacsFinder, clusterId));
    }

    private VmMacsValidation createMacsInUseValidation(ReadMacPool macPool) {
        return new VmMacsValidation(
                EngineMessage.NETWORK_MAC_ADDRESS_IN_USE_DETAILED,
                new VmMacsInUseFinder(macPool));
    }

    public List<VmMacsValidation> createVmMacsValidationList(Guid clusterId, ReadMacPool macPool) {
        return Arrays.asList(
                createOutOfRangeValidation(clusterId),
                createMacsInUseValidation(macPool));
    }
}
