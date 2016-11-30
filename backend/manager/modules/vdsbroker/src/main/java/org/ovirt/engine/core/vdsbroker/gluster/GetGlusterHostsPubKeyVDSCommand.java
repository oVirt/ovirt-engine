package org.ovirt.engine.core.vdsbroker.gluster;

import org.ovirt.engine.core.common.vdscommands.VdsIdVDSCommandParametersBase;
import org.ovirt.engine.core.vdsbroker.vdsbroker.Status;

public class GetGlusterHostsPubKeyVDSCommand<P extends VdsIdVDSCommandParametersBase> extends AbstractGlusterBrokerCommand<P> {

    private GlusterHostsPubKeyReturn result;

    public GetGlusterHostsPubKeyVDSCommand(P parameters) {
        super(parameters);
    }

    @Override
    protected Status getReturnStatus() {
        return result.getStatus();
    }

    @Override
    protected void executeVdsBrokerCommand() {
        result = getBroker().glusterGeoRepKeysGet();
        proceedProxyReturnValue();
        setReturnValue(result.getGeoRepPublicKeys());
    }

}
