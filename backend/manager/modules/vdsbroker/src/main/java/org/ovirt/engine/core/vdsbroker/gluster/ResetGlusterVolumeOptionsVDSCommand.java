package org.ovirt.engine.core.vdsbroker.gluster;

import org.ovirt.engine.core.common.vdscommands.gluster.ResetGlusterVolumeOptionsVDSParameters;
import org.ovirt.engine.core.vdsbroker.vdsbroker.VdsBrokerCommand;

/**
 * VDS command to Reset gluster volume options
 */
public class ResetGlusterVolumeOptionsVDSCommand<P extends ResetGlusterVolumeOptionsVDSParameters> extends VdsBrokerCommand<P> {
    public ResetGlusterVolumeOptionsVDSCommand(P parameters) {
        super(parameters);
    }

    @Override
    protected void ExecuteVdsBrokerCommand() {
        status =
                getBroker().glusterVolumeReset(getParameters().getVolumeName(),
                        (getParameters().getVolumeOption() == null) ? "" : getParameters().getVolumeOption(),
                        getParameters().isforceAction());
        ProceedProxyReturnValue();
    }
}
