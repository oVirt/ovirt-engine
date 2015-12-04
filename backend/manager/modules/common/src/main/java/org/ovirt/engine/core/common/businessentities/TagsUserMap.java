package org.ovirt.engine.core.common.businessentities;

import java.io.Serializable;
import java.util.Objects;

import org.ovirt.engine.core.compat.Guid;

public class TagsUserMap implements Serializable {
    private static final long serialVersionUID = 8616194965200914499L;

    private Guid tagId;
    private Guid userId;

    public TagsUserMap() {
        this (null, Guid.Empty);
    }

    public TagsUserMap(Guid tag_id, Guid user_id) {
        this.tagId = tag_id;
        this.userId = user_id;
    }

    public Guid getTagId() {
        return this.tagId;
    }

    public void setTagId(Guid value) {
        this.tagId = value;
    }

    public Guid getUserId() {
        return this.userId;
    }

    public void setUserId(Guid value) {
        this.userId = value;
    }

    @Override
    public int hashCode() {
        return Objects.hash(
                tagId,
                userId
        );
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof TagsUserMap)) {
            return false;
        }
        TagsUserMap other = (TagsUserMap) obj;
        return Objects.equals(tagId, other.tagId)
                && Objects.equals(userId, other.userId);
    }
}
