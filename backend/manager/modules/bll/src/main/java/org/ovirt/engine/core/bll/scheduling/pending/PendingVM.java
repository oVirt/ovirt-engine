package org.ovirt.engine.core.bll.scheduling.pending;

import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.compat.Guid;

import java.util.HashSet;
import java.util.Set;

/**
 * Represents a VM that is about to start on a host
 */
public class PendingVM extends PendingResource {
    public PendingVM(VDS host, VM vm) {
        super(host, vm);
    }

    public PendingVM(Guid host, VM vm) {
        super(host, vm);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;

        if (o == null || getClass() != o.getClass())
            return false;

        PendingVM that = (PendingVM) o;

        return vm.equals(that.vm);

    }

    @Override
    public int hashCode() {
        return vm.hashCode();
    }

    public static Set<Guid> collectForHost(PendingResourceManager manager, Guid host) {
        Set<Guid> ids = new HashSet<>();

        for (PendingVM pending: manager.pendingHostResources(host, PendingVM.class)) {
            ids.add(pending.getVm());
        }

        return ids;
    }

    public static Guid getScheduledHost(PendingResourceManager manager, VM vm) {
        PendingVM template = new PendingVM((Guid)null, vm);
        PendingVM pending = manager.getExactPendingResource(template);
        return pending == null ? null : pending.getHost();
    }
}
