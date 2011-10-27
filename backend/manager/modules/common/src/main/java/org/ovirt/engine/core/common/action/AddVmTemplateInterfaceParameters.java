package org.ovirt.engine.core.common.action;

import javax.validation.Valid;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

import org.ovirt.engine.core.common.businessentities.VmNetworkInterface;
import org.ovirt.engine.core.compat.Guid;

@XmlAccessorType(XmlAccessType.NONE)
@XmlType(name = "AddVmTemplateInterfaceParameters")
public class AddVmTemplateInterfaceParameters extends VmTemplateParametersBase {
    private static final long serialVersionUID = 5177618608953713542L;

    @XmlElement(name = "Interface")
    @Valid
    private VmNetworkInterface _interface;

    public AddVmTemplateInterfaceParameters(Guid vmtId, VmNetworkInterface iface) {
        super(vmtId);
        _interface = iface;
    }

    public VmNetworkInterface getInterface() {
        return _interface;
    }

    public AddVmTemplateInterfaceParameters() {
    }
}
