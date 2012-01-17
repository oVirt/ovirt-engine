package org.ovirt.engine.core.common.action;

import org.ovirt.engine.core.compat.*;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.NONE)
@XmlType(name = "MoveTagParameters")
public class MoveTagParameters extends TagsActionParametersBase {
    private static final long serialVersionUID = -6320801761505304462L;
    @XmlElement
    private Guid _newParentId;

    public MoveTagParameters(Guid tagId, Guid newParentId) {
        super(tagId);
        _newParentId = newParentId;
    }

    public Guid getNewParentId() {
        return _newParentId;
    }

    public MoveTagParameters() {
    }
}
