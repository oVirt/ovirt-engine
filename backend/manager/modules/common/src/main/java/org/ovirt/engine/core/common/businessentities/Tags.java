package org.ovirt.engine.core.common.businessentities;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

import org.ovirt.engine.core.common.utils.ValidationUtils;
import org.ovirt.engine.core.compat.Guid;

public class Tags implements Serializable {
    private static final long serialVersionUID = -6566155246916011274L;

    private Guid id;

    @Size(min = 1, max = BusinessEntitiesDefinitions.TAG_NAME_SIZE)
    @Pattern(regexp = ValidationUtils.NO_SPECIAL_CHARACTERS_I18N, message = "VALIDATION_TAGS_INVALID_TAG_NAME")
    private String name;

    @Size(max = BusinessEntitiesDefinitions.GENERAL_MAX_SIZE)
    private String description;

    private Guid parent;

    private Boolean readonly;

    private TagsType type;

    private List<Tags> _children;

    public Tags() {
        _children = new ArrayList<>();
        type = TagsType.GeneralTag;
    }

    public Tags(String description, Guid parent_id, Boolean isReadonly, Guid tag_id, String tag_name) {
        this();
        this.description = description;
        this.parent = parent_id;
        this.readonly = isReadonly;
        this.id = tag_id;
        this.name = tag_name;
    }

    @Override
    public int hashCode() {
        return Objects.hash(
                id,
                _children,
                description,
                parent,
                readonly,
                name,
                type
        );
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof Tags)) {
            return false;
        }
        Tags other = (Tags) obj;
        return Objects.equals(id, other.id)
                && Objects.equals(_children, other._children)
                && Objects.equals(description, other.description)
                && Objects.equals(parent, other.parent)
                && Objects.equals(readonly, other.readonly)
                && Objects.equals(name, other.name)
                && type == other.type;
    }

    public String getDescription() {
        return this.description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Guid getParentId() {
        return this.parent;
    }

    public void setParentId(Guid parent) {
        this.parent = parent;
    }

    public Boolean getIsReadonly() {
        return this.readonly;
    }

    public void setIsReadonly(Boolean readOnly) {
        this.readonly = readOnly;
    }

    public Guid getTagId() {
        return this.id;
    }

    public void setTagId(Guid id) {
        this.id = id;
    }

    public String getTagName() {
        return this.name;
    }

    public void setTagName(String name) {
        this.name = name;
    }

    public TagsType getType() {
        return this.type;
    }

    public void setType(TagsType type) {
        this.type = type;
    }

    public List<Tags> getChildren() {
        return _children;
    }

    public void setChildren(List<Tags> children) {
        _children = children;
    }
}
