package org.ovirt.engine.core.vdsbroker.gluster;

import java.util.Objects;

import org.ovirt.engine.core.common.vdscommands.gluster.CreateGlusterVolumeSnapshotVDSParameters;
import org.ovirt.engine.core.vdsbroker.vdsbroker.Status;

public class CreateGlusterVolumeSnapshotVDSCommand<P extends CreateGlusterVolumeSnapshotVDSParameters> extends AbstractGlusterBrokerCommand<P> {
    private GlusterVolumeSnapshotCreateReturn returnValue;

    public CreateGlusterVolumeSnapshotVDSCommand(P params) {
        super(params);
    }

    @Override
    protected Status getReturnStatus() {
        return returnValue.getStatus();
    }

    @Override
    protected void executeVdsBrokerCommand() {
        String volumeName = getParameters().getVolumeName();
        String snapshotName = getParameters().getSnapshotName();
        String description = Objects.toString(getParameters().getDescription(), "");
        boolean force = getParameters().getForce();
        returnValue = getBroker().glusterVolumeSnapshotCreate(volumeName, snapshotName, description, force);
        proceedProxyReturnValue();

        if (getVDSReturnValue().getSucceeded()) {
            setReturnValue(returnValue.getSnapshot());
        }
    }
}
