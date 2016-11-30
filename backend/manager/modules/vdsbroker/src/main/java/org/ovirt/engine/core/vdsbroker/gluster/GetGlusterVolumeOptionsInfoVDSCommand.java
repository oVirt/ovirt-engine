package org.ovirt.engine.core.vdsbroker.gluster;

import org.ovirt.engine.core.common.vdscommands.VdsIdVDSCommandParametersBase;
import org.ovirt.engine.core.vdsbroker.vdsbroker.Status;

public class GetGlusterVolumeOptionsInfoVDSCommand<P extends VdsIdVDSCommandParametersBase> extends AbstractGlusterBrokerCommand<P> {
    private GlusterVolumeOptionsInfoReturn result;

    public GetGlusterVolumeOptionsInfoVDSCommand(P parameters) {
        super(parameters);
    }

    @Override
    protected Status getReturnStatus() {
        return result.getStatus();
    }

    @Override
    protected void executeVdsBrokerCommand() {
        result = getBroker().glusterVolumeSetOptionsList();
        proceedProxyReturnValue();
        setReturnValue(result.optionsHelpSet);
    }
}
