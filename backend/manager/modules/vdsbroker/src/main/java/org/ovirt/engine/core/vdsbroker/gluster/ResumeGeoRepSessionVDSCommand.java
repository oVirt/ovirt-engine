package org.ovirt.engine.core.vdsbroker.gluster;

import org.ovirt.engine.core.common.vdscommands.gluster.GlusterVolumeGeoRepSessionVDSParameters;

public class ResumeGeoRepSessionVDSCommand<P extends GlusterVolumeGeoRepSessionVDSParameters> extends AbstractGlusterBrokerCommand<P> {

    public ResumeGeoRepSessionVDSCommand(P parameters) {
        super(parameters);
    }

    @Override
    protected void executeVdsBrokerCommand() {
        GlusterVolumeGeoRepSessionVDSParameters parameters = getParameters();
        status = getBroker().glusterVolumeGeoRepResume(parameters.getVolumeName(), parameters.getSlaveHost(), parameters.getSlaveVolume(), parameters.getForce());
        proceedProxyReturnValue();
    }
}
