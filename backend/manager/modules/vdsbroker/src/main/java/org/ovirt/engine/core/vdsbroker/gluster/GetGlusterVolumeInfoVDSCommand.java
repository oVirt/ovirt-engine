package org.ovirt.engine.core.vdsbroker.gluster;

import org.ovirt.engine.core.common.vdscommands.gluster.GlusterVolumeInfoVDSParameters;
import org.ovirt.engine.core.vdsbroker.vdsbroker.StatusForXmlRpc;

public class GetGlusterVolumeInfoVDSCommand<P extends GlusterVolumeInfoVDSParameters> extends AbstractGlusterBrokerCommand<P> {
    private GlusterVolumesListReturnForXmlRpc result;

    public GetGlusterVolumeInfoVDSCommand(P parameters) {
        super(parameters);
    }

    @Override
    protected StatusForXmlRpc getReturnStatus() {
        return this.result.getXmlRpcStatus();
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
