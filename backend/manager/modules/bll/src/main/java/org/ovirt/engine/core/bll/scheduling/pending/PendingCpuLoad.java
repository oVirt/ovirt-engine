package org.ovirt.engine.core.bll.scheduling.pending;

import org.ovirt.engine.core.bll.scheduling.utils.VmSpecificPendingResourceEqualizer;
import org.ovirt.engine.core.common.businessentities.CpuPinningPolicy;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.compat.Guid;

public class PendingCpuLoad extends PendingResource {

    private int cpuLoad;

    private CpuPinningPolicy cpuPinningPolicy;

    public PendingCpuLoad(Guid host, VM vm, int cpuLoad) {
        super(host, vm);
        this.cpuLoad = cpuLoad;
        this.cpuPinningPolicy = vm.getCpuPinningPolicy();
    }

    public int getCpuLoad() {
        return cpuLoad;
    }

    public void setCpuLoad(int cpuLoad) {
        this.cpuLoad = cpuLoad;
    }

    public CpuPinningPolicy getCpuPinningPolicy() {
        return cpuPinningPolicy;
    }

    @SuppressWarnings("EqualsWhichDoesntCheckParameterClass")
    @Override
    public boolean equals(Object other) {
        return VmSpecificPendingResourceEqualizer.isEqual(this, other);
    }

    @Override
    public int hashCode() {
        return VmSpecificPendingResourceEqualizer.calcHashCode(this);
    }

    public static int collectForHost(PendingResourceManager manager, Guid host) {
        int sum = 0;
        for (PendingCpuLoad resource: manager.pendingHostResources(host, PendingCpuLoad.class)) {
            sum += resource.getCpuLoad();
        }

        return sum;
    }

    public static int collectSharedForHost(PendingResourceManager manager, Guid host) {
        int sum = 0;
        for (PendingCpuLoad resource : manager.pendingHostResources(host, PendingCpuLoad.class)) {
            if (!resource.getCpuPinningPolicy().isExclusive()) {
                sum += resource.getCpuLoad();
            }
        }

        return sum;
    }
}
