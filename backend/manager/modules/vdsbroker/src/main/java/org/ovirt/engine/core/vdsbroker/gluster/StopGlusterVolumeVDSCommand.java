package org.ovirt.engine.core.vdsbroker.gluster;

import org.ovirt.engine.core.common.vdscommands.gluster.GlusterVolumeActionVDSParameters;
import org.ovirt.engine.core.vdsbroker.vdsbroker.VdsBrokerCommand;

public class StopGlusterVolumeVDSCommand extends VdsBrokerCommand<GlusterVolumeActionVDSParameters> {
    public StopGlusterVolumeVDSCommand(GlusterVolumeActionVDSParameters parameters) {
        super(parameters);
    }

    @Override
    protected void ExecuteVdsBrokerCommand() {
        status = getBroker().glusterVolumeStop(getParameters().getVolumeName(), getParameters().isForceAction());

        ProceedProxyReturnValue();
    }
}
