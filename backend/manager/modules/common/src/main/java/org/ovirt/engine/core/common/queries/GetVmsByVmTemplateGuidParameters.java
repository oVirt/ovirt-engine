package org.ovirt.engine.core.common.queries;

import org.ovirt.engine.core.compat.*;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;

//C# TO JAVA CONVERTER TODO TASK: Java annotations will not correspond to .NET attributes:
@XmlAccessorType(XmlAccessType.NONE)
@XmlType(name = "GetVmsByVmTemplateGuidParameters")
public class GetVmsByVmTemplateGuidParameters extends VdcQueryParametersBase {
    private static final long serialVersionUID = -4308515803583157101L;

    public GetVmsByVmTemplateGuidParameters(Guid id) {
        _id = id;
    }

    // C# TO JAVA CONVERTER TODO TASK: Java annotations will not correspond to
    // .NET attributes:
    @XmlElement(name = "Id")
    private Guid _id = new Guid();

    public Guid getId() {
        return _id;
    }

    public GetVmsByVmTemplateGuidParameters() {
    }
}
