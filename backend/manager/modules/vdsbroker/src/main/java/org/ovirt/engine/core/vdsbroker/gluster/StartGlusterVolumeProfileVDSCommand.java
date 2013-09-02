package org.ovirt.engine.core.vdsbroker.gluster;

import org.ovirt.engine.core.common.vdscommands.gluster.GlusterVolumeVDSParameters;

public class StartGlusterVolumeProfileVDSCommand<P extends GlusterVolumeVDSParameters> extends AbstractGlusterBrokerCommand<P> {
    public StartGlusterVolumeProfileVDSCommand(P parameters) {
        super(parameters);
    }

    @Override
    protected void ExecuteVdsBrokerCommand() {
        status = getBroker().glusterVolumeProfileStart(getParameters().getVolumeName());

        proceedProxyReturnValue();
    }
}
