package org.ovirt.engine.core.common.action;

import org.ovirt.engine.core.common.businessentities.*;

public class TagsVmMapParameters extends TagsActionParametersBase {
    private static final long serialVersionUID = 6685955961538163300L;
    private tags_vm_map _tagsVmMap;

    public TagsVmMapParameters(tags_vm_map tagsVmMap) {
        super(tagsVmMap.gettag_id());
        _tagsVmMap = tagsVmMap;
    }

    public tags_vm_map getTagsVmMap() {
        return _tagsVmMap;
    }

    public TagsVmMapParameters() {
    }
}
