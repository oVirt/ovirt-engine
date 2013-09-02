package org.ovirt.engine.core.vdsbroker.gluster;

import java.util.List;

import org.ovirt.engine.core.common.utils.gluster.GlusterCoreUtil;
import org.ovirt.engine.core.common.vdscommands.gluster.GlusterVolumeBricksActionVDSParameters;

public class AddBricksToGlusterVolumeVDSCommand<P extends GlusterVolumeBricksActionVDSParameters> extends AbstractGlusterBrokerCommand<P> {
    public AddBricksToGlusterVolumeVDSCommand(P parameters) {
        super(parameters);
    }

    @Override
    protected void ExecuteVdsBrokerCommand() {
        List<String> bricks = GlusterCoreUtil.getQualifiedBrickList(getParameters().getBricks());
        status =
                getBroker().glusterVolumeBrickAdd(getParameters().getVolumeName(),
                        bricks.toArray(new String[0]),
                        getParameters().getReplicaCount(),
                        getParameters().getStripeCount());

        proceedProxyReturnValue();
    }
}
