package org.ovirt.engine.core.bll;

import java.util.Collection;

import javax.inject.Inject;

import org.ovirt.engine.core.bll.context.EngineContext;
import org.ovirt.engine.core.bll.tasks.CommandCoordinatorUtil;
import org.ovirt.engine.core.common.queries.GetTasksStatusesByTasksIDsParameters;
import org.ovirt.engine.core.compat.Guid;

public class GetTasksStatusesByTasksIDsQuery<P extends GetTasksStatusesByTasksIDsParameters>
        extends QueriesCommandBase<P> {

    @Inject
    private CommandCoordinatorUtil commandCoordinatorUtil;

    public GetTasksStatusesByTasksIDsQuery(P parameters, EngineContext engineContext) {
        super(parameters, engineContext);
    }

    @Override
    protected void executeQueryCommand() {
        boolean userAuthorizedToRunQuery = getUser().isAdmin();

        if (!userAuthorizedToRunQuery) {
            Collection<Guid> userIds = commandCoordinatorUtil.getUserIdsForVdsmTaskIds(getParameters().getTasksIDs());
            boolean tasksOwnedByUser = userIds.size() == 1 && userIds.contains(getUserID());
            // if user ids is empty the tasks have completed
            userAuthorizedToRunQuery = userIds.isEmpty() || tasksOwnedByUser;
        }

        if (userAuthorizedToRunQuery) {
            getQueryReturnValue().setReturnValue(commandCoordinatorUtil.pollTasks(getParameters().getTasksIDs()));
        } else {
            String errMessage = "Query execution failed due to insufficient permissions. Users can only query tasks started by them.";
            log.error(errMessage);
            getQueryReturnValue().setExceptionString(errMessage);
        }
    }

}
