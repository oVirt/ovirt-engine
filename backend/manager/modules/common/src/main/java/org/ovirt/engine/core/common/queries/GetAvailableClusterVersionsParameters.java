package org.ovirt.engine.core.common.queries;

import org.ovirt.engine.core.compat.*;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;

//C# TO JAVA CONVERTER TODO TASK: Java annotations will not correspond to .NET attributes:
@XmlAccessorType(XmlAccessType.NONE)
@XmlType(name = "GetAvailableClusterVersionsParameters")
public class GetAvailableClusterVersionsParameters extends VdcQueryParametersBase {
    private static final long serialVersionUID = -4823052256384638102L;
    // C# TO JAVA CONVERTER TODO TASK: Java annotations will not correspond to
    // .NET attributes:
    @XmlElement(name = "VdsGroupId", nillable = true)
    private Guid privateVdsGroupId;

    public Guid getVdsGroupId() {
        return privateVdsGroupId;
    }

    public void setVdsGroupId(Guid value) {
        privateVdsGroupId = value;
    }

    public GetAvailableClusterVersionsParameters() {
    }
}
