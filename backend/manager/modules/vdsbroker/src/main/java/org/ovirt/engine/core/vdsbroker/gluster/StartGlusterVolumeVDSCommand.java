package org.ovirt.engine.core.vdsbroker.gluster;

import org.ovirt.engine.core.common.vdscommands.gluster.GlusterVolumeActionVDSParameters;
import org.ovirt.engine.core.vdsbroker.vdsbroker.VdsBrokerCommand;

public class StartGlusterVolumeVDSCommand extends VdsBrokerCommand<GlusterVolumeActionVDSParameters> {
    public StartGlusterVolumeVDSCommand(GlusterVolumeActionVDSParameters parameters) {
        super(parameters);
    }

    @Override
    protected void ExecuteVdsBrokerCommand() {
        status = getBroker().glusterVolumeStart(getParameters().getVolumeName(), getParameters().isForceAction());

        ProceedProxyReturnValue();
    }
}
