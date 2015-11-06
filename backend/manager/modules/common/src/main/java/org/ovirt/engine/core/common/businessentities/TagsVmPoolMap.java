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

    public Guid gettag_id() {
        return this.id.tagId;
    }

    public void settag_id(Guid value) {
        this.id.tagId = value;
    }

    public Guid getvm_pool_id() {
        return this.id.vmPoolId;
    }

    public void setvm_pool_id(Guid value) {
        this.id.vmPoolId = value;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((id == null) ? 0 : id.hashCode());
        result = prime * result + ((created == null) ? 0 : created.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        TagsVmPoolMap other = (TagsVmPoolMap) obj;
        return (Objects.equals(id, other.id)
                && Objects.equals(created, other.created));
    }
}
