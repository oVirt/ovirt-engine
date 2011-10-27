package org.ovirt.engine.core.common.vdscommands;

import org.ovirt.engine.core.compat.*;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;

//C# TO JAVA CONVERTER TODO TASK: Java annotations will not correspond to .NET attributes:
@XmlAccessorType(XmlAccessType.NONE)
@XmlType(name = "ResetIrsVDSCommandParameters")
public class ResetIrsVDSCommandParameters extends IrsBaseVDSCommandParameters {
    public ResetIrsVDSCommandParameters(Guid storagePoolId, String hostName, Guid vdsId) {
        super(storagePoolId);
        setVdsId(vdsId);
        _hostName = hostName;
    }

    // C# TO JAVA CONVERTER TODO TASK: Java annotations will not correspond to
    // .NET attributes:
    @XmlElement
    private String _hostName;

    // C# TO JAVA CONVERTER TODO TASK: Java annotations will not correspond to
    // .NET attributes:
    @XmlElement
    private Guid privateVdsId;

    public Guid getVdsId() {
        return privateVdsId;
    }

    public void setVdsId(Guid value) {
        privateVdsId = value;
    }

    public String getHostName() {
        return _hostName;
    }

    // C# TO JAVA CONVERTER TODO TASK: Java annotations will not correspond to
    // .NET attributes:
    @XmlElement(name = "IgnoreStopFailed")
    private boolean privateIgnoreStopFailed;

    public boolean getIgnoreStopFailed() {
        return privateIgnoreStopFailed;
    }

    public void setIgnoreStopFailed(boolean value) {
        privateIgnoreStopFailed = value;
    }

    public ResetIrsVDSCommandParameters() {
    }

    @Override
    public String toString() {
        return String.format("%s, hostName = %s, vdsId = %s, ignoreStopFailed = %s",
                super.toString(),
                getHostName(),
                getVdsId(),
                getIgnoreStopFailed());
    }
}
