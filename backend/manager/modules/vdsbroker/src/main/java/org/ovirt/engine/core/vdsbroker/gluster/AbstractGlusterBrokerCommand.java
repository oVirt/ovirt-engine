package org.ovirt.engine.core.vdsbroker.gluster;

import org.ovirt.engine.core.common.errors.VDSError;
import org.ovirt.engine.core.common.errors.VdcBllErrors;
import org.ovirt.engine.core.common.vdscommands.VdsIdVDSCommandParametersBase;
import org.ovirt.engine.core.vdsbroker.vdsbroker.VdsBrokerCommand;

/**
 * Gluster Specific VDS broker. The main purpose is to handle gluster command errors.
 */
public abstract class AbstractGlusterBrokerCommand<P extends VdsIdVDSCommandParametersBase> extends VdsBrokerCommand<P> {

    public AbstractGlusterBrokerCommand(P parameters) {
        super(parameters);
    }

    @Override
    protected void ProceedProxyReturnValue() {
        VdcBllErrors returnStatus = GetReturnValueFromStatus(getReturnStatus());
        switch (returnStatus) {
        case GlusterVolumeCreateFailed:
        case GlusterVolumeStartFailed:
        case GlusterVolumeStopFailed:
        case GlusterVolumeSetOptionFailed:
        case GlusterVolumeResetOptionsFailed:
        case GlusterVolumeRebalanceStartFailed:
        case GlusterVolumeDeleteFailed:
        case AddBricksToGlusterVolumeFailed:
        case GlusterVolumeRemoveBricksFailed:
        case GlusterVolumeReplaceBrickStartFailed:
        case GlusterHostRemoveFailed:
        case GlusterAddHostFailed:
        case GlusterPeerListFailed:
            // Capture error from gluster command and record failure
            getVDSReturnValue().setVdsError(new VDSError(returnStatus, getReturnStatus().mMessage));
            getVDSReturnValue().setSucceeded(false);
            break;
        default:
            super.ProceedProxyReturnValue();
            break;
        }
    }
}
