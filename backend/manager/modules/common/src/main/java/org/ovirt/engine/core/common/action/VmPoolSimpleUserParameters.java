package org.ovirt.engine.core.common.action;

import org.ovirt.engine.core.compat.*;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.NONE)
@XmlType(name = "VmPoolSimpleUserParameters")
public class VmPoolSimpleUserParameters extends VmPoolParametersBase implements java.io.Serializable {
    private static final long serialVersionUID = -956095100193433604L;
    @XmlElement(name = "UserId")
    private Guid _userId = new Guid();

    public VmPoolSimpleUserParameters(NGuid vmPoolId, Guid userId) {
        super(vmPoolId);
        _userId = userId;
    }

    public Guid getUserId() {
        return _userId;
    }

    public VmPoolSimpleUserParameters() {
    }
}
