package org.ovirt.engine.core.common.businessentities;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.NONE)
@XmlType(name = "license_usage")
public class license_usage implements Serializable {
    private static final long serialVersionUID = 5363822236535990225L;

    public license_usage() {
    }

    public license_usage(java.util.Date date, int license_desktops, int used_license_desktops, int license_sockets,
            int used_license_sockets) {
        this.date = date;
        this.license_desktops = license_desktops;
        this.used_desktops = used_license_desktops;
        this.license_sockets = license_sockets;
        this.used_sockets = used_license_sockets;
    }

    private java.util.Date date = new java.util.Date(0);

    @XmlElement
    public java.util.Date getDate() {
        return this.date;
    }

    public void setDate(java.util.Date value) {
        this.date = value;
    }

    private int license_desktops;

    @XmlElement
    public int getLicenseDesktops() {
        return this.license_desktops;
    }

    public void setLicenseDesktops(int value) {
        this.license_desktops = value;
    }

    private int used_desktops;

    @XmlElement
    public int getUsedDesktops() {
        return this.used_desktops;
    }

    public void setUsedDesktops(int value) {
        this.used_desktops = value;
    }

    @XmlElement
    private int license_sockets;

    public int getLicenseSockets() {
        return this.license_sockets;
    }

    public void setLicenseSockets(int value) {
        this.license_sockets = value;
    }

    @XmlElement
    private int used_sockets;

    public int getUsedSockets() {
        return this.used_sockets;
    }

    public void setUsedSockets(int value) {
        this.used_sockets = value;
    }

}
