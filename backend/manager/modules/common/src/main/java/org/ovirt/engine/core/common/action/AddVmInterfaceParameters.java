package org.ovirt.engine.core.common.action;

import javax.validation.Valid;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

import org.ovirt.engine.core.common.businessentities.VmNetworkInterface;
import org.ovirt.engine.core.compat.Guid;

@XmlAccessorType(XmlAccessType.NONE)
@XmlType(name = "AddVmInterfaceParameters")
public class AddVmInterfaceParameters extends VmOperationParameterBase {
    private static final long serialVersionUID = -816057026613138800L;
    @XmlElement(name = "Interface")
    @Valid
    private VmNetworkInterface _interface;

    public AddVmInterfaceParameters(Guid vmId, VmNetworkInterface iface) {
        super(vmId);
        _interface = iface;
    }

    public VmNetworkInterface getInterface() {
        return _interface;
    }

    public AddVmInterfaceParameters() {
    }
}
