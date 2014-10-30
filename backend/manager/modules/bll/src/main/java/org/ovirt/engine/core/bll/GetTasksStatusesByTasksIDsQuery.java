package org.ovirt.engine.core.bll;

import org.ovirt.engine.core.bll.tasks.CommandCoordinatorUtil;
import org.ovirt.engine.core.common.queries.GetTasksStatusesByTasksIDsParameters;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;

public class GetTasksStatusesByTasksIDsQuery<P extends GetTasksStatusesByTasksIDsParameters>
        extends QueriesCommandBase<P> {
    public GetTasksStatusesByTasksIDsQuery(P parameters) {
        super(parameters);
    }

    @Override
    protected void executeQueryCommand() {
        if (getUser().isAdmin() ||
                DbFacade.getInstance().getAsyncTaskDao().getVdsmTaskIdsByUser(getUserID()).
                        containsAll(getParameters().getTasksIDs())) {
            getQueryReturnValue().setReturnValue(CommandCoordinatorUtil.pollTasks(getParameters().getTasksIDs()));
        } else {
            String errMessage = "Query execution failed due to insufficient permissions. Users can only query tasks started by them.";
            log.error(errMessage);
            getQueryReturnValue().setExceptionString(errMessage);
        }
    }
}
