package org.ovirt.engine.core.vdsbroker.gluster;

import org.ovirt.engine.core.common.vdscommands.gluster.GlusterVolumesListVDSParameters;
import org.ovirt.engine.core.vdsbroker.vdsbroker.Status;

public class GlusterVolumesListVDSCommand<P extends GlusterVolumesListVDSParameters> extends AbstractGlusterBrokerCommand<P> {
    private GlusterVolumesListReturn result;

    public GlusterVolumesListVDSCommand(P parameters) {
        super(parameters);
    }

    @Override
    protected Status getReturnStatus() {
        return result.getStatus();
    }

    @Override
    protected void executeVdsBrokerCommand() {
        result = getBroker().glusterVolumesList(getParameters().getClusterId());
        proceedProxyReturnValue();
        setReturnValue(result.getVolumes());
    }
}
