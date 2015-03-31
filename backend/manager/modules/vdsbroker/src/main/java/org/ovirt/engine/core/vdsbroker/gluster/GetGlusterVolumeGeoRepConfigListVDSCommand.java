package org.ovirt.engine.core.vdsbroker.gluster;

import java.util.List;

import org.ovirt.engine.core.common.businessentities.gluster.GlusterGeoRepSessionConfiguration;
import org.ovirt.engine.core.common.vdscommands.gluster.GlusterVolumeGeoRepSessionVDSParameters;
import org.ovirt.engine.core.vdsbroker.vdsbroker.StatusForXmlRpc;

public class GetGlusterVolumeGeoRepConfigListVDSCommand<P extends GlusterVolumeGeoRepSessionVDSParameters> extends AbstractGlusterBrokerCommand<P> {

    private GlusterVolumeGeoRepConfigListXmlRpc result;

    public GetGlusterVolumeGeoRepConfigListVDSCommand(P parameters) {
        super(parameters);
    }

    @Override
    protected StatusForXmlRpc getReturnStatus() {
        return result.getXmlRpcStatus();
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
