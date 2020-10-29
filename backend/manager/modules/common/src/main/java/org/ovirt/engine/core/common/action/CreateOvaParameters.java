package org.ovirt.engine.core.common.action;

import java.util.List;
import java.util.Map;

import org.ovirt.engine.core.common.businessentities.VmEntityType;
import org.ovirt.engine.core.common.businessentities.storage.DiskImage;
import org.ovirt.engine.core.compat.Guid;

public class CreateOvaParameters extends ActionParametersBase {

    public enum Phase {
        MEASURE,
        PACK_OVA,
        TEARDOWN
    }

    private VmEntityType entityType;
    private Guid entityId;
    private List<DiskImage> disks;
    private Guid proxyHostId;
    private String directory;
    private String name;
    private Phase phase = Phase.MEASURE;
    private Map<Guid, String> diskIdToPath;

    public List<DiskImage> getDisks() {
        return disks;
    }

    public void setDisks(List<DiskImage> disks) {
        this.disks = disks;
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

    public Map<Guid, String> getDiskIdToPath() {
        return diskIdToPath;
    }

    public void setDiskIdToPath(Map<Guid, String> diskIdToPath) {
        this.diskIdToPath = diskIdToPath;
    }

}
