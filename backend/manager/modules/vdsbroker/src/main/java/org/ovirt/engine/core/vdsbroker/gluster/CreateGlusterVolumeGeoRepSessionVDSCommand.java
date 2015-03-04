package org.ovirt.engine.core.vdsbroker.gluster;

import org.ovirt.engine.core.common.vdscommands.gluster.GlusterVolumeGeoRepSessionVDSParameters;

public class CreateGlusterVolumeGeoRepSessionVDSCommand<P extends GlusterVolumeGeoRepSessionVDSParameters> extends AbstractGlusterBrokerCommand<P> {

    public CreateGlusterVolumeGeoRepSessionVDSCommand(P parameters) {
        super(parameters);
    }

    @Override
    protected void executeVdsBrokerCommand() {
        GlusterVolumeGeoRepSessionVDSParameters parameters = getParameters();
        status =
                getBroker().glusterVolumeGeoRepSessionCreate(parameters.getVolumeName(),
                        parameters.getSlaveHost(),
                        parameters.getSlaveVolume(),
                        parameters.getUserName(),
                        parameters.getForce());
        proceedProxyReturnValue();
    }

}
