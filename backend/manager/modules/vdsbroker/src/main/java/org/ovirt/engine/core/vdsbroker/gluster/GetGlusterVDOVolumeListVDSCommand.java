package org.ovirt.engine.core.vdsbroker.gluster;

import org.ovirt.engine.core.common.vdscommands.VdsIdVDSCommandParametersBase;
import org.ovirt.engine.core.vdsbroker.vdsbroker.Status;

public class GetGlusterVDOVolumeListVDSCommand<P extends VdsIdVDSCommandParametersBase> extends AbstractGlusterBrokerCommand<P> {
    private GlusterVDOVolumeListReturn result;

    public GetGlusterVDOVolumeListVDSCommand(P parameters) {
        super(parameters);
    }

    @Override
    protected Status getReturnStatus() {
        return result.getStatus();
    }

    @Override protected void executeVdsBrokerCommand() {
        result = getBroker().glusterVDOVolumeList();
        proceedProxyReturnValue();

        if (getVDSReturnValue().getSucceeded()) {
            setReturnValue(result.getVdoVolumes());
        }
    }
}
