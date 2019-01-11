package org.ovirt.engine.core.bll.scheduling.pending;

import org.ovirt.engine.core.bll.scheduling.utils.VmSpecificPendingResourceEqualizer;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.compat.Guid;

public class PendingCpuLoad extends PendingResource {
    private int cpuLoad;

    public PendingCpuLoad(Guid host, VM vm, int cpuLoad) {
        super(host, vm);
        this.cpuLoad = cpuLoad;
    }

    public int getCpuLoad() {
        return cpuLoad;
    }

    public void setCpuLoad(int cpuLoad) {
        this.cpuLoad = cpuLoad;
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
}
