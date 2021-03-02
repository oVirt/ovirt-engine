package org.ovirt.engine.ui.uicommonweb.models.hosts;

import java.io.Serializable;

public class HostDetailModel implements Serializable {

    private static final long serialVersionUID = 1L;

    private String name;
    private String address;
    private String password;
    private String glusterPeerAddress;
    private String glusterPeerAddressSSHPublicKey;
    private String sshPublicKey;

    public HostDetailModel() {

    }

    public HostDetailModel(String address, String sshPublicKeyPem) {
        setAddress(address);
        setGlusterPeerAddress(address);
        setSshPublicKey(sshPublicKeyPem);
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

    public String getGlusterPeerAddress() {
        return glusterPeerAddress;
    }

    public void setGlusterPeerAddress(String glusterPeerAddress) {
        this.glusterPeerAddress = glusterPeerAddress;
    }

    public void setSshPublicKey(String sshPublicKey) {
        this.sshPublicKey = sshPublicKey;
    }

    public String getSshPublicKey() {
        return sshPublicKey;
    }

    public void setGlusterPeerAddressSSHPublicKey(String glusterPeerAddressSSHPublicKey) {
        this.glusterPeerAddressSSHPublicKey = glusterPeerAddressSSHPublicKey;
    }

    public String getGlusterPeerAddressSSHPublicKey() {
        return glusterPeerAddressSSHPublicKey;
    }
}
