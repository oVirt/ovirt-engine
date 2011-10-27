package org.ovirt.engine.core.common.action;

import org.ovirt.engine.core.common.businessentities.*;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.NONE)
@XmlType(name = "TagsVmMapParameters")
public class TagsVmMapParameters extends TagsActionParametersBase {
    private static final long serialVersionUID = 6685955961538163300L;
    @XmlElement(name = "TagsVmMap")
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
