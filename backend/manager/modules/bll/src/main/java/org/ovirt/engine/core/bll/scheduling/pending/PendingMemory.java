package org.ovirt.engine.core.bll.scheduling.pending;

import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.compat.Guid;

/**
 * Represents a physical memory allocation that is going to be used
 * by not yet started VM on a specified host
 */
public class PendingMemory extends PendingResource {
    long sizeInMb;

    public PendingMemory(VDS host, VM vm, long sizeInMb) {
        super(host, vm);
        this.sizeInMb = sizeInMb;
    }

    public PendingMemory(Guid host, VM vm, long sizeInMb) {
        super(host, vm);
        this.sizeInMb = sizeInMb;
    }

    public long getSizeInMb() {
        return sizeInMb;
    }

    public void setSizeInMb(long sizeInMb) {
        this.sizeInMb = sizeInMb;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        PendingMemory that = (PendingMemory) o;

        return vm.equals(that.vm);

    }

    @Override
    public int hashCode() {
        return vm.hashCode();
    }

    public static int collectForHost(PendingResourceManager manager, Guid host) {
        int sumMb = 0;
        for (PendingMemory resource: manager.pendingHostResources(host, PendingMemory.class)) {
            sumMb += resource.getSizeInMb();
        }

        return sumMb;
    }
}
