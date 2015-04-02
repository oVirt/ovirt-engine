package org.ovirt.engine.core.vdsbroker.gluster;

import org.ovirt.engine.core.common.vdscommands.gluster.GlusterVolumeGeoRepConfigVdsParameters;

public class SetGlusterVolumeGeoRepConfigDefaultVDSCommand<P extends GlusterVolumeGeoRepConfigVdsParameters> extends AbstractGlusterBrokerCommand<P> {

    public SetGlusterVolumeGeoRepConfigDefaultVDSCommand(P parameters) {
        super(parameters);
    }

    @Override
    protected void executeVdsBrokerCommand() {
        status =
                getBroker().glusterVolumeGeoRepConfigReset(getParameters().getVolumeName(),
                        getParameters().getSlaveHost(),
                        getParameters().getSlaveVolume(),
                        getParameters().getConfigKey(),
                        getParameters().getUserName());
        proceedProxyReturnValue();
    }

}
