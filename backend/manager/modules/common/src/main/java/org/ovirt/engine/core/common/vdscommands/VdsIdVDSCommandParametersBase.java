package org.ovirt.engine.core.common.vdscommands;

import org.ovirt.engine.core.compat.*;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.NONE)
@XmlType(name = "VdsIdVDSCommandParametersBase")
public class VdsIdVDSCommandParametersBase extends VDSParametersBase {
    public VdsIdVDSCommandParametersBase(Guid vdsId) {
        _vdsId = vdsId;
    }

    @XmlElement
    private Guid _vdsId;

    public Guid getVdsId() {
        return _vdsId;
    }

    public VdsIdVDSCommandParametersBase() {
    }

    @Override
    public String toString() {
        return String.format("vdsId = %s", getVdsId());
    }
}
