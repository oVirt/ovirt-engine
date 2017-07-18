package org.ovirt.engine.core.bll.scheduling.pending;

import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.compat.Guid;

/**
 * Abstract pending resource. Use as a base and template for trackable
 * pending resources.
 *
 * It is also a good idea to implement static method that talks to the
 * PendingResourceManager and collects a summary value per Host or VM
 * depending on what is needed.
 */
public abstract class PendingResource {
    private Guid host;
    private Guid vm;

    public PendingResource(Guid host, VM vm) {
        this.host = host;
        this.vm = vm.getId();
    }

    public PendingResource(VDS host, VM vm) {
        this.host = host.getId();
        this.vm = vm.getId();
    }

    public Guid getHost() {
        return host;
    }

    public void setHost(VDS host) {
        this.host = host.getId();
    }

    public Guid getVm() {
        return vm;
    }

    public void setVm(VM vm) {
        this.vm = vm.getId();
    }

    /**
     * The logic of equals and hashCode should match two objects if
     * they represent the same resource independently of values that
     * change in time or the current assignment or allocation.
     *
     * This allows us to replace previous (stale) allocation with
     * new value just by calling .put() or .add().
     *
     * Countable resources should use the VM reference in equals
     * and hashCode if the allocated amount depends on the VM
     * configuration or if multiple VMs can "take" from the
     * resource pool at the same time.
     *
     * For example:
     *
     * - Pending VM memory should only use the VM field
     *   (size can change and host assignment as well)
     * - Pending NIC should use Host and the NIC name
     *   (NIC is tied to a host, the allocation to VM can change)
     */
    public abstract boolean equals(Object other);
    public abstract int hashCode();
}
