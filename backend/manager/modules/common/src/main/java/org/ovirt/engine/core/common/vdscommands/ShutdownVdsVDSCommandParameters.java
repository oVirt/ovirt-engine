package org.ovirt.engine.core.common.vdscommands;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

import org.ovirt.engine.core.compat.Guid;

//C# TO JAVA CONVERTER TODO TASK: Java annotations will not correspond to .NET attributes:
@XmlAccessorType(XmlAccessType.NONE)
@XmlType(name = "ShutdownVdsVDSCommandParameters")
public class ShutdownVdsVDSCommandParameters extends VdsIdVDSCommandParametersBase {
    // C# TO JAVA CONVERTER TODO TASK: Java annotations will not correspond to
    // .NET attributes:
    @XmlElement
    private boolean _reboot;

    public ShutdownVdsVDSCommandParameters(Guid vdsId, boolean reboot) {
        super(vdsId);
        _reboot = reboot;
    }

    public boolean getReboot() {
        return _reboot;
    }

    public ShutdownVdsVDSCommandParameters() {
    }

    @Override
    public String toString() {
        return String.format("%s, reboot=%s", super.toString(), getReboot());
    }
}
