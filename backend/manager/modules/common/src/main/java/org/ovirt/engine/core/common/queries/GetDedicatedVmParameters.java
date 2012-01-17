package org.ovirt.engine.core.common.queries;

import org.ovirt.engine.core.compat.*;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.NONE)
@XmlType(name = "GetDedicatedVmParameters")
public class GetDedicatedVmParameters extends VdcQueryParametersBase {
    private static final long serialVersionUID = 1967442501042226669L;

    public GetDedicatedVmParameters(Guid id) {
        _id = id;
    }

    @XmlElement(name = "Id")
    private Guid _id;

    public Guid getId() {
        return _id;
    }

    public GetDedicatedVmParameters() {
    }
}
