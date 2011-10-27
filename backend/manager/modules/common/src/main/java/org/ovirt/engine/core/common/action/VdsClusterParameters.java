package org.ovirt.engine.core.common.action;

import java.util.ArrayList;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

import org.ovirt.engine.core.common.businessentities.VdsNetworkInterface;
import org.ovirt.engine.core.compat.Guid;

@XmlAccessorType(XmlAccessType.NONE)
@XmlType(name = "VdsClusterParameters")
public class VdsClusterParameters extends VdsActionParameters {
    private static final long serialVersionUID = 2865188379201582824L;
    @XmlElement(name = "VdsGroupId")
    private Guid privateVdsGroupId;

    public Guid getVdsGroupId() {
        return privateVdsGroupId;
    }

    private void setVdsGroupId(Guid value) {
        privateVdsGroupId = value;
    }

    @XmlElement(name = "Interfaces")
    private java.util.ArrayList<VdsNetworkInterface> privateInterfaces;

    public java.util.ArrayList<VdsNetworkInterface> getInterfaces() {
        return privateInterfaces == null ? new ArrayList<VdsNetworkInterface>() : privateInterfaces;
    }

    private void setInterfaces(java.util.ArrayList<VdsNetworkInterface> value) {
        privateInterfaces = value;
    }

    public VdsClusterParameters(Guid vdsId, Guid vdsGroupId, java.util.ArrayList<VdsNetworkInterface> interfaces) {
        super(vdsId);
        setVdsGroupId(vdsGroupId);
        setInterfaces(interfaces);
    }

    public VdsClusterParameters() {
    }
}
