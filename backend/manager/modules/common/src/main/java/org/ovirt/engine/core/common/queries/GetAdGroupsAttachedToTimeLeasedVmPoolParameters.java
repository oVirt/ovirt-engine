package org.ovirt.engine.core.common.queries;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;

//C# TO JAVA CONVERTER TODO TASK: Java annotations will not correspond to .NET attributes:
@XmlAccessorType(XmlAccessType.NONE)
@XmlType(name = "GetAdGroupsAttachedToTimeLeasedVmPoolParameters")
public class GetAdGroupsAttachedToTimeLeasedVmPoolParameters extends VdcQueryParametersBase {
    private static final long serialVersionUID = 2696702562297712449L;

    public GetAdGroupsAttachedToTimeLeasedVmPoolParameters(int id) {
        _id = id;
    }

    // C# TO JAVA CONVERTER TODO TASK: Java annotations will not correspond to
    // .NET attributes:
    @XmlElement(name = "Id")
    private int _id;

    public int getId() {
        return _id;
    }

    public GetAdGroupsAttachedToTimeLeasedVmPoolParameters() {
    }
}
