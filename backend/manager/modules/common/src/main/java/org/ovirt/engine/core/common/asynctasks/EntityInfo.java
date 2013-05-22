package org.ovirt.engine.core.common.asynctasks;

import java.io.Serializable;

import org.ovirt.engine.core.common.VdcObjectType;
import org.ovirt.engine.core.compat.Guid;

public class EntityInfo implements Serializable {

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((id == null) ? 0 : id.hashCode());
        result = prime * result + ((type == null) ? 0 : type.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        EntityInfo other = (EntityInfo) obj;
        if (id == null) {
            if (other.id != null)
                return false;
        } else if (!id.equals(other.id))
            return false;
        if (type != other.type)
            return false;
        return true;
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
