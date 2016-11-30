package org.ovirt.engine.core.vdsbroker.gluster;

import org.ovirt.engine.core.common.vdscommands.gluster.GlusterVolumeGeoRepSessionVDSParameters;
import org.ovirt.engine.core.vdsbroker.vdsbroker.Status;

public class GetGlusterVolumeGeoRepSessionStatusVDSCommand<P extends GlusterVolumeGeoRepSessionVDSParameters> extends AbstractGlusterBrokerCommand<P> {
    GlusterVolumeGeoRepStatusDetail result;

    public GetGlusterVolumeGeoRepSessionStatusVDSCommand(P parameters) {
        super(parameters);
    }

    @Override
    protected Status getReturnStatus() {
        return result.getStatus();
    }

    @Override
    protected void executeVdsBrokerCommand() {
        GlusterVolumeGeoRepSessionVDSParameters parameter = getParameters();
        result = getBroker().glusterVolumeGeoRepSessionStatus(parameter.getVolumeName(),
                parameter.getSlaveHost(),
                parameter.getSlaveVolume(),
                parameter.getUserName());
        proceedProxyReturnValue();
        if (getVDSReturnValue().getSucceeded()) {
            setReturnValue(result.getGeoRepDetails());
        }
    }

}
