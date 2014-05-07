package org.ovirt.engine.core.bll;

import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.common.action.FenceAgentCommandParameterBase;
import org.ovirt.engine.core.common.errors.VdcBllMessages;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;

public class RemoveFenceAgentCommand<T extends FenceAgentCommandParameterBase> extends FenceAgentCommandBase {

    @Override
    protected boolean canDoAction() {
        if (getParameters() == null || getParameters().getAgent() == null || getParameters().getAgent().getId() == null) {
            return failCanDoAction(VdcBllMessages.VDS_REMOVE_FENCE_AGENT_ID_REQUIRED);
        }
        return super.canDoAction();
    }

    @Override
    protected void executeCommand() {
        DbFacade.getInstance().getFenceAgentDao().remove(getParameters().getAgent().getId());
        setSucceeded(true);
    }

    public RemoveFenceAgentCommand() {
        super();
    }

    public RemoveFenceAgentCommand(FenceAgentCommandParameterBase parameters, CommandContext cmdContext) {
        super(parameters, cmdContext);
    }

    public RemoveFenceAgentCommand(FenceAgentCommandParameterBase parameters) {
        super(parameters);
    }

    public RemoveFenceAgentCommand(Guid commandId) {
        super(commandId);
    }
}
