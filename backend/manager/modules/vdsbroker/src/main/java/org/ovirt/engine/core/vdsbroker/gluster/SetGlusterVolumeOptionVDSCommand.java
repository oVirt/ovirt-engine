package org.ovirt.engine.core.vdsbroker.gluster;

import org.ovirt.engine.core.common.vdscommands.gluster.GlusterVolumeOptionVDSParameters;
import org.ovirt.engine.core.vdsbroker.vdsbroker.VdsBrokerCommand;

/**
 * VDS command to set a gluster volume option
 */
public class SetGlusterVolumeOptionVDSCommand<P extends GlusterVolumeOptionVDSParameters> extends VdsBrokerCommand<P> {
    public SetGlusterVolumeOptionVDSCommand(P parameters) {
        super(parameters);
    }

    @Override
    protected void ExecuteVdsBrokerCommand() {
        status =
                getBroker().glusterVolumeSet(getParameters().getVolumeName(),
                        getParameters().getVolumeOption().getKey(),
                        getParameters().getVolumeOption().getValue());
        ProceedProxyReturnValue();
    }
}
