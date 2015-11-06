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
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        TagsUserMap other = (TagsUserMap) obj;
        return (Objects.equals(tagId, other.tagId)
                && Objects.equals(userId, other.userId));
    }
}
