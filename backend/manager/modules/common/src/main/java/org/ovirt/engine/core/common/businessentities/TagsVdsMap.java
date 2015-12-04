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

    public Guid getTagId() {
        return this.id.tagId;
    }

    public void setTagId(Guid value) {
        this.id.tagId = value;
    }

    public Guid getVdsId() {
        return this.id.vdsId;
    }

    public void setVdsId(Guid value) {
        this.id.vdsId = value;
    }

    @Override
    public int hashCode() {
        return Objects.hash(
                id.tagId,
                id.vdsId
        );
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof TagsVdsMap)) {
            return false;
        }
        TagsVdsMap other = (TagsVdsMap) obj;
        return Objects.equals(id.tagId, other.id.tagId)
                && Objects.equals(id.vdsId, other.id.vdsId);
    }
}
