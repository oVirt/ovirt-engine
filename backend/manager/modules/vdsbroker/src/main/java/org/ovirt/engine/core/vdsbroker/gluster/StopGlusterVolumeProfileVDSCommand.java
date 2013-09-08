package org.ovirt.engine.core.vdsbroker.gluster;

import org.ovirt.engine.core.common.vdscommands.gluster.GlusterVolumeVDSParameters;

public class StopGlusterVolumeProfileVDSCommand<P extends GlusterVolumeVDSParameters> extends AbstractGlusterBrokerCommand<P> {
    public StopGlusterVolumeProfileVDSCommand(P parameters) {
        super(parameters);
    }

    @Override
    protected void executeVdsBrokerCommand() {
        status = getBroker().glusterVolumeProfileStop(getParameters().getVolumeName());

        proceedProxyReturnValue();
    }
}
