package org.ovirt.engine.core.vdsbroker.gluster;

import org.ovirt.engine.core.common.vdscommands.gluster.GlusterVolumeVDSParameters;

public class DeleteAllGlusterVolumeSnapshotsVDSCommand<P extends GlusterVolumeVDSParameters> extends AbstractGlusterBrokerCommand<P> {
    public DeleteAllGlusterVolumeSnapshotsVDSCommand(P parameters) {
        super(parameters);
    }

    @Override
    protected void executeVdsBrokerCommand() {
        String volumeName = getParameters().getVolumeName();
        status = getBroker().glusterVolumeSnapshotDeleteAll(volumeName);
        proceedProxyReturnValue();
    }
}
