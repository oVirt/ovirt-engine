package org.ovirt.engine.core.common.vdscommands;

import org.ovirt.engine.core.compat.*;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;

//C# TO JAVA CONVERTER TODO TASK: Java annotations will not correspond to .NET attributes:
@XmlAccessorType(XmlAccessType.NONE)
@XmlType(name = "VmMonitorCommandVDSCommandParameters")
public class VmMonitorCommandVDSCommandParameters extends VdsAndVmIDVDSParametersBase {
    // C# TO JAVA CONVERTER TODO TASK: Java annotations will not correspond to
    // .NET attributes:
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
