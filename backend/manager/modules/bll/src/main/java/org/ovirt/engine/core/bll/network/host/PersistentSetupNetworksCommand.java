package org.ovirt.engine.core.bll.network.host;

import org.ovirt.engine.core.bll.NonTransactiveCommandAttribute;
import org.ovirt.engine.core.bll.VdsCommand;
import org.ovirt.engine.core.bll.network.host.SetupNetworksCommand.SETUP_NETWORKS_RESOLUTION;
import org.ovirt.engine.core.common.action.PersistentSetupNetworksParameters;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.action.VdcReturnValueBase;
import org.ovirt.engine.core.common.action.VdsActionParameters;
import org.ovirt.engine.core.common.errors.VdcBllMessages;

@NonTransactiveCommandAttribute
public class PersistentSetupNetworksCommand<T extends PersistentSetupNetworksParameters> extends VdsCommand<T> {

    public PersistentSetupNetworksCommand(T parameters) {
        super(parameters);
        setVdsId(parameters.getVdsId());
    }

    @Override
    protected void setActionMessageParameters() {
        addCanDoActionMessage(VdcBllMessages.VAR__ACTION__SETUP);
        addCanDoActionMessage(VdcBllMessages.VAR__TYPE__NETWORKS);
    }

    @Override
    protected void executeCommand() {
        VdcReturnValueBase returnValue = getBackend().runInternalAction(VdcActionType.SetupNetworks, getParameters());
        if (returnValue.getSucceeded()
                && SETUP_NETWORKS_RESOLUTION.NO_CHANGES_DETECTED != returnValue.getActionReturnValue()) {
            returnValue =
                    getBackend().runInternalAction(VdcActionType.CommitNetworkChanges,
                            new VdsActionParameters(getParameters().getVdsId()));
        }

        if (!returnValue.getSucceeded()) {
            propagateFailure(returnValue);
        }

        setSucceeded(returnValue.getSucceeded());
    }
}
