package org.ovirt.engine.core.common.action;

import java.util.ArrayList;

import org.ovirt.engine.core.compat.Guid;

public class ImportVmFromExternalProviderParameters extends ImportVmParameters {

    private String url;
    private String username;
    private String password;
    private Guid proxyHostId;
    private ArrayList<Guid> disks;

    public ImportVmFromExternalProviderParameters() {
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Guid getProxyHostId() {
        return proxyHostId;
    }

    public void setProxyHostId(Guid proxy) {
        this.proxyHostId = proxy;
    }

    public ArrayList<Guid> getDisks() {
        return disks;
    }

    public void setDisks(ArrayList<Guid> disks) {
        this.disks = disks;
    }
}
