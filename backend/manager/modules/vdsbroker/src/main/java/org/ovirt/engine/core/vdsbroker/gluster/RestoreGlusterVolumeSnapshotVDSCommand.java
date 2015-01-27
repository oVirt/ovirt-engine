package org.ovirt.engine.core.vdsbroker.gluster;

import org.ovirt.engine.core.common.vdscommands.gluster.GlusterVolumeSnapshotActionVDSParameters;

public class RestoreGlusterVolumeSnapshotVDSCommand<P extends GlusterVolumeSnapshotActionVDSParameters> extends AbstractGlusterBrokerCommand<P> {
    public RestoreGlusterVolumeSnapshotVDSCommand(P parameters) {
        super(parameters);
    }

    @Override
    public void executeVdsBrokerCommand() {
        String snapshotName = getParameters().getSnapshotName();
        status = getBroker().glusterSnapshotRestore(snapshotName);
        proceedProxyReturnValue();
    }
}
