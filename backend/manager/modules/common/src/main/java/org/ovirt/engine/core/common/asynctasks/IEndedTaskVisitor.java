package org.ovirt.engine.core.common.asynctasks;

import org.ovirt.engine.core.common.action.*;

public interface IEndedTaskVisitor {
    boolean Visit(EndedTaskInfo taskInfo, VdcActionParametersBase parameters);
}
