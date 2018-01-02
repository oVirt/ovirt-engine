package org.ovirt.engine.core.vdsbroker.gluster;

import org.ovirt.engine.core.common.vdscommands.gluster.ResetGlusterVolumeBrickActionVDSParameters;

public class ResetGlusterVolumeBrickVDSCommand<P extends ResetGlusterVolumeBrickActionVDSParameters> extends AbstractGlusterBrokerCommand<P> {
    public ResetGlusterVolumeBrickVDSCommand(P parameters) {
        super(parameters);
    }

    @Override
    protected void executeVdsBrokerCommand() {
        status = getBroker().glusterVolumeResetBrickStart(getParameters().getVolumeName(),
                getParameters().getExistingBrickDir());
        if(status.status.code == 0) {
             status = getBroker().glusterVolumeResetBrickCommitForce(getParameters().getVolumeName(),
                    getParameters().getExistingBrickDir());
        }
        proceedProxyReturnValue();
    }
}
