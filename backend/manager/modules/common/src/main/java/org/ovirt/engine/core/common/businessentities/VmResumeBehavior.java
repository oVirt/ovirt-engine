package org.ovirt.engine.core.common.businessentities;

/**
 * If the storage, on which this virtual machine has some disks gets
 * unresponsive, the virtual machine gets paused.
 *
 * These are the possible options, what should happen with the virtual machine
 * in the moment the storage gets available again.
 */
public enum VmResumeBehavior {

    /**
     * The virtual machine gets resumed automatically in the moment the storage is available
     * again.
     *
     * This is the default for VMs which are not HA with a lease.
     */
    AUTO_RESUME,

    /**
     * Do nothing with the virtual machine.
     */
    LEAVE_PAUSED,

    /**
     * The virtual machine will be killed after a timeout (configurable on the hypervisor).
     *
     * This is the only option supported for highly available virtual machines
     * with leases. The reason is that the highly available virtual machine is
     * restarted using the infrastructure and any kind of resume risks
     * split brains.
     */
    KILL
}
