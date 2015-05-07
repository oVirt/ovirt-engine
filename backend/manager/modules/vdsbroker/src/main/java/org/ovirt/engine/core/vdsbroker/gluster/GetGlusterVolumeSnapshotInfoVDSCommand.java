package org.ovirt.engine.core.vdsbroker.gluster;

import org.ovirt.engine.core.common.vdscommands.gluster.GlusterVolumeSnapshotVDSParameters;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.vdsbroker.vdsbroker.StatusForXmlRpc;

public class GetGlusterVolumeSnapshotInfoVDSCommand<P extends GlusterVolumeSnapshotVDSParameters> extends AbstractGlusterBrokerCommand<P> {
    private GlusterVolumeSnapshotInfoReturnForXmlRpc infoReturn;

    public GetGlusterVolumeSnapshotInfoVDSCommand(P parameters) {
        super(parameters);
    }

    @Override
    protected StatusForXmlRpc getReturnStatus() {
        return infoReturn.getXmlRpcStatus();
    }

    @Override
    protected void executeVdsBrokerCommand() {
        Guid clusterId = getParameters().getClusterId();
        String volumeName = getParameters().getVolumeName();
        infoReturn = getBroker().glusterVolumeSnapshotList(clusterId, volumeName);
        proceedProxyReturnValue();

        if (getVDSReturnValue().getSucceeded()) {
            setReturnValue(infoReturn.getSnapshots());
        }
    }
}
