package org.ovirt.engine.core.vdsbroker.gluster;

import org.ovirt.engine.core.common.vdscommands.VdsIdVDSCommandParametersBase;
import org.ovirt.engine.core.vdsbroker.irsbroker.OneUuidReturnForXmlRpc;
import org.ovirt.engine.core.vdsbroker.vdsbroker.StatusForXmlRpc;

public class GetGlusterHostUUIDVDSCommand<P extends VdsIdVDSCommandParametersBase> extends AbstractGlusterBrokerCommand<P> {

    private OneUuidReturnForXmlRpc glusterHostUUID;

    public GetGlusterHostUUIDVDSCommand(P parameters) {
        super(parameters);
    }

    @Override
    protected void executeVdsBrokerCommand() {
        glusterHostUUID = getBroker().glusterHostUUIDGet();

        proceedProxyReturnValue();
        if (getVDSReturnValue().getSucceeded()) {
            setReturnValue(glusterHostUUID.uuid);
        }
    }

    @Override
    protected StatusForXmlRpc getReturnStatus() {
        return glusterHostUUID.getXmlRpcStatus();
    }

}
