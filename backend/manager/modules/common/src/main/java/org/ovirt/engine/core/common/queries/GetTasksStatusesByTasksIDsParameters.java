package org.ovirt.engine.core.common.queries;

import org.ovirt.engine.core.compat.*;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;
import java.util.ArrayList;

@XmlAccessorType(XmlAccessType.NONE)
@XmlType(name = "GetTasksStatusesByTasksIDsParameters")
public class GetTasksStatusesByTasksIDsParameters extends VdcQueryParametersBase {
    private static final long serialVersionUID = -7279145473727752108L;

    public GetTasksStatusesByTasksIDsParameters(java.util.ArrayList<Guid> tasksIDs) {
        _tasksIDs = tasksIDs;
    }

    @XmlElement(name = "TaskIDsGuidArray")
    private java.util.ArrayList<Guid> _tasksIDs;

    public java.util.ArrayList<Guid> getTasksIDs() {
        return _tasksIDs == null ? new ArrayList<Guid>() : _tasksIDs;
    }

    public GetTasksStatusesByTasksIDsParameters() {
    }
}
