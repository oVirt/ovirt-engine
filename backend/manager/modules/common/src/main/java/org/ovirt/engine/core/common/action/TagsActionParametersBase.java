package org.ovirt.engine.core.common.action;

import org.ovirt.engine.core.compat.*;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.NONE)
@XmlType(name = "TagsActionParametersBase")
public class TagsActionParametersBase extends VdcActionParametersBase {
    private static final long serialVersionUID = -799396982675260518L;
    @XmlElement(name = "TagId")
    private Guid _tagId;

    public TagsActionParametersBase(Guid tagId) {
        _tagId = tagId;
    }

    public Guid getTagId() {
        return _tagId;
    }

    public TagsActionParametersBase() {
    }
}
