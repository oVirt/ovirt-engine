package org.ovirt.engine.core.bll;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.common.action.AttachNetworkToVdsGroupParameter;
import org.ovirt.engine.core.common.action.ClusterNetworksParameters;
import org.ovirt.engine.core.common.action.VdcActionParametersBase;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.businessentities.network.Network;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;
import org.ovirt.engine.core.utils.NetworkUtils;

/**
 * A multiple action runner designed to attach/detach networks to/from cluster
 */
public class NetworkClusterAttachmentActionRunner extends MultipleActionsRunner {

    private VdcActionType massAction;

    public NetworkClusterAttachmentActionRunner(VdcActionType actionType,
            List<VdcActionParametersBase> parameters,
            boolean isInternal,
            CommandContext commandContext, VdcActionType massAction) {
        super(actionType, parameters, commandContext, isInternal);
        this.massAction = massAction;
    }

    protected void runCommands() {
        List<AttachNetworkToVdsGroupParameter> params = new ArrayList<>();
        Set<Guid> networkIds = new HashSet<>();
        for (CommandBase<?> command : getCommands()) {
            if (command.getReturnValue().getCanDoAction()) {
                AttachNetworkToVdsGroupParameter parameters =
                        (AttachNetworkToVdsGroupParameter) command.getParameters();
                params.add(parameters);
                Network network =
                        DbFacade.getInstance().getNetworkDao().get(parameters.getNetworkCluster().getNetworkId());
                if (NetworkUtils.isConfiguredByLabel(network)) {
                    networkIds.add(network.getId());
                }
            }
        }

        // managing a single network on multiple clusters can be executed using the regular runner
        if (networkIds.size() <= 1) {
            super.runCommands();
            return;
        }

        // multiple networks can be either attached or detached from a single cluster
        if (!params.isEmpty()) {
            Backend.getInstance().runInternalAction(massAction,
                    new ClusterNetworksParameters(params.get(0).getVdsGroupId(), params), commandContext);
        }
    }
}
