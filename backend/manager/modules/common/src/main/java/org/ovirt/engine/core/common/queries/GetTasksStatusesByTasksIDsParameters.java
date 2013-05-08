package org.ovirt.engine.core.common.queries;

import java.util.ArrayList;

import org.ovirt.engine.core.compat.Guid;

public class GetTasksStatusesByTasksIDsParameters extends VdcQueryParametersBase {
    private static final long serialVersionUID = -7279145473727752108L;

    public GetTasksStatusesByTasksIDsParameters(java.util.ArrayList<Guid> tasksIDs) {
        _tasksIDs = tasksIDs;
    }

    private java.util.ArrayList<Guid> _tasksIDs;

    public java.util.ArrayList<Guid> getTasksIDs() {
        return _tasksIDs == null ? new ArrayList<Guid>() : _tasksIDs;
    }

    public GetTasksStatusesByTasksIDsParameters() {
    }
}
