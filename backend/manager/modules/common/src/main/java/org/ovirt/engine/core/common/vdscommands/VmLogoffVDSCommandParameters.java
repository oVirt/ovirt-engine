package org.ovirt.engine.core.common.vdscommands;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

import org.ovirt.engine.core.compat.Guid;

@XmlAccessorType(XmlAccessType.NONE)
@XmlType(name = "VmLogoffVDSCommandParameters")
public class VmLogoffVDSCommandParameters extends VdsAndVmIDVDSParametersBase {
    @XmlElement
    private boolean _force;

    public VmLogoffVDSCommandParameters(Guid vdsId, Guid vmId, boolean force) {
        super(vdsId, vmId);
        _force = force;
    }

    public boolean getForce() {
        return _force;
    }

    public VmLogoffVDSCommandParameters() {
    }

    @Override
    public String toString() {
        return String.format("%s, force=%s", super.toString(), getForce());
    }
}
