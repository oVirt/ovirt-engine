package org.ovirt.engine.core.common.businessentities;

import java.io.Serializable;

import org.ovirt.engine.core.common.utils.ObjectUtils;
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

    public Guid gettag_id() {
        return this.id.tagId;
    }

    public void settag_id(Guid value) {
        this.id.tagId = value;
    }

    public Guid gettemplate_id() {
        return this.id.templateId;
    }

    public void settemplate_id(Guid value) {
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
        final int prime = 31;
        int result = 1;
        result = prime * result + ((id.tagId == null) ? 0 : id.tagId.hashCode());
        result = prime * result + ((id.templateId == null) ? 0 : id.templateId.hashCode());
        result = prime * result + ((defaultDisplayType == null) ? 0 : defaultDisplayType.hashCode());
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
        TagsTemplateMap other = (TagsTemplateMap) obj;
        return (ObjectUtils.objectsEqual(id.tagId, other.id.tagId)
                && ObjectUtils.objectsEqual(id.templateId, other.id.templateId)
                && ObjectUtils.objectsEqual(defaultDisplayType, other.defaultDisplayType));
    }
}
