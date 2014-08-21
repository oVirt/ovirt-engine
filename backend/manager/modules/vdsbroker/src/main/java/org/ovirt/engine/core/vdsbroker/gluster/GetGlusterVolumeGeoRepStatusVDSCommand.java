package org.ovirt.engine.core.vdsbroker.gluster;

import org.ovirt.engine.core.common.vdscommands.gluster.GlusterVolumeGeoRepSessionVDSParameters;
import org.ovirt.engine.core.vdsbroker.vdsbroker.StatusForXmlRpc;

public class GetGlusterVolumeGeoRepStatusVDSCommand<P extends GlusterVolumeGeoRepSessionVDSParameters> extends AbstractGlusterBrokerCommand<P> {
    GlusterVolumeGeoRepStatusForXmlRpc result;

    public GetGlusterVolumeGeoRepStatusVDSCommand(P parameters) {
        super(parameters);
    }

    @Override
    protected StatusForXmlRpc getReturnStatus() {
        return result.mStatus;
    }

    @Override
    protected void executeVdsBrokerCommand() {
        GlusterVolumeGeoRepSessionVDSParameters parameter = getParameters();
        if (parameter.getVolumeName() == null) {
            result = getBroker().glusterVolumeGeoRepStatus();
        } else if (parameter.getSlaveHost() == null || parameter.getSlaveVolume() == null) {
            result = getBroker().glusterVolumeGeoRepStatus(parameter.getVolumeName());
        } else {
            result = getBroker().glusterVolumeGeoRepStatus(parameter.getVolumeName(), parameter.getSlaveHost(), parameter.getSlaveVolume());
        }
        proceedProxyReturnValue();
        if (getVDSReturnValue().getSucceeded()) {
            setReturnValue(result.getGeoRepSessions());
        }
    }

}
