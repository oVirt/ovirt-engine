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

/**
 * The {@code HostSetupNetworkPoller} class uses {@link FutureVDSCall} to poll the VDSM (with ping verb).
 */
public class HostSetupNetworkPoller {

    /** Time between polling attempts, to prevent flooding the host/network. */
    private static final long POLLING_BREAK = 500;

    public boolean poll(Guid hostId) {
        long timeBeforePoll = System.currentTimeMillis();
        FutureVDSCall<VDSReturnValue> task =
                Backend.getInstance()
                        .getResourceManager()
                        .runFutureVdsCommand(FutureVDSCommandType.Poll, new VdsIdVDSCommandParametersBase(hostId));
        try {
            VDSReturnValue returnValue =
                    task.get(Config.<Integer> getValue(ConfigValues.SetupNetworksPollingTimeout), TimeUnit.SECONDS);
            if (returnValue.getSucceeded()) {
                return true;
            }

            waitBeforeNextPoll(timeBeforePoll);
        } catch (TimeoutException e) {
            // ignore failure. network can go down due to VDSM changing the network. No need to suspend between polls is
            // timeout has reached.
        }

        return false;
    }

    public void waitBeforeNextPoll(long timeBeforePoll) {
        if (System.currentTimeMillis() - timeBeforePoll < POLLING_BREAK) {
            try {
                Thread.sleep(POLLING_BREAK);
            } catch (InterruptedException e) {
                // ignore.
            }
        }
    }
}
