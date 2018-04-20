package org.ovirt.engine.core.vdsbroker.vdsbroker;

import static org.ovirt.engine.core.common.vdscommands.TimeBoundPollVDSCommandParameters.PollTechnique.CONFIRM_CONNECTIVITY;
import static org.ovirt.engine.core.common.vdscommands.TimeBoundPollVDSCommandParameters.PollTechnique.POLL;
import static org.ovirt.engine.core.common.vdscommands.TimeBoundPollVDSCommandParameters.PollTechnique.POLL2;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.ovirt.engine.core.common.vdscommands.TimeBoundPollVDSCommandParameters;
import org.ovirt.engine.core.utils.log.Logged;
import org.ovirt.engine.core.utils.log.Logged.LogLevel;
import org.ovirt.engine.core.vdsbroker.TransportRunTimeException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Sole purpose of this command is to check connectivity with VDSM by invoking getCapabilities verb. Generally it is
 * used on network provisioning flows where VDSM is committing network changes only when traffic is from backend is
 * detected (configurable behavior)
 */
@Logged(executionLevel = LogLevel.TRACE, errorLevel = LogLevel.DEBUG)
public class PollVDSCommand<P extends TimeBoundPollVDSCommandParameters> extends FutureVDSCommand<P> {

    private static final Logger log = LoggerFactory.getLogger(PollVDSCommand.class);
    private static final int TIMEOUT = 2;

    public PollVDSCommand(P parameters) {
        super(parameters);
    }

    @Override
    protected void executeVdsBrokerCommand() {
        if (getParameters().getPollTechnique().equals(POLL)) {
            httpTask = getBroker().poll();
        } else if (getParameters().getPollTechnique().equals(POLL2)) {
            httpTask = getBroker().timeBoundPoll2(TIMEOUT, TimeUnit.SECONDS);
        } else if (getParameters().getPollTechnique().equals(CONFIRM_CONNECTIVITY)) {
            httpTask = getBroker().timeBoundPollConfirmConnectivity(TIMEOUT, TimeUnit.SECONDS);
        }
    }

    @Override
    protected void proceedProxyReturnValue() {
        try {
            super.proceedProxyReturnValue();
        } catch (VDSNetworkException e) {
            handleException(e, "VDSNetworkException was caught");
        } catch (VDSRecoveringException e) {
            handleException(e, "it's in recovery mode");
        }
    }

    @Override
    protected void handleTransportRunTimeException(TransportRunTimeException e) {
        handleException(e, "failed to create connection to the host");
    }

    private void handleException(RuntimeException e, String reason) {
        setVdsRuntimeError(e);
        final String msg = String.format("Failed to poll host %s - %s.", getParameters().getVdsId(), reason);
        log.debug(msg, e);
    }

    /**
     * Since the polling is used only for connectivity testing and might be too repetitive, there is no value in logging
     * it as an audit log
     */
    @Override
    protected void logToAudit() {
    }

    @Override
    protected void logTimeoutException(TimeoutException e, VDSNetworkException ex) {
        //don't do that.
        //PollingVds is usually done to wait until connectivity to host is recovered.
        //Any timeouts during process are expected and we don't want to log such 'failures'.
    }
}
