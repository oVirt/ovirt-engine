package org.ovirt.engine.core.vdsbroker.gluster;

import org.ovirt.engine.core.common.vdscommands.VdsIdVDSCommandParametersBase;
import org.ovirt.engine.core.vdsbroker.irsbroker.OneUuidReturn;
import org.ovirt.engine.core.vdsbroker.vdsbroker.Status;

public class GetGlusterHostUUIDVDSCommand<P extends VdsIdVDSCommandParametersBase> extends AbstractGlusterBrokerCommand<P> {

    private OneUuidReturn glusterHostUUID;

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
    protected Status getReturnStatus() {
        return glusterHostUUID.getStatus();
    }

}
