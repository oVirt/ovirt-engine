package org.ovirt.engine.core.vdsbroker.gluster;

import org.ovirt.engine.core.common.vdscommands.gluster.GlusterVolumeSnapshotVDSParameters;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.vdsbroker.vdsbroker.Status;

public class GetGlusterVolumeSnapshotConfigInfoVDSCommand<P extends GlusterVolumeSnapshotVDSParameters> extends AbstractGlusterBrokerCommand<P> {
    private GlusterVolumeSnapshotConfigReturn infoReturn;

    public GetGlusterVolumeSnapshotConfigInfoVDSCommand(P parameters) {
        super(parameters);
    }

    @Override
    protected Status getReturnStatus() {
        return this.infoReturn.getStatus();
    }

    @Override
    protected void executeVdsBrokerCommand() {
        Guid clusterId = getParameters().getClusterId();
        infoReturn = getBroker().glusterSnapshotConfigList(clusterId);
        proceedProxyReturnValue();

        if (getVDSReturnValue().getSucceeded()) {
            setReturnValue(infoReturn.getGlusterSnapshotConfigInfo());
        }
    }
}
