package org.ovirt.engine.core.bll.scheduling.pending;

import java.util.HashSet;
import java.util.Set;

import org.ovirt.engine.core.bll.scheduling.utils.VmSpecificPendingResourceEqualizer;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.compat.Guid;

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

    @SuppressWarnings("EqualsWhichDoesntCheckParameterClass")
    @Override
    public boolean equals(Object other) {
        return VmSpecificPendingResourceEqualizer.isEqual(this, other);
    }

    @Override
    public int hashCode() {
        return VmSpecificPendingResourceEqualizer.calcHashCode(this);
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
