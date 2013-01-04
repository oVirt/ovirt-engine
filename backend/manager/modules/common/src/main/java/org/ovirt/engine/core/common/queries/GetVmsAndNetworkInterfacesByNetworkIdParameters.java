package org.ovirt.engine.core.common.queries;

import org.ovirt.engine.core.compat.Guid;

public class GetVmsAndNetworkInterfacesByNetworkIdParameters extends IdQueryParameters {

    private static final long serialVersionUID = -3306070454507733795L;

    /**
     * If set, filters by the VM running or not running.
     */
    private Boolean runningVms;

    public GetVmsAndNetworkInterfacesByNetworkIdParameters() {
    }

    /**
     * Create parameters to filter by the given network ID.
     *
     * @param networkId
     *            The network ID to filter the VMs for.
     */
    public GetVmsAndNetworkInterfacesByNetworkIdParameters(Guid networkId) {
        super(networkId);
    }

    /**
     * Create parameters to filter by the given network ID and the VMs that should be running or not.
     *
     * @param networkId
     *            The network ID to filter the VMs for.
     *
     * @param runningVms
     *            <code>true</code> means the returned VMs only those that are running, <code>false</code> means the
     *            returned VMs are only those that are not running.
     */
    public GetVmsAndNetworkInterfacesByNetworkIdParameters(Guid networkId, boolean runningVms) {
        super(networkId);
        this.runningVms = runningVms;
    }

    public Boolean getRunningVms() {
        return runningVms;
    }
}
