package org.ovirt.engine.core.vdsbroker.gluster;

import org.ovirt.engine.core.common.vdscommands.gluster.GlusterVolumeGeoRepSessionVDSParameters;

public class PauseGlusterVolumeGeoRepSessionVDSCommand extends AbstractGlusterBrokerCommand<GlusterVolumeGeoRepSessionVDSParameters> {

    public PauseGlusterVolumeGeoRepSessionVDSCommand(GlusterVolumeGeoRepSessionVDSParameters parameters) {
        super(parameters);
    }

    @Override
    protected void executeVdsBrokerCommand() {
        GlusterVolumeGeoRepSessionVDSParameters parameters = getParameters();
        status =
                getBroker().glusterVolumeGeoRepSessionPause(parameters.getVolumeName(),
                        parameters.getSlaveHost(),
                        parameters.getSlaveVolume(),
                        parameters.getUserName(),
                        parameters.getForce());
        proceedProxyReturnValue();
    }
}
