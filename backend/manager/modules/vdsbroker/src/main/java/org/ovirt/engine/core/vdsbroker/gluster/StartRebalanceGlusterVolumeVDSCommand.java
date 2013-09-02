package org.ovirt.engine.core.vdsbroker.gluster;

import org.ovirt.engine.core.common.vdscommands.gluster.GlusterVolumeRebalanceVDSParameters;

public class StartRebalanceGlusterVolumeVDSCommand<P extends GlusterVolumeRebalanceVDSParameters> extends AbstractGlusterBrokerCommand<P> {
    public StartRebalanceGlusterVolumeVDSCommand(P parameters) {
        super(parameters);
    }

    @Override
    protected void ExecuteVdsBrokerCommand() {
        status =
                getBroker().glusterVolumeRebalanceStart(getParameters().getVolumeName(),
                        getParameters().isFixLayoutOnly(),
                        getParameters().isForceAction());

        proceedProxyReturnValue();
    }
}
