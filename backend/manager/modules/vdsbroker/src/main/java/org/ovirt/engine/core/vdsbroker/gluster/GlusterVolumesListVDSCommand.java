package org.ovirt.engine.core.vdsbroker.gluster;

import org.ovirt.engine.core.common.vdscommands.gluster.GlusterVolumesListVDSParameters;
import org.ovirt.engine.core.vdsbroker.vdsbroker.StatusForXmlRpc;

public class GlusterVolumesListVDSCommand<P extends GlusterVolumesListVDSParameters> extends AbstractGlusterBrokerCommand<P> {
    private GlusterVolumesListReturnForXmlRpc result;

    public GlusterVolumesListVDSCommand(P parameters) {
        super(parameters);
    }

    @Override
    protected StatusForXmlRpc getReturnStatus() {
        return result.getXmlRpcStatus();
    }

    @Override
    protected void executeVdsBrokerCommand() {
        result = getBroker().glusterVolumesList(getParameters().getClusterId());
        proceedProxyReturnValue();
        setReturnValue(result.getVolumes());
    }
}
