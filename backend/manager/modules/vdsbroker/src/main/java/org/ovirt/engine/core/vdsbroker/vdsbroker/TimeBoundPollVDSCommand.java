package org.ovirt.engine.core.vdsbroker.vdsbroker;

import org.ovirt.engine.core.common.vdscommands.TimeBoundPollVDSCommandParameters;
import org.ovirt.engine.core.utils.log.Logged;
import org.ovirt.engine.core.utils.log.Logged.LogLevel;

/**
 * Sole purpose of this command is to check connectivity with VDSM by invoking poll verb.
 */
@Logged(executionLevel = LogLevel.TRACE, errorLevel = LogLevel.DEBUG)
public class TimeBoundPollVDSCommand<P extends TimeBoundPollVDSCommandParameters> extends FutureVDSCommand<P> {

    public TimeBoundPollVDSCommand(P parameters) {
        super(parameters);
    }

    @Override
    protected void executeVdsBrokerCommand() {
        httpTask = getBroker().timeBoundPoll(this.getParameters().getTimeout(), this.getParameters().getUnit());
    }

}
