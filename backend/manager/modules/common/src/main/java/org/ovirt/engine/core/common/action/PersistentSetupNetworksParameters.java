package org.ovirt.engine.core.common.action;

public class PersistentSetupNetworksParameters extends SetupNetworksParameters {

    private static final long serialVersionUID = 2120379740832172688L;

    private int sequence;

    private int total;

    private String networkNames;

    public PersistentSetupNetworksParameters() {
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
