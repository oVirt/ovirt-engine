package org.ovirt.engine.core.common.queries;

import org.ovirt.engine.core.common.businessentities.*;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.NONE)
@XmlType(name = "GetAllUsersInVdcRoleParameters")
public class GetAllUsersInVdcRoleParameters extends VdcQueryParametersBase {
    private static final long serialVersionUID = 4075283289991368556L;

    public GetAllUsersInVdcRoleParameters(VdcRole id) {
        _id = id;
    }

    @XmlElement(name = "Id")
    private VdcRole _id = VdcRole.forValue(0);

    public VdcRole getId() {
        return _id;
    }

    public GetAllUsersInVdcRoleParameters() {
    }
}
