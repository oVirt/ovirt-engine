package org.ovirt.engine.core.vdsbroker.gluster;

import org.ovirt.engine.core.common.vdscommands.gluster.GlusterVolumeRemoveBricksVDSParameters;
import org.ovirt.engine.core.vdsbroker.vdsbroker.StatusForXmlRpc;

public class GlusterVolumeRemoveBricksVDSCommand<P extends GlusterVolumeRemoveBricksVDSParameters> extends AbstractGlusterBrokerCommand<P> {

    public GlusterVolumeRemoveBricksVDSCommand(P parameters) {
        super(parameters);
    }

    @Override
    protected StatusForXmlRpc getReturnStatus() {
        return status.mStatus;
    }

    @Override
    protected void ExecuteVdsBrokerCommand() {
        status =
                getBroker().glusterVolumeRemoveBrickForce(getParameters().getVolumeName(),
                        getParameters().getBrickDirectories().toArray(new String[0]), getParameters().getReplicaCount());

        proceedProxyReturnValue();
    }
}
