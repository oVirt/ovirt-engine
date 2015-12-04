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

    public Guid getGroupId() {
        return this.groupId;
    }

    public void setGroupId(Guid value) {
        this.groupId = value;
    }

    public Guid getTagId() {
        return this.tagId;
    }

    public void setTagId(Guid value) {
        this.tagId = value;
    }

    @Override
    public int hashCode() {
        return Objects.hash(
                groupId,
                tagId
        );
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof TagsUserGroupMap)) {
            return false;
        }
        TagsUserGroupMap other = (TagsUserGroupMap) obj;
        return Objects.equals(groupId, other.groupId)
                && Objects.equals(tagId, other.tagId);
    }
}
