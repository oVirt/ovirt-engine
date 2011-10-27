package org.ovirt.engine.core.common.queries;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

import org.ovirt.engine.core.compat.Guid;

@XmlAccessorType(XmlAccessType.NONE)
@XmlType(name = "GetAllVmPoolsAttachedToUserParameters")
public class GetAllVmPoolsAttachedToUserParameters extends VdcQueryParametersBase {
    private static final long serialVersionUID = -6835998142489522597L;

    public GetAllVmPoolsAttachedToUserParameters(Guid userId) {
        this.userId = userId;
    }

    @XmlElement(name = "UserId")
    private Guid userId;

    public Guid getUserId() {
        return userId;
    }

    public GetAllVmPoolsAttachedToUserParameters() {
    }
}
