package org.ovirt.engine.core.bll.network.host;

import org.ovirt.engine.core.bll.FenceAgentCommandBase;
import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.common.action.FenceAgentCommandParameterBase;
import org.ovirt.engine.core.common.errors.VdcBllMessages;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;

public class RemoveFenceAgentsByVdsIdCommand<T extends FenceAgentCommandParameterBase> extends FenceAgentCommandBase {

    @Override
    protected boolean canDoAction() {
        if (getParameters() == null || getParameters().getVdsId() == null) {
            return failCanDoAction(VdcBllMessages.VDS_REMOVE_FENCE_AGENTS_VDS_ID_REQUIRED);
        }
        return super.canDoAction();
    }

    @Override
    protected void executeCommand() {
        DbFacade.getInstance().getFenceAgentDao().removeByVdsId(getParameters().getVdsId());
        setSucceeded(true);
    }

    public RemoveFenceAgentsByVdsIdCommand() {
        super();
    }

    public RemoveFenceAgentsByVdsIdCommand(FenceAgentCommandParameterBase parameters, CommandContext cmdContext) {
        super(parameters, cmdContext);
    }

    public RemoveFenceAgentsByVdsIdCommand(FenceAgentCommandParameterBase parameters) {
        super(parameters);
    }

    public RemoveFenceAgentsByVdsIdCommand(Guid commandId) {
        super(commandId);
    }
}
