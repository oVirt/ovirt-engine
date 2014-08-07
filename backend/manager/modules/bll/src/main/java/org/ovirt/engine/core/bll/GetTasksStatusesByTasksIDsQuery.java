package org.ovirt.engine.core.bll;

import org.ovirt.engine.core.bll.tasks.CommandCoordinatorUtil;
import org.ovirt.engine.core.common.queries.GetTasksStatusesByTasksIDsParameters;

public class GetTasksStatusesByTasksIDsQuery<P extends GetTasksStatusesByTasksIDsParameters>
        extends QueriesCommandBase<P> {
    public GetTasksStatusesByTasksIDsQuery(P parameters) {
        super(parameters);
    }

    @Override
    protected void executeQueryCommand() {
        getQueryReturnValue().setReturnValue(CommandCoordinatorUtil.pollTasks(getParameters().getTasksIDs()));
    }
}
