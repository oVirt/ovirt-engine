package org.ovirt.engine.core.bll;

import javax.inject.Inject;

import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.common.action.FenceAgentCommandParameterBase;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.dao.FenceAgentDao;

public class RemoveFenceAgentCommand<T extends FenceAgentCommandParameterBase> extends FenceAgentCommandBase {

    @Inject
    private FenceAgentDao fenceAgentDao;

    @Override
    protected boolean validate() {
        if (getParameters() == null || getParameters().getAgent() == null || getParameters().getAgent().getId() == null) {
            return failValidation(EngineMessage.VDS_REMOVE_FENCE_AGENT_ID_REQUIRED);
        }
        return super.validate();
    }

    @Override
    protected void executeCommand() {
        fenceAgentDao.remove(getParameters().getAgent().getId());
        setSucceeded(true);
    }

    public RemoveFenceAgentCommand(FenceAgentCommandParameterBase parameters, CommandContext cmdContext) {
        super(parameters, cmdContext);
    }
}
