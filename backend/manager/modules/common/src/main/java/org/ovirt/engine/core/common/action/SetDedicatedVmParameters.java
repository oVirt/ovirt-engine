package org.ovirt.engine.core.common.action;

import org.ovirt.engine.core.compat.*;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.NONE)
@XmlType(name = "SetDedicatedVmParameters")
public class SetDedicatedVmParameters extends VmOperationParameterBase {
    private static final long serialVersionUID = 8019813360303861204L;
    @XmlElement(name = "VdsId")
    private Guid _vdsId;

    public SetDedicatedVmParameters(Guid vmId, Guid vdsId) {
        super(vmId);
        _vdsId = vdsId;
    }

    public Guid getVdsId() {
        return _vdsId;
    }

    public SetDedicatedVmParameters() {
    }
}
