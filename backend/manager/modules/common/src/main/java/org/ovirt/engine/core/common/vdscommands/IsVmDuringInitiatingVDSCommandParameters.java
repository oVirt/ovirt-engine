package org.ovirt.engine.core.common.vdscommands;

import org.ovirt.engine.core.compat.*;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;

//C# TO JAVA CONVERTER TODO TASK: Java annotations will not correspond to .NET attributes:
@XmlAccessorType(XmlAccessType.NONE)
@XmlType(name = "IsVmDuringInitiatingVDSCommandParameters")
public class IsVmDuringInitiatingVDSCommandParameters extends VDSParametersBase {
    public IsVmDuringInitiatingVDSCommandParameters(Guid vmId) {
        _vmId = vmId;
    }

    // C# TO JAVA CONVERTER TODO TASK: Java annotations will not correspond to
    // .NET attributes:
    @XmlElement
    private Guid _vmId = new Guid();

    public Guid getVmId() {
        return _vmId;
    }

    public IsVmDuringInitiatingVDSCommandParameters() {
    }

    @Override
    public String toString() {
        return String.format("vmId = %s", getVmId());
    }
}
