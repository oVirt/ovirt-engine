package org.ovirt.engine.core.common.queries;

import org.ovirt.engine.core.compat.*;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.NONE)
@XmlType(name = "GetVmsRunningOnVDSParameters")
public class GetVmsRunningOnVDSParameters extends VdcQueryParametersBase {
    private static final long serialVersionUID = 2940416694903953509L;

    public GetVmsRunningOnVDSParameters(Guid id) {
        _id = id;
    }

    @XmlElement(name = "Id")
    private Guid _id;

    public Guid getId() {
        return _id;
    }

    public GetVmsRunningOnVDSParameters() {
    }
}
