package org.ovirt.engine.core.common.action;

import org.ovirt.engine.core.compat.*;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.NONE)
@XmlType(name = "VmToAdElementParameters")
public class VmToAdElementParameters extends AdElementParametersBase {
    private static final long serialVersionUID = -7146905191066527540L;
    @XmlElement(name = "VmId")
    private Guid _vmId = new Guid();

    public VmToAdElementParameters(Guid adElementId, Guid vmId) {
        super(adElementId);
        _vmId = vmId;
    }

    public Guid getVmId() {
        return _vmId;
    }

    public VmToAdElementParameters() {
    }
}
