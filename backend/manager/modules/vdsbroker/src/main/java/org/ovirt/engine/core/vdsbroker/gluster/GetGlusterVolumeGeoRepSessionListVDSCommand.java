package org.ovirt.engine.core.vdsbroker.gluster;

import org.ovirt.engine.core.common.vdscommands.gluster.GlusterVolumeGeoRepSessionVDSParameters;
import org.ovirt.engine.core.vdsbroker.vdsbroker.Status;

public class GetGlusterVolumeGeoRepSessionListVDSCommand<P extends GlusterVolumeGeoRepSessionVDSParameters> extends AbstractGlusterBrokerCommand<P> {
    GlusterVolumeGeoRepStatus result;

    public GetGlusterVolumeGeoRepSessionListVDSCommand(P parameters) {
        super(parameters);
    }

    @Override
    protected Status getReturnStatus() {
        return result.getStatus();
    }

    @Override
    protected void executeVdsBrokerCommand() {
        GlusterVolumeGeoRepSessionVDSParameters parameter = getParameters();
        if (parameter.getVolumeName() == null) {
            result = getBroker().glusterVolumeGeoRepSessionList();
        } else if (parameter.getSlaveHost() == null || parameter.getSlaveVolume() == null) {
            result = getBroker().glusterVolumeGeoRepSessionList(parameter.getVolumeName());
        } else {
            result =
                    getBroker().glusterVolumeGeoRepSessionList(parameter.getVolumeName(),
                            parameter.getSlaveHost(),
                            parameter.getSlaveVolume(),
                            parameter.getUserName());
        }
        proceedProxyReturnValue();
        if (getVDSReturnValue().getSucceeded()) {
            setReturnValue(result.getGeoRepSessions());
        }
    }

}
