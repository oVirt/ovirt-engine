package org.ovirt.engine.core.common.asynctasks;

import org.ovirt.engine.core.common.action.*;

public class SetTaskGroupStatusVisitor implements IEndedTaskVisitor {
    private boolean _success;

    public SetTaskGroupStatusVisitor(boolean success) {
        _success = success;
    }

    public boolean Visit(EndedTaskInfo taskInfo, VdcActionParametersBase parameters) {
        parameters.setTaskGroupSuccess(_success);
        return false;
    }

    public SetTaskGroupStatusVisitor() {
    }
}
