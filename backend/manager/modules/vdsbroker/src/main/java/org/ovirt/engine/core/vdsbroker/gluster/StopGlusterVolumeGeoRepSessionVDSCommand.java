package org.ovirt.engine.core.vdsbroker.gluster;

import org.ovirt.engine.core.common.vdscommands.gluster.GlusterVolumeGeoRepSessionVDSParameters;

/**
 * VDS command to stop a gluster geo-replication session
 */
public class StopGlusterVolumeGeoRepSessionVDSCommand<P extends GlusterVolumeGeoRepSessionVDSParameters> extends AbstractGlusterBrokerCommand<P> {

    public StopGlusterVolumeGeoRepSessionVDSCommand(P parameters) {
        super(parameters);
    }

    @Override
    protected void executeVdsBrokerCommand() {
        status = getBroker().glusterVolumeGeoRepSessionStop(getParameters().getVolumeName(),
                getParameters().getSlaveHost(),
                getParameters().getSlaveVolume(),
                getParameters().getUserName(),
                getParameters().getForce());
        // Handle errors if any
        proceedProxyReturnValue();

    }

}
