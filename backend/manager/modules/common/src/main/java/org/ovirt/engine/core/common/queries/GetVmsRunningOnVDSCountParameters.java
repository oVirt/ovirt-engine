package org.ovirt.engine.core.common.queries;

import org.ovirt.engine.core.compat.*;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.NONE)
@XmlType(name = "GetVmsRunningOnVDSCountParameters")
public class GetVmsRunningOnVDSCountParameters extends VdcQueryParametersBase {
    private static final long serialVersionUID = 2045250638895753689L;

    public GetVmsRunningOnVDSCountParameters(Guid id) {
        _id = id;
    }

    @XmlElement(name = "Id")
    private Guid _id;

    public Guid getId() {
        return _id;
    }

    public GetVmsRunningOnVDSCountParameters() {
    }
}
