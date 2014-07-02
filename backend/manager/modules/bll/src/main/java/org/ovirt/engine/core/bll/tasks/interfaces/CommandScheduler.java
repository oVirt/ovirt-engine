package org.ovirt.engine.core.bll.tasks.interfaces;

import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.common.action.VdcActionParametersBase;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.action.VdcReturnValueBase;

import java.util.concurrent.Future;

public interface CommandScheduler {
    public abstract Future<VdcReturnValueBase> executeAsyncCommand(VdcActionType actionType, VdcActionParametersBase parameters, CommandContext cmdContext);
}
