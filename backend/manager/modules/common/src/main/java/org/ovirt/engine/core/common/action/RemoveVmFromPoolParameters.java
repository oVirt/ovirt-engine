package org.ovirt.engine.core.common.action;

import org.ovirt.engine.core.compat.*;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.NONE)
@XmlType(name = "RemoveVmFromPoolParameters")
public class RemoveVmFromPoolParameters extends VmPoolParametersBase {
    private static final long serialVersionUID = -9051384517712295806L;
    @XmlElement(name = "VmId")
    private Guid _vmId = new Guid();

    public RemoveVmFromPoolParameters(Guid vmId) {
        super(Guid.Empty);
        _vmId = vmId;
    }

    public Guid getVmId() {
        return _vmId;
    }

    public RemoveVmFromPoolParameters() {
    }
}
