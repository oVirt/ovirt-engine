package org.ovirt.engine.core.common.vdscommands;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

import org.ovirt.engine.core.compat.Guid;

//C# TO JAVA CONVERTER TODO TASK: Java annotations will not correspond to .NET attributes:
@XmlAccessorType(XmlAccessType.NONE)
@XmlType(name = "RunVmHyperChannelCommandVDSCommandParameters")
public class RunVmHyperChannelCommandVDSCommandParameters extends VdsAndVmIDVDSParametersBase {
    // C# TO JAVA CONVERTER TODO TASK: Java annotations will not correspond to
    // .NET attributes:
    @XmlElement
    private String _hcCommand;

    public RunVmHyperChannelCommandVDSCommandParameters(Guid vdsId, Guid vmId, String hcCommand) {
        super(vdsId, vmId);
        _hcCommand = hcCommand;
    }

    public String getHcCommand() {
        return _hcCommand;
    }

    public RunVmHyperChannelCommandVDSCommandParameters() {
    }
}
