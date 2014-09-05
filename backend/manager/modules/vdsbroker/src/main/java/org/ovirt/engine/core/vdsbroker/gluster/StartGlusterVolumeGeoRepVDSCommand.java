package org.ovirt.engine.core.vdsbroker.gluster;

import org.ovirt.engine.core.common.vdscommands.gluster.GlusterVolumeGeoRepSessionVDSParameters;

public class StartGlusterVolumeGeoRepVDSCommand<P extends GlusterVolumeGeoRepSessionVDSParameters> extends AbstractGlusterBrokerCommand<P> {

    public StartGlusterVolumeGeoRepVDSCommand(P parameters) {
        super(parameters);
    }

    @Override
    protected void executeVdsBrokerCommand() {
        GlusterVolumeGeoRepSessionVDSParameters parameters = getParameters();
        status = getBroker().glusterVolumeGeoRepStart(parameters.getVolumeName(), parameters.getSlaveHost(), parameters.getSlaveVolume(), parameters.getForce());
        proceedProxyReturnValue();
    }

}
