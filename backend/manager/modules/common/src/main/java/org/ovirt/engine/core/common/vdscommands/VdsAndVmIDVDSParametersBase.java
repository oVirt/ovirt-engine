package org.ovirt.engine.core.common.vdscommands;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

import org.ovirt.engine.core.compat.Guid;

@XmlAccessorType(XmlAccessType.NONE)
@XmlType(name = "VdsAndVmIDVDSParametersBase")
public class VdsAndVmIDVDSParametersBase extends VdsIdVDSCommandParametersBase {
    @XmlElement
    private Guid _vmId = new Guid();

    public VdsAndVmIDVDSParametersBase(Guid vdsId, Guid vmId) {
        super(vdsId);
        _vmId = vmId;
    }

    public Guid getVmId() {
        return _vmId;
    }

    public VdsAndVmIDVDSParametersBase() {
    }

    @Override
    public String toString() {
        return String.format("%s, vmId=%s", super.toString(), getVmId());
    }
}
