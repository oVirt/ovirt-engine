package org.ovirt.engine.core.common.action;

import org.ovirt.engine.core.compat.*;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.NONE)
@XmlType(name = "DetachAdGroupFromTimeLeasedPoolParameters")
public class DetachAdGroupFromTimeLeasedPoolParameters extends VmPoolToAdElementParameters {
    private static final long serialVersionUID = -6203708281726089292L;
    @XmlElement
    private boolean _isInternal;

    public DetachAdGroupFromTimeLeasedPoolParameters(Guid adElementId, Guid vmPoolId, boolean isInternal) {
        super(adElementId, vmPoolId);
        _isInternal = isInternal;
    }

    public boolean getIsInternal() {
        return _isInternal;
    }

    public DetachAdGroupFromTimeLeasedPoolParameters() {
    }
}
