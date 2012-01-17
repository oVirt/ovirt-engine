package org.ovirt.engine.core.common.vdscommands;

import org.ovirt.engine.core.compat.*;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.NONE)
@XmlType(name = "SPMTaskGuidBaseVDSCommandParameters")
public class SPMTaskGuidBaseVDSCommandParameters extends IrsBaseVDSCommandParameters {
    public SPMTaskGuidBaseVDSCommandParameters(Guid storagePoolId, Guid taskId) {
        super(storagePoolId);
        setTaskId(taskId);
    }

    @XmlElement(name = "TaskId")
    private Guid privateTaskId = new Guid();

    public Guid getTaskId() {
        return privateTaskId;
    }

    private void setTaskId(Guid value) {
        privateTaskId = value;
    }

    public SPMTaskGuidBaseVDSCommandParameters() {
    }

    @Override
    public String toString() {
        return String.format("%s, taskId = %s", super.toString(), getTaskId());
    }
}
