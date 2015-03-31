package org.ovirt.engine.core.vdsbroker.gluster;

import org.ovirt.engine.core.common.vdscommands.gluster.GlusterVolumeSnapshotVDSParameters;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.vdsbroker.vdsbroker.StatusForXmlRpc;

public class GetGlusterVolumeSnapshotConfigInfoVDSCommand<P extends GlusterVolumeSnapshotVDSParameters> extends AbstractGlusterBrokerCommand<P> {
    private GlusterVolumeSnapshotConfigReturnForXmlRpc infoReturn;

    public GetGlusterVolumeSnapshotConfigInfoVDSCommand(P parameters) {
        super(parameters);
    }

    @Override
    protected StatusForXmlRpc getReturnStatus() {
        return this.infoReturn.getXmlRpcStatus();
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
