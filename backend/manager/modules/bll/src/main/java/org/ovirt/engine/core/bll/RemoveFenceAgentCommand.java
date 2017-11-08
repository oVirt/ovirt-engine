package org.ovirt.engine.core.bll;

import java.util.List;

import javax.inject.Inject;

import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.common.action.FenceAgentCommandParameterBase;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.pm.FenceAgent;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.FenceAgentDao;
import org.ovirt.engine.core.dao.VdsDao;

public class RemoveFenceAgentCommand<T extends FenceAgentCommandParameterBase> extends FenceAgentCommandBase {

    @Inject
    private VdsDao vdsDao;

    @Inject
    private FenceAgentDao fenceAgentDao;

    @Override
    protected void setActionMessageParameters() {
        super.setActionMessageParameters();
        addValidationMessage(EngineMessage.VAR__ACTION__REMOVE);
    }

    @Override
    protected boolean validate() {
        if (getParameters() == null || getParameters().getAgent() == null || getParameters().getAgent().getId() == null) {
            return failValidation(EngineMessage.VDS_REMOVE_FENCE_AGENT_ID_REQUIRED);
        }
        // check for removal of last fence agent while PM is enabled in the host
        Guid vdsId = getParameters().getAgent().getHostId();
        VDS host = vdsDao.get(vdsId);
        if (host == null) {
            return failValidation(EngineMessage.VDS_INVALID_SERVER_ID);
        }

        if (host.isPmEnabled()) {
            List<FenceAgent> fenceAgents = fenceAgentDao.getFenceAgentsForHost(getVdsId());
            if (fenceAgents.size() == 1) {
                return failValidation(EngineMessage.VDS_REMOVE_LAST_FENCE_AGENT_PM_ENABLED);
            }
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
