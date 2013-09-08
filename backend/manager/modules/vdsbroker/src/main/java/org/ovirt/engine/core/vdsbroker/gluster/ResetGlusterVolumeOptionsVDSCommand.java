package org.ovirt.engine.core.vdsbroker.gluster;

import org.ovirt.engine.core.common.vdscommands.gluster.ResetGlusterVolumeOptionsVDSParameters;

/**
 * VDS command to Reset gluster volume options
 */
public class ResetGlusterVolumeOptionsVDSCommand<P extends ResetGlusterVolumeOptionsVDSParameters> extends AbstractGlusterBrokerCommand<P> {
    public ResetGlusterVolumeOptionsVDSCommand(P parameters) {
        super(parameters);
    }

    @Override
    protected void executeVdsBrokerCommand() {
        status =
                getBroker().glusterVolumeReset(getParameters().getVolumeName(),
                        (getParameters().getVolumeOption() == null) ? "" : getParameters().getVolumeOption().getKey(),
                        getParameters().isforceAction());
        proceedProxyReturnValue();
    }
}
