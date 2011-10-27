package org.ovirt.engine.core.common.action;

import org.ovirt.engine.core.compat.*;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;

import java.util.ArrayList;

//VB & C# TO JAVA CONVERTER TODO TASK: Java annotations will not correspond to .NET attributes:
@XmlAccessorType(XmlAccessType.NONE)
@XmlType(name = "AttachVdsToTagParameters")
public class AttachVdsToTagParameters extends TagsActionParametersBase {
    private static final long serialVersionUID = -6599471346607548452L;
    // VB & C# TO JAVA CONVERTER TODO TASK: Java annotations will not correspond
    // to .NET attributes:
    @XmlElement
    private java.util.ArrayList<Guid> _entitiesId;

    public AttachVdsToTagParameters(Guid tagId, java.util.ArrayList<Guid> entitiesId) {
        super(tagId);
        _entitiesId = entitiesId;
    }

    public java.util.ArrayList<Guid> getEntitiesId() {
        return _entitiesId == null ? new ArrayList<Guid>() : _entitiesId;
    }

    public AttachVdsToTagParameters() {
    }
}
