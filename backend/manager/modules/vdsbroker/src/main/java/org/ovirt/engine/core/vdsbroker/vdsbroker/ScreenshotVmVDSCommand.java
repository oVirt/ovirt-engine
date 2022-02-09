package org.ovirt.engine.core.vdsbroker.vdsbroker;

import org.ovirt.engine.core.common.vdscommands.VdsAndVmIDVDSParametersBase;
import org.ovirt.engine.core.utils.log.Logged;

@Logged(executionLevel = Logged.LogLevel.OFF)
public class ScreenshotVmVDSCommand<P extends VdsAndVmIDVDSParametersBase> extends VdsBrokerCommand<P> {
    private ScreenshotInfoReturn result;

    public ScreenshotVmVDSCommand(P parameters) {
        super(parameters);
    }

    @Override
    protected void executeVdsBrokerCommand() {
        result = getBroker().createScreenshot(getParameters().getVmId().toString());
        proceedProxyReturnValue();
        setReturnValue(result.getEncodedScreenshotInfo());
    }

    @Override
    protected Status getReturnStatus() {
        return result.getStatus();
    }

    @Override
    protected Object getReturnValueFromBroker() {
        return result;
    }
}
