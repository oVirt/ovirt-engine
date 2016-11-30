package org.ovirt.engine.core.vdsbroker.gluster;

import org.ovirt.engine.core.common.vdscommands.gluster.UpdateGlusterGeoRepKeysVDSParameters;
import org.ovirt.engine.core.vdsbroker.vdsbroker.Status;

public class UpdateGlusterGeoRepKeysVDSCommand<P extends UpdateGlusterGeoRepKeysVDSParameters> extends AbstractGlusterBrokerCommand<P> {

    public UpdateGlusterGeoRepKeysVDSCommand(P parameters) {
        super(parameters);
    }

    @Override
    protected Status getReturnStatus() {
        return status.status;
    }

    @Override
    protected void executeVdsBrokerCommand() {
        UpdateGlusterGeoRepKeysVDSParameters parameters = getParameters();
        status =
                getBroker().glusterGeoRepKeysUpdate(parameters.getGeoRepPubKeys(), parameters.getRemoteUserName());
        proceedProxyReturnValue();
    }

}
