package org.ovirt.engine.core.bll.network.host;

import static java.util.concurrent.TimeUnit.SECONDS;
import static org.ovirt.engine.core.common.action.LockProperties.Scope.Execution;

import org.ovirt.engine.core.bll.VdsCommand;
import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.common.action.ActionReturnValue;
import org.ovirt.engine.core.common.action.ActionType;
import org.ovirt.engine.core.common.action.LockProperties;
import org.ovirt.engine.core.common.action.VdsActionParameters;
import org.ovirt.engine.core.common.config.Config;
import org.ovirt.engine.core.common.config.ConfigValues;

public class RefreshHostCommand extends VdsCommand<VdsActionParameters> {

    public RefreshHostCommand(VdsActionParameters parameters, CommandContext commandContext) {
        super(parameters, commandContext);
    }

    @Override
    protected void executeCommand() {
        VdsActionParameters parameters = new VdsActionParameters(getVdsId());
        int timeout = Config.<Integer> getValue(ConfigValues.SetupNetworksWaitTimeoutSeconds);
        parameters.setLockProperties(LockProperties.create(Execution).withWaitTimeout(SECONDS.toMillis(timeout)));

        ActionReturnValue returnValue = runInternalAction(ActionType.RefreshHostCapabilities, parameters);
        if (!returnValue.getSucceeded()) {
            return;
        }

        returnValue = runInternalAction(ActionType.RefreshHostDevices, parameters);
        if (!returnValue.getSucceeded()) {
            return;
        }

        setSucceeded(true);
    }
}
