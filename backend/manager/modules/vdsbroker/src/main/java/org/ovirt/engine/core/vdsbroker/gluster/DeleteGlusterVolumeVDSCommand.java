package org.ovirt.engine.core.vdsbroker.gluster;

import org.ovirt.engine.core.common.vdscommands.gluster.GlusterVolumeVDSParameters;
import org.ovirt.engine.core.vdsbroker.vdsbroker.VdsBrokerCommand;

public class DeleteGlusterVolumeVDSCommand extends VdsBrokerCommand<GlusterVolumeVDSParameters> {
    public DeleteGlusterVolumeVDSCommand(GlusterVolumeVDSParameters parameters) {
        super(parameters);
    }

    @Override
    protected void ExecuteVdsBrokerCommand() {
        status = getBroker().glusterVolumeDelete(getParameters().getVolumeName());

        ProceedProxyReturnValue();
    }
}
