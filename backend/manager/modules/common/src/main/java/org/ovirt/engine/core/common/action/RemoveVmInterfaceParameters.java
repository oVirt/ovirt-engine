package org.ovirt.engine.core.common.action;

import org.ovirt.engine.core.compat.*;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.NONE)
@XmlType(name = "RemoveVmInterfaceParameters")
public class RemoveVmInterfaceParameters extends VmOperationParameterBase {
    private static final long serialVersionUID = -1016346012853472189L;
    @XmlElement(name = "InterfaceId")
    private Guid _interfaceId = new Guid();

    public RemoveVmInterfaceParameters(Guid vmId, Guid ifaceId) {
        super(vmId);
        _interfaceId = ifaceId;
    }

    public Guid getInterfaceId() {
        return _interfaceId;
    }

    public RemoveVmInterfaceParameters() {
    }
}
