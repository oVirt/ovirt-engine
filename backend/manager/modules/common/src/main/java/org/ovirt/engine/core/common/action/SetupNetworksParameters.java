package org.ovirt.engine.core.common.action;

import java.util.ArrayList;
import java.util.List;

import javax.validation.Valid;
import javax.validation.constraints.Min;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

import org.ovirt.engine.core.common.businessentities.VdsNetworkInterface;


@XmlAccessorType(XmlAccessType.NONE)
@XmlType(name = "SetupNetworksParameters")
public class SetupNetworksParameters extends VdsActionParameters {

    @XmlElement(name = "Interfaces")
    @Valid
    private List<VdsNetworkInterface> interfaces;

    @XmlElement(name = "Force")
    private boolean force;

    @XmlElement(name = "CheckConnectivity")
    private boolean checkConnectivity;

    @XmlElement(name = "ConnectivityTimeout")
    @Min(value = 0, message = "NETWORK_CONNECTIVITY_TIMEOUT_NEGATIVE")
    private int conectivityTimeout;

    /**
     * @param interfaces Interfaces that are connected to a network or bond
     */
    public SetupNetworksParameters() {
        this.interfaces = new ArrayList<VdsNetworkInterface>();
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
}

