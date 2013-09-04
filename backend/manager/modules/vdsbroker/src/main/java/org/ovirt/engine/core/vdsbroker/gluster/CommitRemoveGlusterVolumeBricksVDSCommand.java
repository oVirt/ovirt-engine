package org.ovirt.engine.core.vdsbroker.gluster;

import org.ovirt.engine.core.common.vdscommands.gluster.GlusterVolumeRemoveBricksVDSParameters;

public class CommitRemoveGlusterVolumeBricksVDSCommand<P extends GlusterVolumeRemoveBricksVDSParameters> extends AbstractGlusterBrokerCommand<P> {

    public CommitRemoveGlusterVolumeBricksVDSCommand(P parameters) {
        super(parameters);
    }

    @Override
    protected void executeVdsBrokerCommand() {
        status = getBroker().glusterVolumeRemoveBricksCommit(getParameters().getVolumeName(),
                getParameters().getBrickDirectories().toArray(new String[0]),
                getParameters().getReplicaCount());

        proceedProxyReturnValue();
    }

}
