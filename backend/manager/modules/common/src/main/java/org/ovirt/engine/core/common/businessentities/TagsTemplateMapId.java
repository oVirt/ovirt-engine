package org.ovirt.engine.core.common.businessentities;

import org.ovirt.engine.core.compat.Guid;

import java.io.Serializable;

public class TagsTemplateMapId implements Serializable {
    private static final long serialVersionUID = 3806639687244222549L;

    Guid tagId;
    Guid templateId;

    public TagsTemplateMapId() {
    }

    public TagsTemplateMapId(Guid tagId, Guid templateId) {
        super();
        this.tagId = tagId;
        this.templateId = templateId;
    }
}
