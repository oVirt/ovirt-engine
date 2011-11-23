package org.ovirt.engine.core.common.action;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

import org.ovirt.engine.core.common.businessentities.VdsNetworkInterface;
import org.ovirt.engine.core.common.businessentities.network;


@XmlAccessorType(XmlAccessType.NONE)
@XmlType(name = "SetupNetworksParameters")
public class SetupNetworksParameters extends VdsActionParameters {
    @XmlElement(name = "Networks")
    private List<network> networks;
    @XmlElement(name = "RemovedNetworks")
    private List<network> removedNetworks;
    @XmlElement(name = "Bonds")
    private List<VdsNetworkInterface> bonds;
    @XmlElement(name = "RemovedBonds")
    private List<VdsNetworkInterface> removedBonds;
    @XmlElement(name = "Interfaces")
    private List<VdsNetworkInterface> interfaces;

    @XmlElement(name = "Force")
    private boolean force;
    @XmlElement(name = "CheckConnectivity")
    private boolean checkConnectivity;
    @XmlElement(name = "ConnectivityTimeout")
    private int conectivityTimeout;

    /**
     * @param networks Added networks only
     * @param removedNetworks Removed networks only
     * @param bonds Added bonds only
     * @param removedBonds Removed networks only
     * @param interfaces Interfaces that are connected to a network or bond
     */
    public SetupNetworksParameters() {
        this.networks = new ArrayList<network>();
        this.removedNetworks = new ArrayList<network>();
        this.bonds = new ArrayList<VdsNetworkInterface>();
        this.removedBonds = new ArrayList<VdsNetworkInterface>();
        this.interfaces = new ArrayList<VdsNetworkInterface>();
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

    public List<network> getRemovedNetworks() {
        return removedNetworks;
    }

    public void setRemovedNetworks(List<network> removedNetworks) {
        this.removedNetworks = removedNetworks;
    }

    public List<VdsNetworkInterface> getRemovedBonds() {
        return removedBonds;
    }

    public void setRemovedBonds(List<VdsNetworkInterface> removedBonds) {
        this.removedBonds = removedBonds;
    }
}

