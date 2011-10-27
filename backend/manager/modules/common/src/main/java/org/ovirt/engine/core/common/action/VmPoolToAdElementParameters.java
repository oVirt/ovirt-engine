package org.ovirt.engine.core.common.action;

import org.ovirt.engine.core.compat.*;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.NONE)
@XmlType(name = "VmPoolToAdElementParameters")
public class VmPoolToAdElementParameters extends AdElementParametersBase {
    private static final long serialVersionUID = 8877429811876287907L;
    @XmlElement
    private Guid _vmPoolId;

    public VmPoolToAdElementParameters(Guid adElementId, Guid vmPoolId) {
        super(adElementId);
        _vmPoolId = vmPoolId;
    }

    public Guid getVmPoolId() {
        return _vmPoolId;
    }

    public VmPoolToAdElementParameters() {
    }
}
