package org.ovirt.engine.core.vdsbroker.gluster;

import org.ovirt.engine.core.common.vdscommands.gluster.GlusterVolumeOptionVDSParameters;

/**
 * VDS command to set a gluster volume option
 */
public class SetGlusterVolumeOptionVDSCommand<P extends GlusterVolumeOptionVDSParameters> extends AbstractGlusterBrokerCommand<P> {
    public SetGlusterVolumeOptionVDSCommand(P parameters) {
        super(parameters);
    }

    @Override
    protected void executeVdsBrokerCommand() {
        status =
                getBroker().glusterVolumeSet(getParameters().getVolumeName(),
                        getParameters().getVolumeOption().getKey(),
                        getParameters().getVolumeOption().getValue());
        proceedProxyReturnValue();
    }
}
