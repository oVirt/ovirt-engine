package org.ovirt.engine.core.common.vdscommands;

import org.ovirt.engine.core.compat.*;
import org.ovirt.engine.core.common.businessentities.*;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.NONE)
@XmlType(name = "SetVmStatusVDSCommandParameters")
public class SetVmStatusVDSCommandParameters extends VDSParametersBase {
    @XmlElement
    private Guid _vmId = new Guid();
    @XmlElement
    private VMStatus _status = VMStatus.forValue(0);

    public SetVmStatusVDSCommandParameters(Guid vmId, VMStatus status) {
        _vmId = vmId;
        _status = status;
    }

    public Guid getVmId() {
        return _vmId;
    }

    public VMStatus getStatus() {
        return _status;
    }

    public SetVmStatusVDSCommandParameters() {
    }

    @Override
    public String toString() {
        return String.format("vmId = %s, status = %s", getVmId(), getStatus());
    }
}
