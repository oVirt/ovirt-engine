package org.ovirt.engine.core.common.action;

import org.ovirt.engine.core.compat.*;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.NONE)
@XmlType(name = "DetachUserFromTimeLeasedPoolParameters")
public class DetachUserFromTimeLeasedPoolParameters extends VmPoolSimpleUserParameters {
    private static final long serialVersionUID = 7826859699180843171L;
    @XmlElement
    private boolean _isInternal;

    public DetachUserFromTimeLeasedPoolParameters(Guid vmPoolId, Guid userId, boolean isInternal) {
        super(vmPoolId, userId);
        _isInternal = isInternal;
    }

    public boolean getIsInternal() {
        return _isInternal;
    }

    public DetachUserFromTimeLeasedPoolParameters() {
    }
}
