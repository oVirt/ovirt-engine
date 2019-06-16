package org.ovirt.engine.core.common.action;

import org.ovirt.engine.core.compat.Guid;

public class PersistentHostSetupNetworksParameters extends HostSetupNetworksParameters {
    private static final long serialVersionUID = -854488425356529849L;

    private int sequence;

    private int total;

    private String networkNames;

    public PersistentHostSetupNetworksParameters() {
    }

    public PersistentHostSetupNetworksParameters(Guid hostId) {
        this(hostId, 1, 1);
    }

    public PersistentHostSetupNetworksParameters(Guid hostId, int total, int sequence) {
        super(hostId);
        this.total = total;
        this.sequence = sequence;
    }

    public int getSequence() {
        return sequence;
    }

    public void setSequence(int sequence) {
        this.sequence = sequence;
    }

    public int getTotal() {
        return total;
    }

    public void setTotal(int total) {
        this.total = total;
    }

    public String getNetworkNames() {
        return networkNames;
    }

    public void setNetworkNames(String networkNames) {
        this.networkNames = networkNames;
    }
}
