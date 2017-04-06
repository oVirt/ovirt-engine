package org.ovirt.engine.core.bll.network.host;

import javax.inject.Inject;

import org.ovirt.engine.core.bll.FenceAgentCommandBase;
import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.common.action.FenceAgentCommandParameterBase;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.dao.FenceAgentDao;

public class RemoveFenceAgentsByVdsIdCommand<T extends FenceAgentCommandParameterBase> extends FenceAgentCommandBase {

    @Inject
    private FenceAgentDao fenceAgentDao;

    @Override
    protected boolean validate() {
        if (getParameters() == null || getParameters().getVdsId() == null) {
            return failValidation(EngineMessage.VDS_REMOVE_FENCE_AGENTS_VDS_ID_REQUIRED);
        }
        return super.validate();
    }

    @Override
    protected void executeCommand() {
        fenceAgentDao.removeByVdsId(getParameters().getVdsId());
        setSucceeded(true);
    }

    public RemoveFenceAgentsByVdsIdCommand(FenceAgentCommandParameterBase parameters, CommandContext cmdContext) {
        super(parameters, cmdContext);
    }
}
