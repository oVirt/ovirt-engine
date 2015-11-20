package org.ovirt.engine.core.bll.scheduling.pending;

import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.compat.Guid;

/**
 * Represents a logical memory allocation that is going to be used
 * by not yet started VM on a specified host (this is the maximum
 * amount of memory that might be allocated and we use it for
 * cluster overcommit calculations).
 */
public class PendingOvercommitMemory extends PendingMemory {
    public PendingOvercommitMemory(VDS host,
            VM vm, long sizeInMb) {
        super(host, vm, sizeInMb);
    }

    public PendingOvercommitMemory(Guid host,
            VM vm, long sizeInMb) {
        super(host, vm, sizeInMb);
    }

    public static int collectForHost(PendingResourceManager manager, Guid host) {
        int sumMb = 0;
        for (PendingOvercommitMemory resource: manager.pendingHostResources(host, PendingOvercommitMemory.class)) {
            sumMb += resource.getSizeInMb();
        }

        return sumMb;
    }
}
