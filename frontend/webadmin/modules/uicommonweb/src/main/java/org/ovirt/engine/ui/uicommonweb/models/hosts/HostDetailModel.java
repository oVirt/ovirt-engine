package org.ovirt.engine.ui.uicommonweb.models.hosts;

import java.io.Serializable;

public class HostDetailModel implements Serializable {

    private static final long serialVersionUID = 1L;

    private String name;
    private String address;
    private String password;
    private String fingerprint;

    public HostDetailModel() {

    }

    public HostDetailModel(String address, String shaFingerPrint) {
        setAddress(address);
        setFingerprint(shaFingerPrint);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getFingerprint() {
        return fingerprint;
    }

    public void setFingerprint(String fingerprint) {
        this.fingerprint = fingerprint;
    }
}
