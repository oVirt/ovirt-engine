package org.ovirt.engine.core.common.businessentities;

import java.io.Serializable;
import java.util.Objects;

import org.ovirt.engine.core.compat.Guid;

public class TagsVdsMap implements Serializable {
    private static final long serialVersionUID = 6203389145081594705L;

    private TagsVdsMapId id;

    public TagsVdsMap() {
        id = new TagsVdsMapId();
    }

    public TagsVdsMap(Guid tag_id, Guid vds_id) {
        this();
        this.id.tagId = tag_id;
        this.id.vdsId = vds_id;
    }

    public Guid gettag_id() {
        return this.id.tagId;
    }

    public void settag_id(Guid value) {
        this.id.tagId = value;
    }

    public Guid getvds_id() {
        return this.id.vdsId;
    }

    public void setvds_id(Guid value) {
        this.id.vdsId = value;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((id.tagId == null) ? 0 : id.tagId.hashCode());
        result = prime * result + ((id.vdsId == null) ? 0 : id.vdsId.hashCode());
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
        TagsVdsMap other = (TagsVdsMap) obj;
        return (Objects.equals(id.tagId, other.id.tagId)
                && Objects.equals(id.vdsId, other.id.vdsId));
    }
}
