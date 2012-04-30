package org.ovirt.engine.core.common.action;

import java.util.ArrayList;

import org.ovirt.engine.core.common.businessentities.VdsNetworkInterface;
import org.ovirt.engine.core.common.businessentities.network;
import org.ovirt.engine.core.compat.Guid;

public class UpdateNetworkToVdsParameters extends AttachNetworkToVdsParameters {
    private static final long serialVersionUID = 5938344434089627682L;

    public UpdateNetworkToVdsParameters(Guid vdsId, network net, java.util.ArrayList<VdsNetworkInterface> interfaces) {
        super(vdsId, net, null);
        setInterfaces(interfaces);
    }

    private java.util.ArrayList<VdsNetworkInterface> privateInterfaces;

    public java.util.ArrayList<VdsNetworkInterface> getInterfaces() {
        return privateInterfaces == null ? new ArrayList<VdsNetworkInterface>() : privateInterfaces;
    }

    public void setInterfaces(java.util.ArrayList<VdsNetworkInterface> value) {
        privateInterfaces = value;
    }

    private String privateBondName;

    public String getBondName() {
        return privateBondName;
    }

    public void setBondName(String value) {
        privateBondName = value;
    }

    public UpdateNetworkToVdsParameters() {
    }
}
