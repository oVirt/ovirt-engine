package org.ovirt.engine.core.common.vdscommands;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.ovirt.engine.core.common.businessentities.Entities;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.network.Bond;
import org.ovirt.engine.core.common.utils.ToStringBuilder;

public class HostSetupNetworksVdsCommandParameters extends VdsIdAndVdsVDSCommandParametersBase {
    private List<HostNetwork> networks;
    private Set<String> removedNetworks;
    private List<Bond> bonds;
    private Set<String> removedBonds;
    private boolean rollbackOnFailure;
    private int conectivityTimeout;
    private boolean hostNetworkQosSupported;

    public HostSetupNetworksVdsCommandParameters(VDS host,
            List<HostNetwork> networks,
            Set<String> removedNetworks,
            List<Bond> bonds,
            Set<String> removedBonds) {
        super(host);
        this.networks = (networks == null) ? new ArrayList<HostNetwork>() : networks;
        this.removedNetworks = (removedNetworks == null) ? new HashSet<String>() : removedNetworks;
        this.bonds = (bonds == null) ? new ArrayList<Bond>() : bonds;
        this.removedBonds = (removedBonds == null) ? new HashSet<String>() : removedBonds;
    }

    public HostSetupNetworksVdsCommandParameters() {
    }

    public List<HostNetwork> getNetworks() {
        return networks;
    }

    public void setNetworks(List<HostNetwork> networks) {
        this.networks = networks;
    }

    public Set<String> getRemovedNetworks() {
        return removedNetworks;
    }

    public void setRemovedNetworks(Set<String> removedNetworks) {
        this.removedNetworks = removedNetworks;
    }

    public List<Bond> getBonds() {
        return bonds;
    }

    public void setBonds(List<Bond> bonds) {
        this.bonds = bonds;
    }

    public Set<String> getRemovedBonds() {
        return removedBonds;
    }

    public void setRemovedBonds(Set<String> removedBonds) {
        this.removedBonds = removedBonds;
    }

    public boolean isRollbackOnFailure() {
        return rollbackOnFailure;
    }

    public int getConectivityTimeout() {
        return conectivityTimeout;
    }

    public void setRollbackOnFailure(boolean checkConnectivity) {
        this.rollbackOnFailure = checkConnectivity;
    }

    public void setConectivityTimeout(int conectivityTimeout) {
        this.conectivityTimeout = conectivityTimeout;
    }

    public boolean getHostNetworkQosSupported() {
        return hostNetworkQosSupported;
    }

    public void setHostNetworkQosSupported(boolean hostNetworkQosSupported) {
        this.hostNetworkQosSupported = hostNetworkQosSupported;
    }

    @Override
    protected ToStringBuilder appendAttributes(ToStringBuilder tsb) {
        return super.appendAttributes(tsb)
                .append("rollbackOnFailure", isRollbackOnFailure())
                .append("conectivityTimeout", getConectivityTimeout())
                .append("hostNetworkQosSupported", getHostNetworkQosSupported())
                .append("networks", Entities.collectionToString(getNetworks(), "\t\t"))
                .append("removedNetworks", getRemovedNetworks())
                .append("bonds", Entities.collectionToString(getBonds(), "\t\t"))
                .append("removedBonds", getBonds());
    }
}
