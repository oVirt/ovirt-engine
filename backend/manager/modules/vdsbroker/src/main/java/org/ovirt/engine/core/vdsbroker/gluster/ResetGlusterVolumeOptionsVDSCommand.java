package org.ovirt.engine.core.vdsbroker.gluster;

import org.ovirt.engine.core.common.vdscommands.gluster.ResetGlusterVolumeOptionsVDSParameters;
import org.ovirt.engine.core.vdsbroker.vdsbroker.VdsBrokerCommand;

/**
 * VDS command to set a gluster volume option
 */
public class ResetGlusterVolumeOptionsVDSCommand<P extends ResetGlusterVolumeOptionsVDSParameters> extends VdsBrokerCommand<P> {
    public ResetGlusterVolumeOptionsVDSCommand(P parameters) {
        super(parameters);
    }

    @Override
    protected void ExecuteVdsBrokerCommand() {
        status =
                getBroker().glusterVolumeOptionsReset(getParameters().getVolumeName(),
                        getParameters().getVolumeOption(),
                        getParameters().isforceAction());
        ProceedProxyReturnValue();
    }
}
