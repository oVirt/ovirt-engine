package org.ovirt.engine.core.vdsbroker.gluster;

import org.ovirt.engine.core.common.vdscommands.gluster.GlusterVolumeActionVDSParameters;

public class StartGlusterVolumeVDSCommand<P extends GlusterVolumeActionVDSParameters> extends AbstractGlusterBrokerCommand<P> {

    public StartGlusterVolumeVDSCommand(P parameters) {
        super(parameters);
    }

    @Override
    protected void executeVdsBrokerCommand() {
        status = getBroker().glusterVolumeStart(getParameters().getVolumeName(), getParameters().isForceAction());

        proceedProxyReturnValue();
    }
}
