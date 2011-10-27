package org.ovirt.engine.core.common.action;

import org.ovirt.engine.core.common.businessentities.*;
import javax.validation.Valid;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.NONE)
@XmlType(name = "TagsOperationParameters")
public class TagsOperationParameters extends TagsActionParametersBase {
    private static final long serialVersionUID = 4931970264921707074L;
    @XmlElement
    @Valid
    private tags _tag;

    public TagsOperationParameters(tags tag) {
        super(tag.gettag_id());
        _tag = tag;
    }

    public tags getTag() {
        return _tag;
    }

    public TagsOperationParameters() {
    }
}
