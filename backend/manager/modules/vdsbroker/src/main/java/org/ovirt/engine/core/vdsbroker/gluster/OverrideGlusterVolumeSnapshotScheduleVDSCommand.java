package org.ovirt.engine.core.vdsbroker.gluster;

import org.ovirt.engine.core.common.vdscommands.gluster.GlusterVolumeActionVDSParameters;

public class OverrideGlusterVolumeSnapshotScheduleVDSCommand<P extends GlusterVolumeActionVDSParameters> extends AbstractGlusterBrokerCommand<P> {
    public OverrideGlusterVolumeSnapshotScheduleVDSCommand(P params) {
        super(params);
    }

    @Override
    protected void executeVdsBrokerCommand() {
        status = getBroker().glusterSnapshotScheduleOverride(getParameters().isForceAction());
        proceedProxyReturnValue();
    }
}
