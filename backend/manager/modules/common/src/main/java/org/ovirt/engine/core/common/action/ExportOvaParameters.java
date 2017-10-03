package org.ovirt.engine.core.common.action;

import java.util.Map;

import javax.validation.constraints.NotNull;

import org.ovirt.engine.core.common.businessentities.VmEntityType;
import org.ovirt.engine.core.common.businessentities.storage.DiskImage;
import org.ovirt.engine.core.compat.Guid;

public class ExportOvaParameters extends ActionParametersBase {

    private static final long serialVersionUID = 4931085923357685965L;

    public enum Phase {
        CREATE_DISKS,
        CREATE_OVA,
        REMOVE_DISKS
    }

    private VmEntityType entityType;
    private Guid entityId;
    private Phase phase = Phase.CREATE_DISKS;
    private Map<DiskImage, DiskImage> diskInfoDestinationMap;
    private Guid proxyHostId;
    @NotNull
    private String directory;
    private String name;

    public ExportOvaParameters() {
        entityType = VmEntityType.VM;
    }

    public VmEntityType getEntityType() {
        return entityType;
    }

    public void setEntityType(VmEntityType entityType) {
        this.entityType = entityType;
    }

    public Guid getEntityId() {
        return entityId;
    }

    public void setEntityId(Guid entityId) {
        this.entityId = entityId;
    }

    public Phase getPhase() {
        return phase;
    }

    public void setPhase(Phase phase) {
        this.phase = phase;
    }

    public Map<DiskImage, DiskImage> getDiskInfoDestinationMap() {
        return diskInfoDestinationMap;
    }

    public void setDiskInfoDestinationMap(Map<DiskImage, DiskImage> diskInfoDestinationMap) {
        this.diskInfoDestinationMap = diskInfoDestinationMap;
    }

    public Guid getProxyHostId() {
        return proxyHostId;
    }

    public void setProxyHostId(Guid proxyHostId) {
        this.proxyHostId = proxyHostId;
    }

    public String getDirectory() {
        return directory;
    }

    public void setDirectory(String path) {
        this.directory = path;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
