package org.ovirt.engine.core.common.queries;

import org.ovirt.engine.core.compat.*;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.NONE)
@XmlType(name = "GetUsersByVmidParameters")
public class GetUsersByVmidParameters extends VdcQueryParametersBase {
    private static final long serialVersionUID = 8439269144799226825L;

    public GetUsersByVmidParameters(Guid id) {
        _id = id;
    }

    @XmlElement(name = "Id")
    private Guid _id = new Guid();

    public Guid getVmId() {
        return _id;
    }

    public GetUsersByVmidParameters() {
    }
}
