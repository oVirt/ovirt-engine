package org.ovirt.engine.core.common.vdscommands;

import java.util.List;

import org.ovirt.engine.core.common.businessentities.storage.DiskImage;
import org.ovirt.engine.core.common.utils.ToStringBuilder;
import org.ovirt.engine.core.compat.Guid;

public class ConvertVmVDSParameters extends VdsIdVDSCommandParametersBase {

    private String url;
    private String username;
    private String password;
    private String vmName;
    private List<DiskImage> disks;
    private Guid storagePoolId;
    private Guid storageDomainId;
    private Guid vmId;
    private String virtioIsoPath;
    private String compatVersion;

    public ConvertVmVDSParameters() {
    }

    public ConvertVmVDSParameters(Guid vdsId) {
        super(vdsId);
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

    public void setStorageDomainId(Guid storageDomain) {
        this.storageDomainId = storageDomain;
    }

    public Guid getVmId() {
        return vmId;
    }

    public void setVmId(Guid vmId) {
        this.vmId = vmId;
    }

    public String getVirtioIsoPath() {
        return virtioIsoPath;
    }

    public void setVirtioIsoPath(String virtioIsoPath) {
        this.virtioIsoPath = virtioIsoPath;
    }

    public String getCompatVersion() {
        return this.compatVersion;
    }

    public void setCompatVersion(String compatVersion) {
        this.compatVersion = compatVersion;
    }

    @Override
    protected ToStringBuilder appendAttributes(ToStringBuilder tsb) {
        ToStringBuilder builder = super.appendAttributes(tsb)
                .append("url", getUrl())
                .append("username", getUsername())
                .append("vmId", getVmId())
                .append("vmName", getVmName())
                .append("storageDomainId", getStorageDomainId())
                .append("storagePoolId", getStoragePoolId())
                .append("virtioIsoPath", getVirtioIsoPath())
                .append("compatVersion", getCompatVersion());
        for (int i=0; i<getDisks().size(); ++i) {
            builder.append(String.format("Disk%d", i), getDisks().get(i).getId());
        }
        return builder;
    }
}
