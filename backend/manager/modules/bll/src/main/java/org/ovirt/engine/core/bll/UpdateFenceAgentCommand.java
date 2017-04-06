package org.ovirt.engine.core.bll;

import javax.inject.Inject;

import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.common.action.FenceAgentCommandParameterBase;
import org.ovirt.engine.core.common.businessentities.Cluster;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.pm.FenceAgent;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.ClusterDao;
import org.ovirt.engine.core.dao.FenceAgentDao;
import org.ovirt.engine.core.dao.VdsDao;
import org.ovirt.engine.core.utils.pm.FenceConfigHelper;

public class UpdateFenceAgentCommand<T extends FenceAgentCommandParameterBase> extends FenceAgentCommandBase {

    @Inject
    private VdsDao vdsDao;
    @Inject
    private ClusterDao clusterDao;
    @Inject
    private FenceAgentDao fenceAgentDao;

    public UpdateFenceAgentCommand(FenceAgentCommandParameterBase parameters, CommandContext cmdContext) {
        super(parameters, cmdContext);
    }

    @Override
    protected boolean validate() {
        if (getParameters() != null
                && getParameters().getAgent() != null
                && getParameters().getAgent().getHostId() != null
                && getParameters().getAgent().getType() != null) {
            Guid vdsId = getParameters().getAgent().getHostId();
            VDS vds = vdsDao.get(vdsId);
            Guid vdsClusterId = vds.getClusterId();
            Cluster cluster = clusterDao.get(vdsClusterId);
            String clusterCompatibilityVersion = cluster.getCompatibilityVersion().toString();

            if (!FenceConfigHelper.getValidFenceAgentTypes(clusterCompatibilityVersion)
                    .contains(getParameters().getAgent().getType())) {
                return failValidation(EngineMessage.ACTION_TYPE_FAILED_AGENT_NOT_SUPPORTED);
            }
        }
        return super.validate();
    }

    @Override
    protected void executeCommand() {
        FenceAgent agent = getParameters().getAgent();
        fenceAgentDao.update(agent);
        setSucceeded(true);
    }
}
