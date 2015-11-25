package org.ovirt.engine.core.common.businessentities;

import java.io.Serializable;
import java.util.Objects;

import org.ovirt.engine.core.compat.Guid;

public class VdsSpmIdMap implements Serializable, BusinessEntity<Guid> {
    private static final long serialVersionUID = 1256284558588450211L;

    public VdsSpmIdMap() {
        storagePoolId = Guid.Empty;
    }

    public VdsSpmIdMap(Guid storagePoolId, Guid vdsId, int vdsSpmId) {
        this.storagePoolId = storagePoolId;
        this.vdsId = vdsId;
        this.vdsSpmId = vdsSpmId;
    }

    private Guid storagePoolId;

    public Guid getStoragePoolId() {
        return this.storagePoolId;
    }

    public void setStoragePoolId(Guid value) {
        this.storagePoolId = value;
    }

    private Guid vdsId;

    @Override
    public Guid getId() {
        return this.vdsId;
    }

    @Override
    public void setId(Guid value) {
        this.vdsId = value;
    }

    private int vdsSpmId;

    public int getVdsSpmId() {
        return this.vdsSpmId;
    }

    public void setVdsSpmId(int value) {
        this.vdsSpmId = value;
    }

    @Override
    public int hashCode() {
        return Objects.hash(storagePoolId, vdsId, vdsSpmId);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof VdsSpmIdMap)) {
            return false;
        }
        VdsSpmIdMap other = (VdsSpmIdMap) obj;
        return Objects.equals(storagePoolId, other.storagePoolId)
                && Objects.equals(vdsId, other.vdsId)
                && vdsSpmId == other.vdsSpmId;
    }
}
