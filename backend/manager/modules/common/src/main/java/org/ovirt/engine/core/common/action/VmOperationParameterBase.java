package org.ovirt.engine.core.common.action;

import org.ovirt.engine.core.compat.*;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.NONE)
@XmlType(name = "VmOperationParameterBase")
public class VmOperationParameterBase extends VdcActionParametersBase implements java.io.Serializable {
    private static final long serialVersionUID = -6248335374537898949L;

    public VmOperationParameterBase() {
    }

    @XmlElement(name = "VmId")
    private Guid _vmId = new Guid();

    public VmOperationParameterBase(Guid vmId) {
        _vmId = vmId;
    }

    public Guid getVmId() {
        return _vmId;
    }

    public void setVmId(Guid value) {
        _vmId = value;
    }

}
