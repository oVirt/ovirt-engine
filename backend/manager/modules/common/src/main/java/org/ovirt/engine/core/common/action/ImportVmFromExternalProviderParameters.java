package org.ovirt.engine.core.common.action;

import java.util.ArrayList;
import java.util.Map;

import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.compat.Guid;

public class ImportVmFromExternalProviderParameters extends ImportVmParameters {
    private static final long serialVersionUID = 3891077822520360466L;

    public enum Phase {
        CREATE_DISKS,
        CONVERT,
        POST_CONVERT
    }

    private String url;
    private String username;
    private String password;
    private Guid proxyHostId;
    private ArrayList<Guid> disks;
    private String virtioIsoName;
    private String externalName;
    private Phase importPhase = Phase.CREATE_DISKS;
    private Map<Guid, Guid> imageMappings;

    public ImportVmFromExternalProviderParameters() {
    }

    public ImportVmFromExternalProviderParameters(VM vm, Guid destStorageDomainId, Guid storagePoolId, Guid clusterId) {
        super(vm, destStorageDomainId, storagePoolId, clusterId);
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

    public String getVirtioIsoName() {
        return virtioIsoName;
    }

    public void setVirtioIsoName(String virtioIsoPath) {
        this.virtioIsoName = virtioIsoPath;
    }

    public String getExternalName() {
        return externalName;
    }

    public void setExternalName(String externalName) {
        this.externalName = externalName;
    }

    public Phase getImportPhase() {
        return importPhase;
    }

    public void setImportPhase(Phase phase) {
        this.importPhase = phase;
    }

    public Map<Guid, Guid> getImageMappings() {
        return imageMappings;
    }

    public void setImageMappings(Map<Guid, Guid> diskMappings) {
        this.imageMappings = diskMappings;
    }
}
