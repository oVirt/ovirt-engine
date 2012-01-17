package org.ovirt.engine.core.common.queries;

import org.ovirt.engine.core.compat.*;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.NONE)
@XmlType(name = "GetVmsByUseridParameters")
public class GetVmsByUseridParameters extends VdcQueryParametersBase {
    private static final long serialVersionUID = -5292104747871231090L;

    public GetVmsByUseridParameters(Guid id) {
        _id = id;
    }

    @XmlElement(name = "Id")
    private Guid _id = new Guid();

    public Guid getUserId() {
        return _id;
    }

    public GetVmsByUseridParameters() {
    }
}
