package org.ovirt.engine.core.common.action;

import org.ovirt.engine.core.compat.*;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.NONE)
@XmlType(name = "AddVmToPoolParameters")
public class AddVmToPoolParameters extends VmPoolParametersBase {
    private static final long serialVersionUID = 1019066360476623259L;
    @XmlElement(name = "VmId")
    private Guid _vmId = new Guid();

    public AddVmToPoolParameters(Guid vmPoolId, Guid vmId) {
        super(vmPoolId);
        _vmId = vmId;
    }

    public Guid getVmId() {
        return _vmId;
    }

    public AddVmToPoolParameters() {
    }
}
