package org.ovirt.engine.core.vdsbroker.gluster;

import org.ovirt.engine.core.common.vdscommands.gluster.GlusterVolumeGeoRepSessionVDSParameters;
import org.ovirt.engine.core.vdsbroker.vdsbroker.StatusForXmlRpc;

public class GetGlusterVolumeGeoRepSessionStatusVDSCommand<P extends GlusterVolumeGeoRepSessionVDSParameters> extends AbstractGlusterBrokerCommand<P> {
    GlusterVolumeGeoRepStatusDetailForXmlRpc result;

    public GetGlusterVolumeGeoRepSessionStatusVDSCommand(P parameters) {
        super(parameters);
    }

    @Override
    protected StatusForXmlRpc getReturnStatus() {
        return result.mStatus;
    }

    @Override
    protected void executeVdsBrokerCommand() {
        GlusterVolumeGeoRepSessionVDSParameters parameter = getParameters();
        result = getBroker().glusterVolumeGeoRepSessionStatus(parameter.getVolumeName(),
                        parameter.getSlaveHost(),
                        parameter.getSlaveVolume());
        proceedProxyReturnValue();
        if (getVDSReturnValue().getSucceeded()) {
            setReturnValue(result.getGeoRepDetails());
        }
    }

}
