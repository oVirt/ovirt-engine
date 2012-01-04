package org.ovirt.engine.core.vdsbroker.vdsbroker;

import org.ovirt.engine.core.common.vdscommands.VdsIdAndVdsVDSCommandParametersBase;

/**
 * Sole purpose of this command is to check connectivity with VDSM by invoking getCapabilities verb. Generally it is
 * used on network provisioning flows where VDSM is committing network changes only when traffic is from backend is
 * detected (configurable behavior)
 */
public class PollVDSCommand<P extends VdsIdAndVdsVDSCommandParametersBase> extends FutureVDSCommand<P> {

    public PollVDSCommand(P parameters) {
        super(parameters);
    }

    @Override
    protected void ExecuteVdsBrokerCommand() {
        httpTask = getBroker().poll();
    }

}
