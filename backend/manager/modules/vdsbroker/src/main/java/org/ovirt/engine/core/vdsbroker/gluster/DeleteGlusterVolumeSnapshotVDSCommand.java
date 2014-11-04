package org.ovirt.engine.core.vdsbroker.gluster;

import org.ovirt.engine.core.common.vdscommands.gluster.GlusterVolumeSnapshotActionVDSParameters;

public class DeleteGlusterVolumeSnapshotVDSCommand<P extends GlusterVolumeSnapshotActionVDSParameters> extends AbstractGlusterBrokerCommand<P> {
    public DeleteGlusterVolumeSnapshotVDSCommand(P params) {
        super(params);
    }

    @Override
    protected void executeVdsBrokerCommand() {
        String snapshotName = getParameters().getSnapshotName();
        status = getBroker().glusterSnapshotDelete(snapshotName);
        proceedProxyReturnValue();
    }
}
