package org.ovirt.engine.core.common.action;

import java.util.HashMap;
import java.util.Map;

import org.ovirt.engine.core.common.businessentities.storage.DiskImage;
import org.ovirt.engine.core.compat.Guid;

public class CreateAllOvaDisksParameters extends ActionParametersBase {

    private Guid entityId;
    private Map<Guid, DiskImage> diskInfoDestinationMap;

    public CreateAllOvaDisksParameters() {
        diskInfoDestinationMap = new HashMap<>();
    }

    public Map<Guid, DiskImage> getDiskInfoDestinationMap() {
        return diskInfoDestinationMap;
    }

    public void setDiskInfoDestinationMap(Map<Guid, DiskImage> diskInfoDestinationMap) {
        this.diskInfoDestinationMap = diskInfoDestinationMap;
    }

    public Guid getEntityId() {
        return entityId;
    }

    public void setEntityId(Guid entityId) {
        this.entityId = entityId;
    }
}
