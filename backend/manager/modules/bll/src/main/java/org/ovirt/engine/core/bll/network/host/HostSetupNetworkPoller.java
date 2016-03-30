package org.ovirt.engine.core.bll.network.host;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.ovirt.engine.core.bll.Backend;
import org.ovirt.engine.core.common.config.Config;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.common.interfaces.FutureVDSCall;
import org.ovirt.engine.core.common.vdscommands.FutureVDSCommandType;
import org.ovirt.engine.core.common.vdscommands.VDSReturnValue;
import org.ovirt.engine.core.common.vdscommands.VdsIdVDSCommandParametersBase;
import org.ovirt.engine.core.compat.Guid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@code HostSetupNetworkPoller} class uses {@link FutureVDSCall} to poll the VDSM (with ping verb).
 */
public class HostSetupNetworkPoller {

    private static final Logger LOGGER = LoggerFactory.getLogger(HostSetupNetworkPoller.class);

    /**
     * Time millis between polling attempts, to prevent flooding the host/network.
     */
    private static final long POLLING_BREAK = 500;
    private static final long POLLING_BREAK_IN_NANOS = TimeUnit.MILLISECONDS.toNanos(POLLING_BREAK);
    private Long timestampOfEndOfPreviousInvocation;

    public boolean poll(Guid hostId) {
        waitBetweenPolls();

        LOGGER.trace("Request to do poll for host {}.", hostId);
        FutureVDSCall<VDSReturnValue> task =
                Backend.getInstance()
                        .getResourceManager()
                        .runFutureVdsCommand(FutureVDSCommandType.Poll, new VdsIdVDSCommandParametersBase(hostId));
        LOGGER.trace("FutureVDSCommandType.Poll executed for host{}.", hostId);

        boolean succeeded = getValue(task);
        LOGGER.trace("Result of FutureVDSCommandType.Poll for host {}: {}", hostId, succeeded);
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
                    task.get(Config.<Integer> getValue(ConfigValues.SetupNetworksPollingTimeout), TimeUnit.SECONDS);
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
