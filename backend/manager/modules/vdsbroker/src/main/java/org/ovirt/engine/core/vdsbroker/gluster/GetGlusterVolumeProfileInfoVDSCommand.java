package org.ovirt.engine.core.vdsbroker.gluster;

import org.ovirt.engine.core.common.vdscommands.gluster.GlusterVolumeProfileInfoVDSParameters;
import org.ovirt.engine.core.vdsbroker.vdsbroker.Status;

public class GetGlusterVolumeProfileInfoVDSCommand<P extends GlusterVolumeProfileInfoVDSParameters> extends AbstractGlusterBrokerCommand<P> {
    private GlusterVolumeProfileInfoReturn result;

    public GetGlusterVolumeProfileInfoVDSCommand(P parameters) {
        super(parameters);
    }

    @Override
    protected Status getReturnStatus() {
        return result.getStatus();
    }

    @Override
    protected void executeVdsBrokerCommand() {
        result = getBroker().glusterVolumeProfileInfo(getParameters().getClusterId(), getParameters().getVolumeName(), getParameters().isNfs());
        proceedProxyReturnValue();
        setReturnValue(result.getGlusterVolumeProfileInfo());
    }
}
