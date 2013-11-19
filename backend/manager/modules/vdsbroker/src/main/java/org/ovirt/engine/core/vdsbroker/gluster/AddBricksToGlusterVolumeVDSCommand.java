package org.ovirt.engine.core.vdsbroker.gluster;

import java.util.List;

import org.ovirt.engine.core.common.gluster.GlusterFeatureSupported;
import org.ovirt.engine.core.common.utils.gluster.GlusterCoreUtil;
import org.ovirt.engine.core.common.vdscommands.gluster.GlusterVolumeBricksActionVDSParameters;

public class AddBricksToGlusterVolumeVDSCommand<P extends GlusterVolumeBricksActionVDSParameters> extends AbstractGlusterBrokerCommand<P> {
    public AddBricksToGlusterVolumeVDSCommand(P parameters) {
        super(parameters);
    }

    @Override
    protected void executeVdsBrokerCommand() {
        boolean isForce = getParameters().isForce();
        boolean supportForceCreateVolume =
                GlusterFeatureSupported.glusterForceCreateVolumeSupported(getParameters().getClusterVersion());
        List<String> bricks = GlusterCoreUtil.getQualifiedBrickList(getParameters().getBricks());
        status = supportForceCreateVolume ?
                getBroker().glusterVolumeBrickAdd(getParameters().getVolumeName(),
                        bricks.toArray(new String[0]),
                        getParameters().getReplicaCount(),
                        getParameters().getStripeCount(),
                        isForce) :
                getBroker().glusterVolumeBrickAdd(getParameters().getVolumeName(),
                        bricks.toArray(new String[0]),
                        getParameters().getReplicaCount(),
                        getParameters().getStripeCount());

        proceedProxyReturnValue();
    }
}
