package org.ovirt.engine.core.common.queries;

import java.util.ArrayList;

import org.ovirt.engine.core.compat.Guid;

public class GetTasksStatusesByTasksIDsParameters extends QueryParametersBase {
    private static final long serialVersionUID = -7279145473727752108L;

    public GetTasksStatusesByTasksIDsParameters(ArrayList<Guid> tasksIDs) {
        _tasksIDs = tasksIDs;
    }

    private ArrayList<Guid> _tasksIDs;

    public ArrayList<Guid> getTasksIDs() {
        return _tasksIDs == null ? new ArrayList<>() : _tasksIDs;
    }

    public GetTasksStatusesByTasksIDsParameters() {
    }
}
