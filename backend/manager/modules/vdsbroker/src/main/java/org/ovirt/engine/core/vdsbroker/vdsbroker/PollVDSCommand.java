package org.ovirt.engine.core.vdsbroker.vdsbroker;

import org.ovirt.engine.core.common.vdscommands.VdsIdVDSCommandParametersBase;
import org.ovirt.engine.core.utils.log.Logged;
import org.ovirt.engine.core.utils.log.Logged.LogLevel;

/**
 * Sole purpose of this command is to check connectivity with VDSM by invoking getCapabilities verb. Generally it is
 * used on network provisioning flows where VDSM is committing network changes only when traffic is from backend is
 * detected (configurable behavior)
 */
@Logged(executionLevel = LogLevel.TRACE, errorLevel = LogLevel.DEBUG)
public class PollVDSCommand<P extends VdsIdVDSCommandParametersBase> extends FutureVDSCommand<P> {

    public PollVDSCommand(P parameters) {
        super(parameters);
    }

    @Override
    protected void executeVdsBrokerCommand() {
        httpTask = getBroker().poll();
    }

}
