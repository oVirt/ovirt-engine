package org.ovirt.engine.core.common.action;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

import org.ovirt.engine.core.compat.Guid;

@XmlAccessorType(XmlAccessType.NONE)
@XmlType(name = "PowerClientMigrateOnConnectCheckParameters")
public class PowerClientMigrateOnConnectCheckParameters extends MigrateVmParameters {
    private static final long serialVersionUID = -7195616799472809959L;
    @XmlElement(name = "ClientIp")
    private String privateClientIp;

    public String getClientIp() {
        return privateClientIp;
    }

    private void setClientIp(String value) {
        privateClientIp = value;
    }

    @XmlElement(name = "VdsId")
    private Guid privateVdsId;

    public Guid getVdsId() {
        return privateVdsId;
    }

    private void setVdsId(Guid value) {
        privateVdsId = value;
    }

    public PowerClientMigrateOnConnectCheckParameters(boolean force, Guid vmId, String clientIp, Guid vdsId) {
        super(force, vmId);
        setClientIp(clientIp);
        setVdsId(vdsId);
    }

    public PowerClientMigrateOnConnectCheckParameters() {
    }
}
