package org.ovirt.engine.core.vdsbroker.vdsbroker;


/**
 * The <code>HostNetworkInterfacesPersister</code> interface defines the actions for persisting network interfaces as
 * reported by vdsm to the ovirt-engine db
 */
public interface HostNetworkInterfacesPersister {

    /**
     * Persists host network changes
     */
    public void persistTopology();
}
