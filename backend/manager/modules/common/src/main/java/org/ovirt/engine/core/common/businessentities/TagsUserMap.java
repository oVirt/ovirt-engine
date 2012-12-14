package org.ovirt.engine.core.common.businessentities;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import org.hibernate.annotations.Type;
import org.hibernate.annotations.TypeDef;

import org.ovirt.engine.core.common.businessentities.mapping.GuidType;
import org.ovirt.engine.core.compat.Guid;

@Entity
@Table(name = "tags_user_map", uniqueConstraints = { @UniqueConstraint(columnNames = { "tag_id", "user_id" }) })
@TypeDef(name = "guid", typeClass = GuidType.class)
@NamedQueries({ @NamedQuery(name = "all_tag_user_maps_by_tag_name",
        query = "select m from tags_user_map m, tags t where (m.tagId = t.id) and (t.name = :tag_name)") })
public class TagsUserMap implements Serializable {
    private static final long serialVersionUID = 8616194965200914499L;

    @Id
    @Column(name = "tag_id")
    @Type(type = "guid")
    private Guid tagId;

    @Column(name = "user_id")
    @Type(type = "guid")
    private Guid userId = new Guid();

    @Column(name = "_create_date")
    private Date created = new Date();

    public TagsUserMap() {
    }

    public TagsUserMap(Guid tag_id, Guid user_id) {
        this.tagId = tag_id;
        this.userId = user_id;
    }

    public Guid gettag_id() {
        return this.tagId;
    }

    public void settag_id(Guid value) {
        this.tagId = value;
    }

    public Guid getuser_id() {
        return this.userId;
    }

    public void setuser_id(Guid value) {
        this.userId = value;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((tagId == null) ? 0 : tagId.hashCode());
        result = prime * result + ((userId == null) ? 0 : userId.hashCode());
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
        TagsUserMap other = (TagsUserMap) obj;
        if (tagId == null) {
            if (other.tagId != null)
                return false;
        } else if (!tagId.equals(other.tagId))
            return false;
        if (userId == null) {
            if (other.userId != null)
                return false;
        } else if (!userId.equals(other.userId))
            return false;
        return true;
    }
}
