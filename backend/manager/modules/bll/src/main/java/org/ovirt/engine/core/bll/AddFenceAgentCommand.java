package org.ovirt.engine.core.bll;

import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.common.action.FenceAgentCommandParameterBase;
import org.ovirt.engine.core.common.businessentities.pm.FenceAgent;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;

public class AddFenceAgentCommand<T extends FenceAgentCommandParameterBase> extends FenceAgentCommandBase {

    public AddFenceAgentCommand(FenceAgentCommandParameterBase parameters, CommandContext cmdContext) {
        super(parameters, cmdContext);
    }

    public AddFenceAgentCommand(Guid commandId) {
        super(commandId);
    }

    @Override
    protected boolean validate() {
        if (getParameters() == null
                || getParameters().getAgent() == null
                || getParameters().getAgent().getIp() == null
                || getParameters().getAgent().getHostId() == null
                || getParameters().getAgent().getPassword() == null
                || getParameters().getAgent().getType() == null
                || getParameters().getAgent().getUser() == null) {
            return failValidation(EngineMessage.VDS_ADD_FENCE_AGENT_MANDATORY_PARAMETERS_MISSING);
        }
        return super.validate();
    }

    @Override
    protected void executeCommand() {
        FenceAgent agent = getParameters().getAgent();
        DbFacade.getInstance().getFenceAgentDao().save(agent);
        getReturnValue().setActionReturnValue(agent.getId());
        setSucceeded(true);
    }
}
