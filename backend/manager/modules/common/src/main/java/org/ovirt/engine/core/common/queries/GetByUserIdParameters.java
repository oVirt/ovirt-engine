package org.ovirt.engine.core.common.queries;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

import org.ovirt.engine.core.compat.Guid;

/**
 * class for all getById queries by user ids TODO re-factor commands that duplicates this functionality. extend this
 * class if more than id is needed
 */
@XmlAccessorType(XmlAccessType.NONE)
@XmlType(name = "GetByUserIdParameters")
public class GetByUserIdParameters extends VdcQueryParametersBase {
    private static final long serialVersionUID = 1092832045219008933L;
    @XmlElement(name = "userId")
    private Guid userId;

    public GetByUserIdParameters() {
    }

    public GetByUserIdParameters(Guid userId) {
        this.userId = userId;
    }

    public void setUserId(Guid userId) {
        this.userId = userId;
    }

    public Guid getUserId() {
        return userId;
    }

}
