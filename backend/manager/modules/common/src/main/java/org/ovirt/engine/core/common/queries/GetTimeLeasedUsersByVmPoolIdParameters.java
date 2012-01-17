package org.ovirt.engine.core.common.queries;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.NONE)
@XmlType(name = "GetTimeLeasedUsersByVmPoolIdParameters")
public class GetTimeLeasedUsersByVmPoolIdParameters extends VdcQueryParametersBase {
    private static final long serialVersionUID = -7330156039187698528L;

    public GetTimeLeasedUsersByVmPoolIdParameters(int id) {
        _id = id;
    }

    @XmlElement(name = "Id")
    private int _id;

    public int getId() {
        return _id;
    }

    public GetTimeLeasedUsersByVmPoolIdParameters() {
    }
}
