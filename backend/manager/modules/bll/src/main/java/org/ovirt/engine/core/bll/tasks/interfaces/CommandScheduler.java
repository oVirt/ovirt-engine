package org.ovirt.engine.core.bll.tasks.interfaces;

import java.util.concurrent.Future;

import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.common.action.ActionParametersBase;
import org.ovirt.engine.core.common.action.ActionType;
import org.ovirt.engine.core.common.action.VdcReturnValueBase;

public interface CommandScheduler {

    Future<VdcReturnValueBase> executeAsyncCommand(ActionType actionType,
            ActionParametersBase parameters,
            CommandContext cmdContext);

}
