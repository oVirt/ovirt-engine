package org.ovirt.engine.core.vdsbroker.gluster;

import java.util.List;

import org.ovirt.engine.core.common.businessentities.gluster.GlusterGeoRepSessionConfiguration;
import org.ovirt.engine.core.common.vdscommands.gluster.GlusterVolumeGeoRepSessionVDSParameters;
import org.ovirt.engine.core.vdsbroker.vdsbroker.Status;

public class GetGlusterVolumeGeoRepConfigListVDSCommand<P extends GlusterVolumeGeoRepSessionVDSParameters> extends AbstractGlusterBrokerCommand<P> {

    private GlusterVolumeGeoRepConfigList result;

    public GetGlusterVolumeGeoRepConfigListVDSCommand(P parameters) {
        super(parameters);
    }

    @Override
    protected Status getReturnStatus() {
        return result.getStatus();
    }

    @Override
    protected void executeVdsBrokerCommand() {
        result =
                getBroker().glusterVolumeGeoRepConfigList(getParameters().getVolumeName(),
                        getParameters().getSlaveHost(),
                        getParameters().getSlaveVolume(),
                        getParameters().getUserName());
        proceedProxyReturnValue();
        List<GlusterGeoRepSessionConfiguration> sessionConfigs = result.getSessionConfig();
        setReturnValue(sessionConfigs);
    }
}
