package org.ovirt.engine.core.vdsbroker.gluster;

import org.ovirt.engine.core.common.vdscommands.gluster.GlusterVolumeActionVDSParameters;

public class StopGlusterVolumeVDSCommand<P extends GlusterVolumeActionVDSParameters> extends AbstractGlusterBrokerCommand<P> {

    public StopGlusterVolumeVDSCommand(P parameters) {
        super(parameters);
    }

    @Override
    protected void executeVdsBrokerCommand() {
        status = getBroker().glusterVolumeStop(getParameters().getVolumeName(), getParameters().isForceAction());

        proceedProxyReturnValue();
    }
}
