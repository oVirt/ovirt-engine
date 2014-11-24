package org.ovirt.engine.core.vdsbroker.gluster;

import org.ovirt.engine.core.common.vdscommands.gluster.CreateGlusterVolumeSnapshotVDSParameters;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.vdsbroker.irsbroker.OneUuidReturnForXmlRpc;
import org.ovirt.engine.core.vdsbroker.vdsbroker.StatusForXmlRpc;

public class CreateGlusterVolumeSnapshotVDSCommand<P extends CreateGlusterVolumeSnapshotVDSParameters> extends AbstractGlusterBrokerCommand<P> {
    private OneUuidReturnForXmlRpc uuidReturn;

    public CreateGlusterVolumeSnapshotVDSCommand(P params) {
        super(params);
    }

    @Override
    protected StatusForXmlRpc getReturnStatus() {
        return uuidReturn.mStatus;
    }

    @Override
    protected void executeVdsBrokerCommand() {
        String volumeName = getParameters().getVolumeName();
        String snapshotName = getParameters().getSnapshotName();
        String description = getParameters().getDescription() == null ? "" : getParameters().getDescription();
        boolean force = getParameters().getForce();
        uuidReturn = getBroker().glusterSnapshotCreate(volumeName, snapshotName, description, force);
        proceedProxyReturnValue();

        if (getVDSReturnValue().getSucceeded()) {
            setReturnValue(Guid.createGuidFromStringDefaultEmpty(uuidReturn.mUuid));
        }
    }
}
