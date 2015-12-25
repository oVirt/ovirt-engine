package org.ovirt.engine.core.bll.scheduling.pending;

import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.compat.Guid;

/**
 * Represents a cpu core allocation that is going to be used
 * by not yet started VM on a specified host
 */
public class PendingCpuCores extends PendingResource {
    int coreCount;

    public PendingCpuCores(VDS host, VM vm, int coreCount) {
        super(host, vm);
        this.coreCount = coreCount;
    }

    public PendingCpuCores(Guid host, VM vm, int coreCount) {
        super(host, vm);
        this.coreCount = coreCount;
    }

    public long getCoreCount() {
        return coreCount;
    }

    public void setCoreCount(int coreCount) {
        this.coreCount = coreCount;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        PendingCpuCores that = (PendingCpuCores) o;

        return vm.equals(that.vm);

    }

    @Override
    public int hashCode() {
        return vm.hashCode();
    }

    public static int collectForHost(PendingResourceManager manager, Guid host) {
        int sum = 0;
        for (PendingCpuCores resource: manager.pendingHostResources(host, PendingCpuCores.class)) {
            sum += resource.getCoreCount();
        }

        return sum;
    }
}
