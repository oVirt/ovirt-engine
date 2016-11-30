package org.ovirt.engine.core.vdsbroker.gluster;

import org.ovirt.engine.core.common.vdscommands.gluster.GlusterVolumeInfoVDSParameters;
import org.ovirt.engine.core.vdsbroker.vdsbroker.Status;

public class GetGlusterVolumeInfoVDSCommand<P extends GlusterVolumeInfoVDSParameters> extends AbstractGlusterBrokerCommand<P> {
    private GlusterVolumesListReturn result;

    public GetGlusterVolumeInfoVDSCommand(P parameters) {
        super(parameters);
    }

    @Override
    protected Status getReturnStatus() {
        return this.result.getStatus();
    }

    @Override
    public void executeVdsBrokerCommand() {
        result = getBroker().glusterVolumeInfo(getParameters().getClusterId(), getParameters().getVolumeName());
        proceedProxyReturnValue();

        if (getVDSReturnValue().getSucceeded()) {
            setReturnValue(result.getVolumes());
        }
    }
}
