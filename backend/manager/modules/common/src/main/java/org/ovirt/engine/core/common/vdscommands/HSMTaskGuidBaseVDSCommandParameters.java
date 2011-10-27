package org.ovirt.engine.core.common.vdscommands;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

import org.ovirt.engine.core.compat.Guid;

//C# TO JAVA CONVERTER TODO TASK: Java annotations will not correspond to .NET attributes:
@XmlAccessorType(XmlAccessType.NONE)
@XmlType(name = "HSMTaskGuidBaseVDSCommandParameters")
public class HSMTaskGuidBaseVDSCommandParameters extends VdsIdVDSCommandParametersBase {
    public HSMTaskGuidBaseVDSCommandParameters(Guid vdsId, Guid taskId) {
        super(vdsId);
        setTaskId(taskId);
    }

    // C# TO JAVA CONVERTER TODO TASK: Java annotations will not correspond to
    // .NET attributes:
    @XmlElement(name = "TaskId")
    private Guid privateTaskId = new Guid();

    public Guid getTaskId() {
        return privateTaskId;
    }

    private void setTaskId(Guid value) {
        privateTaskId = value;
    }

    public HSMTaskGuidBaseVDSCommandParameters() {
    }

    @Override
    public String toString() {
        return String.format("%s, taskId=%s", super.toString(), getTaskId());
    }
}
