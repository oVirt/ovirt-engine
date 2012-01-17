package org.ovirt.engine.core.common.vdscommands;

import org.ovirt.engine.core.compat.*;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.NONE)
@XmlType(name = "VmMonitorCommandVDSCommandParameters")
public class VmMonitorCommandVDSCommandParameters extends VdsAndVmIDVDSParametersBase {
    @XmlElement
    private String _command;

    public VmMonitorCommandVDSCommandParameters(Guid vdsId, Guid vmId, String command) {
        super(vdsId, vmId);
        _command = command;
    }

    public String getCommand() {
        return _command;
    }

    public VmMonitorCommandVDSCommandParameters() {
    }
}
