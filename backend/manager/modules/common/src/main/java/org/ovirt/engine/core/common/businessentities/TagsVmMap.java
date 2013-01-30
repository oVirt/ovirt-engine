package org.ovirt.engine.core.common.businessentities;

import java.io.Serializable;

import org.ovirt.engine.core.compat.Guid;

public class TagsVmMap implements Serializable {
    private static final long serialVersionUID = 4061390921955269261L;

    private TagsVmMapId id = new TagsVmMapId();

    private Integer defaultDisplayType = 0;

    public TagsVmMap() {
    }

    public TagsVmMap(Guid tag_id, Guid vm_id) {
        this.id.tagId = tag_id;
        this.id.vmId = vm_id;
    }

    public Guid gettag_id() {
        return this.id.tagId;
    }

    public void settag_id(Guid value) {
        this.id.tagId = value;
    }

    public Guid getvm_id() {
        return this.id.vmId;
    }

    public void setvm_id(Guid value) {
        this.id.vmId = value;
    }

    public Integer getDefaultDisplayType() {
        return this.defaultDisplayType;
    }

    public void setDefaultDisplayType(Integer value) {
        this.defaultDisplayType = value;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((defaultDisplayType == null) ? 0 : defaultDisplayType.hashCode());
        result = prime * result + ((id.tagId == null) ? 0 : id.tagId.hashCode());
        result = prime * result + ((id.vmId == null) ? 0 : id.vmId.hashCode());
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
        TagsVmMap other = (TagsVmMap) obj;
        if (defaultDisplayType == null) {
            if (other.defaultDisplayType != null)
                return false;
        } else if (!defaultDisplayType.equals(other.defaultDisplayType))
            return false;
        if (id.tagId == null) {
            if (other.id.tagId != null)
                return false;
        } else if (!id.tagId.equals(other.id.tagId))
            return false;
        if (id.vmId == null) {
            if (other.id.vmId != null)
                return false;
        } else if (!id.vmId.equals(other.id.vmId))
            return false;
        return true;
    }
}
