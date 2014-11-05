package org.ovirt.engine.core.vdsbroker.gluster;

import org.ovirt.engine.core.common.vdscommands.gluster.GlusterVolumeSnapshotActionVDSParameters;

public class DeactivateGlusterVolumeSnapshotVDSCommand<P extends GlusterVolumeSnapshotActionVDSParameters> extends AbstractGlusterBrokerCommand<P> {
    public DeactivateGlusterVolumeSnapshotVDSCommand(P params) {
        super(params);
    }

    @Override
    protected void executeVdsBrokerCommand() {
        String snapshotName = getParameters().getSnapshotName();
        status = getBroker().glusterSnapshotDeactivate(snapshotName);
        proceedProxyReturnValue();
    }
}
