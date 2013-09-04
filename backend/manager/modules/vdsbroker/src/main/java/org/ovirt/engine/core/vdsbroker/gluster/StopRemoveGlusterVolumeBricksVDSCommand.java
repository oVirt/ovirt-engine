package org.ovirt.engine.core.vdsbroker.gluster;

import org.ovirt.engine.core.common.vdscommands.gluster.GlusterVolumeRemoveBricksVDSParameters;

public class StopRemoveGlusterVolumeBricksVDSCommand<P extends GlusterVolumeRemoveBricksVDSParameters> extends AbstractGlusterBrokerCommand<P> {

    public StopRemoveGlusterVolumeBricksVDSCommand(P parameters) {
        super(parameters);
    }

    @Override
    protected void executeVdsBrokerCommand() {
        status = getBroker().glusterVolumeRemoveBricksStop(getParameters().getVolumeName(),
                getParameters().getBrickDirectories().toArray(new String[0]),
                getParameters().getReplicaCount());

        proceedProxyReturnValue();
    }

}
