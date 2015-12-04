package org.ovirt.engine.core.common.businessentities;

import java.io.Serializable;
import java.util.Objects;

import org.ovirt.engine.core.compat.Guid;

public class TagsVmMap implements Serializable {
    private static final long serialVersionUID = 4061390921955269261L;

    private TagsVmMapId id;

    private Integer defaultDisplayType;

    public TagsVmMap() {
        id = new TagsVmMapId();
        defaultDisplayType = 0;
    }

    public TagsVmMap(Guid tag_id, Guid vm_id) {
        this();
        this.id.tagId = tag_id;
        this.id.vmId = vm_id;
    }

    public Guid getTagId() {
        return this.id.tagId;
    }

    public void setTagId(Guid value) {
        this.id.tagId = value;
    }

    public Guid getVmId() {
        return this.id.vmId;
    }

    public void setVmId(Guid value) {
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
        return Objects.hash(
                id.tagId,
                id.vmId,
                defaultDisplayType
        );
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof TagsVmMap)) {
            return false;
        }
        TagsVmMap other = (TagsVmMap) obj;
        return Objects.equals(id.tagId, other.id.tagId)
                && Objects.equals(id.vmId, other.id.vmId)
                && Objects.equals(defaultDisplayType, other.defaultDisplayType);
    }
}
