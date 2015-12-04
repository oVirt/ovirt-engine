package org.ovirt.engine.core.common.businessentities;

import java.io.Serializable;
import java.util.Date;
import java.util.Objects;

import org.ovirt.engine.core.compat.Guid;

public class TagsVmPoolMap implements Serializable {
    private static final long serialVersionUID = 1110697686039279639L;

    private TagsVmPoolMapId id;
    private Date created;

    public TagsVmPoolMap() {
        id = new TagsVmPoolMapId();
        created = new Date();
    }

    public Guid getTagId() {
        return this.id.tagId;
    }

    public void setTagId(Guid value) {
        this.id.tagId = value;
    }

    public Guid getVmPoolId() {
        return this.id.vmPoolId;
    }

    public void setVmPoolId(Guid value) {
        this.id.vmPoolId = value;
    }

    @Override
    public int hashCode() {
        return Objects.hash(
                id,
                created
        );
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof TagsVmPoolMap)) {
            return false;
        }
        TagsVmPoolMap other = (TagsVmPoolMap) obj;
        return Objects.equals(id, other.id)
                && Objects.equals(created, other.created);
    }
}
