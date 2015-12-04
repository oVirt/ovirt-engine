package org.ovirt.engine.core.common.action;

import org.ovirt.engine.core.common.businessentities.TagsVmMap;

public class TagsVmMapParameters extends TagsActionParametersBase {
    private static final long serialVersionUID = 6685955961538163300L;
    private TagsVmMap _tagsVmMap;

    public TagsVmMapParameters(TagsVmMap tagsVmMap) {
        super(tagsVmMap.getTagId());
        _tagsVmMap = tagsVmMap;
    }

    public TagsVmMap getTagsVmMap() {
        return _tagsVmMap;
    }

    public TagsVmMapParameters() {
    }
}
