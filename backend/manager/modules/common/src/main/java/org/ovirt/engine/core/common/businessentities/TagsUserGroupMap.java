package org.ovirt.engine.core.common.businessentities;

import java.io.Serializable;
import java.util.Objects;

import org.ovirt.engine.core.compat.Guid;

public class TagsUserGroupMap implements Serializable {
    private static final long serialVersionUID = -731433436876337432L;

    private Guid tagId;

    private Guid groupId;

    public TagsUserGroupMap() {
        this (Guid.Empty, null);
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
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        TagsUserGroupMap other = (TagsUserGroupMap) obj;
        return (Objects.equals(groupId, other.groupId)
                && Objects.equals(tagId, other.tagId));
    }
}
