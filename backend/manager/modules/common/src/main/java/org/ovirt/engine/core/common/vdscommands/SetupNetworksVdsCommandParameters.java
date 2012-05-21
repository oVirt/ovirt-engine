package org.ovirt.engine.core.common.vdscommands;

import java.util.ArrayList;
import java.util.List;

import org.ovirt.engine.core.common.businessentities.VdsNetworkInterface;
import org.ovirt.engine.core.common.businessentities.network;
import org.ovirt.engine.core.compat.Guid;

public class SetupNetworksVdsCommandParameters extends VdsIdVDSCommandParametersBase {
    private List<network> networks;
    private List<String> removedNetworks;
    private List<VdsNetworkInterface> bonds;
    private List<VdsNetworkInterface> removedBonds;
    private List<VdsNetworkInterface> interfaces;

    private boolean force;
    private boolean checkConnectivity;
    private int conectivityTimeout;

    /**
     * @param networks
     *            Added networks only
     * @param removedNetworks
     *            Removed networks only
     * @param bonds
     *            Added bonds only
     * @param removedBonds
     *            Removed networks only
     * @param interfaces
     *            Interfaces that are connected to a network or bond
     */
    public SetupNetworksVdsCommandParameters(Guid vdsId,
            List<network> networks,
            List<String> removedNetworks,
            List<VdsNetworkInterface> bonds,
            List<VdsNetworkInterface> removedBonds,
            List<VdsNetworkInterface> interfaces) {
        super(vdsId);
        this.networks = (networks == null) ? new ArrayList<network>() : networks;
        this.removedNetworks = (removedNetworks == null) ? new ArrayList<String>() : removedNetworks;
        this.bonds = (bonds == null) ? new ArrayList<VdsNetworkInterface>() : bonds;
        this.removedBonds = (removedBonds == null) ? new ArrayList<VdsNetworkInterface>() : removedBonds;
        this.interfaces = (interfaces == null) ? new ArrayList<VdsNetworkInterface>() : interfaces;
    }

    public List<network> getNetworks() {
        return networks;
    }

    public void setNetworks(List<network> networks) {
        this.networks = networks;
    }

    public List<VdsNetworkInterface> getBonds() {
        return bonds;
    }

    public void setBonds(List<VdsNetworkInterface> bonds) {
        this.bonds = bonds;
    }

    public List<VdsNetworkInterface> getInterfaces() {
        return interfaces;
    }

    public void setInterfaces(List<VdsNetworkInterface> interfaces) {
        this.interfaces = interfaces;
    }

    public boolean isForce() {
        return force;
    }

    public boolean isCheckConnectivity() {
        return checkConnectivity;
    }

    public int getConectivityTimeout() {
        return conectivityTimeout;
    }

    public void setForce(boolean force) {
        this.force = force;
    }

    public void setCheckConnectivity(boolean checkConnectivity) {
        this.checkConnectivity = checkConnectivity;
    }

    public void setConectivityTimeout(int conectivityTimeout) {
        this.conectivityTimeout = conectivityTimeout;
    }

    public List<String> getRemovedNetworks() {
        return removedNetworks;
    }

    public void setRemovedNetworks(List<String> removedNetworks) {
        this.removedNetworks = removedNetworks;
    }

    public List<VdsNetworkInterface> getRemovedBonds() {
        return removedBonds;
    }

    public void setRemovedBonds(List<VdsNetworkInterface> removedBonds) {
        this.removedBonds = removedBonds;
    }
}
