package org.ovirt.engine.core.bll.scheduling.pending;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.ovirt.engine.core.bll.scheduling.utils.VmSpecificPendingResourceEqualizer;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VdsCpuUnit;
import org.ovirt.engine.core.compat.Guid;

public class PendingCpuPinning extends PendingResource {
    private List<VdsCpuUnit> cpuPinning;

    public PendingCpuPinning(Guid host, VM vm, List<VdsCpuUnit> cpuPinnings) {
        super(host, vm);
        this.cpuPinning = cpuPinnings;
    }

    public List<VdsCpuUnit> getCpuPinning() {
        return this.cpuPinning;
    }

    @Override
    public boolean equals(Object other) {
        return VmSpecificPendingResourceEqualizer.isEqual(this, other);
    }

    @Override
    public int hashCode() {
        return VmSpecificPendingResourceEqualizer.calcHashCode(this);
    }

    public static Map<Guid, List<VdsCpuUnit>> collectForHost(PendingResourceManager manager, Guid host) {
        Map<Guid, List<VdsCpuUnit>> vmToPendingPinnings = new HashMap<>();
        manager.pendingHostResources(host, PendingCpuPinning.class)
                .stream()
                .filter(resource -> resource.getCpuPinning() != null)
                .forEach(resource -> vmToPendingPinnings.put(resource.getVm(), resource.getCpuPinning()));

        return vmToPendingPinnings;
    }
}
