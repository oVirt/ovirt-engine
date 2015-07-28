package org.ovirt.engine.core.common.vdscommands;

import java.util.List;

import org.ovirt.engine.core.common.businessentities.storage.DiskImage;
import org.ovirt.engine.core.common.utils.ToStringBuilder;
import org.ovirt.engine.core.compat.Guid;

public class ConvertOvaVDSParameters extends VdsIdVDSCommandParametersBase {

    private String vmName;
    private List<DiskImage> disks;
    private Guid storagePoolId;
    private Guid storageDomainId;
    private Guid vmId;
    private String virtioIsoPath;
    private String ovaPath;

    public ConvertOvaVDSParameters() {
    }

    public ConvertOvaVDSParameters(Guid vdsId) {
        super(vdsId);
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

    public String getOvaPath() {
        return ovaPath;
    }

    public void setOvaPath(String ovaPath) {
        this.ovaPath = ovaPath;
    }

    public String getVmName() {
        return vmName;
    }

    public void setVmName(String vmName) {
        this.vmName = vmName;
    }

    @Override
    protected ToStringBuilder appendAttributes(ToStringBuilder tsb) {
        ToStringBuilder builder = super.appendAttributes(tsb)
                .append("ovaPath", getOvaPath())
                .append("vmName", getVmName())
                .append("storageDomainId", getStorageDomainId())
                .append("storagePoolId", getStoragePoolId())
                .append("virtioIsoPath", getVirtioIsoPath());
        for (int i=0; i<getDisks().size(); ++i) {
            builder.append(String.format("Disk%d", i), getDisks().get(i).getId());
        }
        return builder;
    }

}
