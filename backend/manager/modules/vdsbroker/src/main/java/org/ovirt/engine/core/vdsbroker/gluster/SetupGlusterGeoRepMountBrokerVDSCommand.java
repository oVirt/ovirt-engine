package org.ovirt.engine.core.vdsbroker.gluster;

import org.ovirt.engine.core.common.vdscommands.gluster.SetUpGlusterGeoRepMountBrokerVDSParameters;

public class SetupGlusterGeoRepMountBrokerVDSCommand<P extends SetUpGlusterGeoRepMountBrokerVDSParameters> extends AbstractGlusterBrokerCommand<P> {

    public SetupGlusterGeoRepMountBrokerVDSCommand(P parameters) {
        super(parameters);
    }

    @Override
    protected void executeVdsBrokerCommand() {
        SetUpGlusterGeoRepMountBrokerVDSParameters parameters = getParameters();
        status =
                getBroker().glusterGeoRepMountBrokerSetup(parameters.getRemoteVolumeName(),
                        parameters.getRemoteUserName(),
                        parameters.getRemoteGroupName(), parameters.isPartial());
        proceedProxyReturnValue();
    }

}
