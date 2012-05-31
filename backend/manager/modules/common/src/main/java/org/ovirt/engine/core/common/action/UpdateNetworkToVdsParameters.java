package org.ovirt.engine.core.common.action;

import java.util.ArrayList;

import org.ovirt.engine.core.common.businessentities.VdsNetworkInterface;
import org.ovirt.engine.core.common.businessentities.network;
import org.ovirt.engine.core.compat.Guid;

public class UpdateNetworkToVdsParameters extends AttachNetworkToVdsParameters {
    private static final long serialVersionUID = 5938344434089627682L;

    private ArrayList<VdsNetworkInterface> interfaces;
    private String bondName;

    public UpdateNetworkToVdsParameters() {
    }

    public UpdateNetworkToVdsParameters(Guid vdsId, network net, ArrayList<VdsNetworkInterface> interfaces) {
        super(vdsId, net, null);
        setInterfaces(interfaces);
    }

    public ArrayList<VdsNetworkInterface> getInterfaces() {
        return interfaces == null ? new ArrayList<VdsNetworkInterface>() : interfaces;
    }

    public void setInterfaces(ArrayList<VdsNetworkInterface> value) {
        interfaces = value;
    }

    public String getBondName() {
        return bondName;
    }

    public void setBondName(String value) {
        bondName = value;
    }

}
