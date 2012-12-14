package org.ovirt.engine.core.common.businessentities;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.AttributeOverride;
import javax.persistence.AttributeOverrides;
import javax.persistence.Column;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.Table;

import org.hibernate.annotations.TypeDef;

import org.ovirt.engine.core.common.businessentities.mapping.GuidType;
import org.ovirt.engine.core.compat.Guid;

@Entity
@Table(name = "tags_vds_map")
@TypeDef(name = "guid", typeClass = GuidType.class)
public class TagsVdsMap implements Serializable {
    private static final long serialVersionUID = 6203389145081594705L;

    @EmbeddedId
    @AttributeOverrides({
        @AttributeOverride(name = "tagId", column = @Column(name = "tag_id")),
        @AttributeOverride(name = "vdsId", column = @Column(name = "vds_id")) })
        private TagsVdsMapId id = new TagsVdsMapId();

    @Column(name = "_create_date")
    private Date created = new Date();

    public TagsVdsMap() {
    }

    public TagsVdsMap(Guid tag_id, Guid vds_id) {
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
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        TagsVdsMap other = (TagsVdsMap) obj;
        if (id.tagId == null) {
            if (other.id.tagId != null)
                return false;
        } else if (!id.tagId.equals(other.id.tagId))
            return false;
        if (id.vdsId == null) {
            if (other.id.vdsId != null)
                return false;
        } else if (!id.vdsId.equals(other.id.vdsId))
            return false;
        return true;
    }
}
