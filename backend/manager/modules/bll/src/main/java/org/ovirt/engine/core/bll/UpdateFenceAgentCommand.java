package org.ovirt.engine.core.bll;

import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.common.action.FenceAgentCommandParameterBase;
import org.ovirt.engine.core.common.businessentities.pm.FenceAgent;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;

public class UpdateFenceAgentCommand<T extends FenceAgentCommandParameterBase> extends FenceAgentCommandBase {

    public UpdateFenceAgentCommand() {
        super();
    }

    public UpdateFenceAgentCommand(FenceAgentCommandParameterBase parameters, CommandContext cmdContext) {
        super(parameters, cmdContext);
    }

    public UpdateFenceAgentCommand(FenceAgentCommandParameterBase parameters) {
        super(parameters);
    }

    public UpdateFenceAgentCommand(Guid commandId) {
        super(commandId);
    }

    @Override
    protected void executeCommand() {
        FenceAgent agent = getParameters().getAgent();
        DbFacade.getInstance().getFenceAgentDao().update(agent);
        setSucceeded(true);
    }
}
