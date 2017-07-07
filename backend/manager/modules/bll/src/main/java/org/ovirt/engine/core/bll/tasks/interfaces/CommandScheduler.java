package org.ovirt.engine.core.bll.tasks.interfaces;

import java.util.concurrent.Future;

import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.common.action.ActionParametersBase;
import org.ovirt.engine.core.common.action.ActionReturnValue;
import org.ovirt.engine.core.common.action.ActionType;

public interface CommandScheduler {

    Future<ActionReturnValue> executeAsyncCommand(ActionType actionType,
            ActionParametersBase parameters,
            CommandContext cmdContext);

}
