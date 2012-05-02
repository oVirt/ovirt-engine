package org.ovirt.engine.core.common.asynctasks;

import org.ovirt.engine.core.common.action.VdcActionParametersBase;

public class SetTaskGroupStatusVisitor implements IEndedTaskVisitor {

    /**
     * Set the task success by the task status, but only when the command id parameters correspond with the task command
     * id.
     *
     * @param taskInfo
     *            Task info containing command ID & success indication.
     * @param parameters
     *            Command parameters to set success for.
     */
    public boolean Visit(EndedTaskInfo taskInfo, VdcActionParametersBase parameters) {
        if (parameters.getCommandId().equals(taskInfo.getTaskParameters().getDbAsyncTask().getCommandId())) {
            parameters.setTaskGroupSuccess(taskInfo.getTaskStatus().getTaskEndedSuccessfully());
        }

        return false;
    }
}
