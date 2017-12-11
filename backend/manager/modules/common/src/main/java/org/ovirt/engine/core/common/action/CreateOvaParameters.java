package org.ovirt.engine.core.common.action;

import java.util.Map;

import org.ovirt.engine.core.common.businessentities.VmEntityType;
import org.ovirt.engine.core.common.businessentities.storage.DiskImage;
import org.ovirt.engine.core.compat.Guid;

public class CreateOvaParameters extends ActionParametersBase {

    private VmEntityType entityType;
    private Guid entityId;
    private Map<DiskImage, DiskImage> diskInfoDestinationMap;
    private Guid proxyHostId;
    private String directory;
    private String name;

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

}
