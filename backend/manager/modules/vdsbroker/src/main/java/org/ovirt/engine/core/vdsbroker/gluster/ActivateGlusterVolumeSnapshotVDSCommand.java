package org.ovirt.engine.core.vdsbroker.gluster;

import org.ovirt.engine.core.common.vdscommands.gluster.GlusterVolumeActivateSnapshotVDSParameters;

public class ActivateGlusterVolumeSnapshotVDSCommand<P extends GlusterVolumeActivateSnapshotVDSParameters> extends AbstractGlusterBrokerCommand<P> {
    public ActivateGlusterVolumeSnapshotVDSCommand(P params) {
        super(params);
    }

    @Override
    protected void executeVdsBrokerCommand() {
        String snapshotName = getParameters().getSnapshotName();
        boolean force = getParameters().getForce();
        status = getBroker().glusterSnapshotActivate(snapshotName, force);
        proceedProxyReturnValue();
    }
}
