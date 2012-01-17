package org.ovirt.engine.core.common.action;

import org.ovirt.engine.core.compat.*;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;

import java.util.ArrayList;

@XmlAccessorType(XmlAccessType.NONE)
@XmlType(name = "AttachEntityToTagParameters")
public class AttachEntityToTagParameters extends TagsActionParametersBase {
    private static final long serialVersionUID = -180068487863209744L;
    @XmlElement(name = "_entitiesIdGuidArray")
    private java.util.ArrayList<Guid> _entitiesId;

    public AttachEntityToTagParameters(Guid tagId, java.util.ArrayList<Guid> entitiesId) {
        super(tagId);
        _entitiesId = entitiesId;
    }

    public java.util.ArrayList<Guid> getEntitiesId() {
        return _entitiesId == null ? new ArrayList<Guid>() : _entitiesId;
    }

    public AttachEntityToTagParameters() {
    }
}
