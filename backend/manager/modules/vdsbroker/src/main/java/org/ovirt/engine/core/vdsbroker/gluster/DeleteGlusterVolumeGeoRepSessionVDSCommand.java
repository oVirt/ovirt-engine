package org.ovirt.engine.core.vdsbroker.gluster;

import org.ovirt.engine.core.common.vdscommands.gluster.GlusterVolumeGeoRepSessionVDSParameters;

/**
 * VDS command to delete a gluster geo-replication session
 */
public class DeleteGlusterVolumeGeoRepSessionVDSCommand<P extends GlusterVolumeGeoRepSessionVDSParameters> extends AbstractGlusterBrokerCommand<P> {

    public DeleteGlusterVolumeGeoRepSessionVDSCommand(P parameters) {
        super(parameters);
    }


    @Override
    protected void executeVdsBrokerCommand() {
        status = getBroker().glusterVolumeGeoRepSessionDelete(getParameters().getVolumeName(),
                getParameters().getSlaveHost(),
                getParameters().getSlaveVolume(),
                getParameters().getUserName());
        // Handle errors if any
        proceedProxyReturnValue();

    }

}
