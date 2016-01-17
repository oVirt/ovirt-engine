package org.ovirt.engine.core.bll.network.host;

import org.ovirt.engine.core.bll.VdsCommand;
import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.action.VdcReturnValueBase;
import org.ovirt.engine.core.common.action.VdsActionParameters;

public class RefreshHostCommand extends VdsCommand<VdsActionParameters> {

    public RefreshHostCommand(VdsActionParameters parameters, CommandContext commandContext) {
        super(parameters, commandContext);
    }

    @Override
    protected void executeCommand() {
        VdsActionParameters parameters = new VdsActionParameters(getVdsId());
        VdcReturnValueBase returnValue = runInternalAction(VdcActionType.RefreshHostCapabilities, parameters);
        if (!returnValue.getSucceeded()) {
            return;
        }

        returnValue = runInternalAction(VdcActionType.RefreshHostDevices, parameters);
        if (!returnValue.getSucceeded()) {
            return;
        }

        setSucceeded(true);
    }
}
