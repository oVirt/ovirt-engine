package org.ovirt.engine.core.vdsbroker.gluster;

import org.ovirt.engine.core.common.vdscommands.gluster.GlusterVolumeGeoRepConfigVdsParameters;

public class SetGlusterVolumeGeoRepConfigVDSCommand<P extends GlusterVolumeGeoRepConfigVdsParameters> extends AbstractGlusterBrokerCommand<P> {

    public SetGlusterVolumeGeoRepConfigVDSCommand(P parameters) {
        super(parameters);
    }

    @Override
    protected void executeVdsBrokerCommand() {
        status =
                getBroker().glusterVolumeGeoRepConfigSet(getParameters().getVolumeName(),
                        getParameters().getSlaveHost(),
                        getParameters().getSlaveVolume(),
                        getParameters().getConfigKey(),
                        getParameters().getConfigValue(),
                        getParameters().getUserName());
        proceedProxyReturnValue();
    }
}
