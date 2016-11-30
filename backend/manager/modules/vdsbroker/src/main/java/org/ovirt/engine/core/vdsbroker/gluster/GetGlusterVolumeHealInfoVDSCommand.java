package org.ovirt.engine.core.vdsbroker.gluster;

import org.ovirt.engine.core.common.vdscommands.gluster.GlusterVolumeVDSParameters;
import org.ovirt.engine.core.vdsbroker.vdsbroker.Status;

public class GetGlusterVolumeHealInfoVDSCommand<P extends GlusterVolumeVDSParameters> extends AbstractGlusterBrokerCommand<P> {

    private GlusterVolumesHealInfoReturn result;

    public GetGlusterVolumeHealInfoVDSCommand(P parameters) {
        super(parameters);
    }

    @Override
    protected Status getReturnStatus() {
        return result.getStatus();
    }

    @Override
    protected void executeVdsBrokerCommand() {
        result = getBroker().glusterVolumeHealInfo(getParameters().getVolumeName());
        proceedProxyReturnValue();
        setReturnValue(result.getUnSyncedEntries());
    }
}
