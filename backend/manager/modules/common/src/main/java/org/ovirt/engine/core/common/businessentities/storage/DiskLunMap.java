package org.ovirt.engine.core.common.businessentities.storage;

import java.util.Objects;

import org.ovirt.engine.core.common.businessentities.BusinessEntity;
import org.ovirt.engine.core.compat.Guid;

public class DiskLunMap implements BusinessEntity<DiskLunMapId> {
    private static final long serialVersionUID = -6528043171116600954L;

    private DiskLunMapId id;

    public DiskLunMap() {
        id = new DiskLunMapId();
    }

    public DiskLunMap(Guid diskId, String lunId) {
        this();
        getId().setDiskId(diskId);
        getId().setLunId(lunId);
    }

    public DiskLunMapId getId() {
        return id;
    }

    public void setId(DiskLunMapId id) {
        this.id = id;
    }

    public Guid getDiskId() {
        return id.getDiskId();
    }

    public void setDiskId(Guid diskId) {
        id.setDiskId(diskId);
    }

    public String getLunId() {
        return id.getLunId();
    }

    public void setLunId(String lunId) {
        id.setLunId(lunId);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof DiskLunMap)) {
            return false;
        }
        DiskLunMap other = (DiskLunMap) obj;
        return Objects.equals(id, other.id);
    }
}
