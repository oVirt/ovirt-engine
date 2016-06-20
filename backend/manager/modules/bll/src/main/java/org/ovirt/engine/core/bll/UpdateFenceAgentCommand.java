package org.ovirt.engine.core.bll;

import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.common.action.FenceAgentCommandParameterBase;
import org.ovirt.engine.core.common.businessentities.pm.FenceAgent;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;

public class UpdateFenceAgentCommand<T extends FenceAgentCommandParameterBase> extends FenceAgentCommandBase {

    public UpdateFenceAgentCommand(FenceAgentCommandParameterBase parameters, CommandContext cmdContext) {
        super(parameters, cmdContext);
    }

    @Override
    protected void executeCommand() {
        FenceAgent agent = getParameters().getAgent();
        DbFacade.getInstance().getFenceAgentDao().update(agent);
        setSucceeded(true);
    }
}
