package org.ovirt.engine.core.vdsbroker.gluster;

import org.ovirt.engine.core.common.vdscommands.gluster.GlusterVolumeVDSParameters;

public class StopRebalanceGlusterVolumeVDSCommand <P extends GlusterVolumeVDSParameters> extends AbstractGlusterBrokerCommand<P> {

    public StopRebalanceGlusterVolumeVDSCommand(P parameters) {
        super(parameters);
    }

    @Override
    protected void ExecuteVdsBrokerCommand() {
        status = getBroker().glusterVolumeRebalanceStop(getParameters().getVolumeName());
        proceedProxyReturnValue();
    }
}
