package org.ovirt.engine.core.vdsbroker.gluster;

import org.ovirt.engine.core.common.vdscommands.gluster.ReplaceGlusterVolumeBrickActionVDSParameters;

public class ReplaceGlusterVolumeBrickVDSCommand<P extends ReplaceGlusterVolumeBrickActionVDSParameters> extends AbstractGlusterBrokerCommand<P> {
    public ReplaceGlusterVolumeBrickVDSCommand(P parameters) {
        super(parameters);
    }

    @Override
    protected void executeVdsBrokerCommand() {
        status = getBroker().glusterVolumeReplaceBrickCommitForce(getParameters().getVolumeName(),
                            getParameters().getExistingBrickDir(),
                            getParameters().getNewBrickDir());
        proceedProxyReturnValue();
    }
}
