package org.ovirt.engine.core.common.businessentities;

import java.io.Serializable;
import java.util.Objects;

import org.ovirt.engine.core.compat.Guid;

public class TagsTemplateMap implements Serializable {
    private static final long serialVersionUID = 4061390921955269261L;

    private TagsTemplateMapId id;

    private Integer defaultDisplayType;

    public TagsTemplateMap() {
        id = new TagsTemplateMapId();
        defaultDisplayType = 0;
    }

    public TagsTemplateMap(Guid tag_id, Guid template_id) {
        this();
        this.id.tagId = tag_id;
        this.id.templateId = template_id;
    }

    public Guid getTagId() {
        return this.id.tagId;
    }

    public void setTagId(Guid value) {
        this.id.tagId = value;
    }

    public Guid getTemplateId() {
        return this.id.templateId;
    }

    public void setTemplateId(Guid value) {
        this.id.templateId = value;
    }

    public Integer getDefaultDisplayType() {
        return this.defaultDisplayType;
    }

    public void setDefaultDisplayType(Integer value) {
        this.defaultDisplayType = value;
    }

    @Override
    public int hashCode() {
        return Objects.hash(
                id.tagId,
                id.templateId,
                defaultDisplayType
        );
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof TagsTemplateMap)) {
            return false;
        }
        TagsTemplateMap other = (TagsTemplateMap) obj;
        return Objects.equals(id.tagId, other.id.tagId)
                && Objects.equals(id.templateId, other.id.templateId)
                && Objects.equals(defaultDisplayType, other.defaultDisplayType);
    }
}
