package org.ovirt.engine.core.common.asynctasks;

import java.io.Serializable;
import java.util.Objects;

import org.ovirt.engine.core.common.VdcObjectType;
import org.ovirt.engine.core.compat.Guid;

public class EntityInfo implements Serializable {

    @Override
    public int hashCode() {
        return Objects.hash(
                id,
                type
        );
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof EntityInfo)) {
            return false;
        }
        EntityInfo other = (EntityInfo) obj;
        return Objects.equals(id, other.id)
                && Objects.equals(type, other.type);
    }

    /**
     *
     */
    private static final long serialVersionUID = 4791138479890279057L;

    private VdcObjectType type;

    public VdcObjectType getType() {
        return type;
    }

    public void setType(VdcObjectType type) {
        this.type = type;
    }

    public Guid getId() {
        return id;
    }

    public void setId(Guid id) {
        this.id = id;
    }

    private Guid id;

    public EntityInfo() {
    }

    public EntityInfo(VdcObjectType type, Guid id) {
        this.type = type;
        this.id = id;

    }

}
