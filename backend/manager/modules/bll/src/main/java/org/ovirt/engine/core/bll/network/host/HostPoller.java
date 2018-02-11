package org.ovirt.engine.core.bll.network.host;

import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.ovirt.engine.core.common.config.Config;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.common.interfaces.FutureVDSCall;
import org.ovirt.engine.core.common.interfaces.VDSBrokerFrontend;
import org.ovirt.engine.core.common.vdscommands.FutureVDSCommandType;
import org.ovirt.engine.core.common.vdscommands.TimeBoundPollVDSCommandParameters;
import org.ovirt.engine.core.common.vdscommands.VDSReturnValue;
import org.ovirt.engine.core.di.Injector;
import org.ovirt.engine.core.vdsbroker.vdsbroker.PollVDSCommand;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@code HostPoller} class uses {@link PollVDSCommand} to poll the VDSM.
 * The parameters object passed to its constructor determines the polling
 * verb that will be used by {@link PollVDSCommand} for the actual poll.
 * @see TimeBoundPollVDSCommandParameters.PollTechnique
 */
public class HostPoller {

    private static final Logger LOGGER = LoggerFactory.getLogger(HostPoller.class);

    /**
     * Time millis between polling attempts, to prevent flooding the host/network.
     */
    private static final long POLLING_BREAK = 500;
    private static final long POLLING_BREAK_IN_NANOS = TimeUnit.MILLISECONDS.toNanos(POLLING_BREAK);
    private Long timestampOfEndOfPreviousInvocation;
    private TimeBoundPollVDSCommandParameters parameters;

    public HostPoller(TimeBoundPollVDSCommandParameters parameters) {
        this.parameters = Objects.requireNonNull(parameters);
    }

    public boolean poll() {
        waitBetweenPolls();

        LOGGER.trace("Request to do poll for host {}.", parameters.getVdsId());
        FutureVDSCall<VDSReturnValue> task =
                Injector.get(VDSBrokerFrontend.class).runFutureVdsCommand(FutureVDSCommandType.Poll, parameters);
        LOGGER.trace("FutureVDSCommandType.Poll executed for host{}.", parameters.getVdsId());

        boolean succeeded = getValue(task);
        LOGGER.trace("Result of FutureVDSCommandType.Poll for host {}: {}", parameters.getVdsId(), succeeded);
        timestampOfEndOfPreviousInvocation = currentTimestamp();
        return succeeded;
    }

    /**
     * @param task future to get value from.
     *
     * @return true if execution of task was a success, false if execution was not successful or wasn't completed in
     * given timeout.
     */
    private boolean getValue(FutureVDSCall<VDSReturnValue> task) {
        try {
            VDSReturnValue vdsReturnValue =
                    task.get(Config.<Long> getValue(ConfigValues.SetupNetworksPollingTimeout), TimeUnit.SECONDS);
            return vdsReturnValue.getSucceeded();
        } catch (TimeoutException e) {
            // VDSReturn value did not become available in given timeout. Ignore failure.
            // network can go down due to VDSM changing the network.
            return false;
        }
    }

    private void waitBetweenPolls() {
        /*
         * Please notice, that thread sleep does not guarantee to return *only after* required time period passed.
         * It may return sooner. When testing with required 500ms sleep, thread awaken after ~ 200ms, which is not even
         * close.
        * */
        while (timestampOfEndOfPreviousInvocation != null
                && currentTimestamp() - timestampOfEndOfPreviousInvocation < POLLING_BREAK_IN_NANOS) {
            try {
                Thread.sleep(POLLING_BREAK);
            } catch (InterruptedException e) {
                // ignore.
            }
        }

    }

    private long currentTimestamp() {
        return System.nanoTime();
    }
}
