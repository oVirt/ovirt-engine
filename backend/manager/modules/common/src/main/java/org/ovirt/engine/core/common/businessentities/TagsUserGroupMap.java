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
@Table(name = "tags_user_group_map", uniqueConstraints = { @UniqueConstraint(columnNames = { "tag_id", "group_id" }) })
@TypeDef(name = "guid", typeClass = GuidType.class)
@NamedQueries({ @NamedQuery(
        name = "get_all_tags_user_group_maps_for_tag_name",
        query = "select m from tags_user_group_map m, tags t where (m.tagId = t.id) and (t.name = :tag_name)") })
public class TagsUserGroupMap implements Serializable {
    private static final long serialVersionUID = -731433436876337432L;

    @Id
    @Column(name = "tag_id")
    @Type(type = "guid")
    private Guid tagId;

    @Column(name = "group_id")
    @Type(type = "guid")
    private Guid groupId = new Guid();

    @Column(name = "_create_date")
    private Date created = new Date();

    public TagsUserGroupMap() {
    }

    public TagsUserGroupMap(Guid group_id, Guid tag_id) {
        this.groupId = group_id;
        this.tagId = tag_id;
    }

    public Guid getgroup_id() {
        return this.groupId;
    }

    public void setgroup_id(Guid value) {
        this.groupId = value;
    }

    public Guid gettag_id() {
        return this.tagId;
    }

    public void settag_id(Guid value) {
        this.tagId = value;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((groupId == null) ? 0 : groupId.hashCode());
        result = prime * result + ((tagId == null) ? 0 : tagId.hashCode());
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
        TagsUserGroupMap other = (TagsUserGroupMap) obj;
        if (groupId == null) {
            if (other.groupId != null)
                return false;
        } else if (!groupId.equals(other.groupId))
            return false;
        if (tagId == null) {
            if (other.tagId != null)
                return false;
        } else if (!tagId.equals(other.tagId))
            return false;
        return true;
    }
}
