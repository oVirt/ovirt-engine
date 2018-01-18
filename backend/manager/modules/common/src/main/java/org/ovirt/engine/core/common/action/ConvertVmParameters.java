package org.ovirt.engine.core.common.action;

import java.util.List;

import org.ovirt.engine.core.common.businessentities.OriginType;
import org.ovirt.engine.core.common.businessentities.network.VmNetworkInterface;
import org.ovirt.engine.core.common.businessentities.storage.DiskImage;
import org.ovirt.engine.core.compat.Guid;

public class ConvertVmParameters extends VmOperationParameterBase {

    private String url;
    private String username;
    private String password;
    private String vmName;
    private List<DiskImage> disks;
    private Guid storagePoolId;
    private Guid storageDomainId;
    private Guid proxyHostId;
    private Guid clusterId;
    private String virtioIsoName;
    private OriginType originType;
    private String compatVersion;
    private List<VmNetworkInterface> networkInterfaces;

    public ConvertVmParameters() {
    }

    public ConvertVmParameters(Guid vmId) {
        super(vmId);
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

    public String getVmName() {
        return vmName;
    }

    public void setVmName(String vmName) {
        this.vmName = vmName;
    }

    public List<DiskImage> getDisks() {
        return disks;
    }

    public void setDisks(List<DiskImage> disks) {
        this.disks = disks;
    }

    public Guid getStoragePoolId() {
        return storagePoolId;
    }

    public void setStoragePoolId(Guid storagePoolId) {
        this.storagePoolId = storagePoolId;
    }

    public Guid getStorageDomainId() {
        return storageDomainId;
    }

    public void setStorageDomainId(Guid storageDomainId) {
        this.storageDomainId = storageDomainId;
    }

    public Guid getProxyHostId() {
        return proxyHostId;
    }

    public void setProxyHostId(Guid proxyHostId) {
        this.proxyHostId = proxyHostId;
    }

    public Guid getClusterId() {
        return clusterId;
    }

    public void setClusterId(Guid clusterId) {
        this.clusterId = clusterId;
    }

    public String getVirtioIsoName() {
        return virtioIsoName;
    }

    public void setVirtioIsoName(String virtioIsoName) {
        this.virtioIsoName = virtioIsoName;
    }

    public OriginType getOriginType() {
        return originType;
    }

    public void setOriginType(OriginType originType) {
        this.originType = originType;
    }

    public String getCompatVersion() {
        return this.compatVersion;
    }

    public void setCompatVersion(String compatVersion) {
        this.compatVersion = compatVersion;
    }

    public List<VmNetworkInterface> getNetworkInterfaces() {
        return networkInterfaces;
    }

    public void setNetworkInterfaces(List<VmNetworkInterface> networkInterfaces) {
        this.networkInterfaces = networkInterfaces;
    }
}
