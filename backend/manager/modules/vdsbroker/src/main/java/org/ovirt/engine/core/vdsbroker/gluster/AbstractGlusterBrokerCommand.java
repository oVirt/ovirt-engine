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
    protected void proceedProxyReturnValue() {
        VdcBllErrors returnStatus = getReturnValueFromStatus(getReturnStatus());
        switch (returnStatus) {
        case GlusterGeneralException:
        case GlusterPermissionDeniedException:
        case GlusterSyntaxErrorException:
        case GlusterMissingArgumentException:
        case GlusterCmdExecFailedException:
        case GlusterXmlErrorException:
        case GlusterVolumeCreateFailed:
        case GlusterVolumeStartFailed:
        case GlusterVolumeStopFailed:
        case AddBricksToGlusterVolumeFailed:
        case GlusterVolumeSetOptionFailed:
        case GlusterVolumeRebalanceStartFailed:
        case GlusterVolumeGeoRepSessionStartFailed:
        case GlusterVolumeDeleteFailed:
        case GlusterVolumeReplaceBrickStartFailed:
        case GlusterVolumeListFailed:
        case GlusterVolumeOptionInfoFailed:
        case GlusterVolumeResetOptionsFailed:
        case GlusterVolumeRemoveBricksFailed:
        case GlusterVolumeProfileStartFailed:
        case GlusterVolumeGeoRepSessionPauseFailed:
        case GlusterVolumeProfileStopFailed:
        case GlusterAddHostFailed:
        case GlusterHostRemoveFailedException:
        case GlusterHostIsNotPartOfCluster:
        case GlusterPeerListFailed:
        case GlusterVolumeStatusFailed:
        case GlusterVolumeProfileInfoFailed:
        case GlusterHookFailed:
        case GlusterHookEnableFailed:
        case GlusterHookDisableFailed:
        case GlusterHookNotFound:
        case GlusterHookListException:
        case GlusterHostUUIDNotFound:
        case GlusterHookConflict:
        case GlusterServicesListFailed:
        case GlusterHookUpdateFailed:
        case GlusterHookAlreadyExists:
        case GlusterHookChecksumMismatch:
        case GlusterHookAddFailed:
        case GlusterHookRemoveFailed:
        case GlusterServicesActionFailed:
        case GlusterServiceActionNotSupported:
        case GlusterVolumeRebalanceStopFailed:
        case GlusterVolumeStatusAllFailedException:
        case GlusterVolumeRebalanceStatusFailedException:
        case GlusterVolumeGeoRepSessionResumeFailed:
        case GlusterVolumeRemoveBricksStartFailed:
        case GlusterVolumeRemoveBricksStopFailed:
        case GlusterVolumeRemoveBrickStatusFailed:
        case GlusterVolumeRemoveBricksCommitFailed:
        case GlusterVolumeGeoRepStatusFailed:
        case GlusterGeoRepConfigFailed:
        case GlusterLibgfapiException:
        case GlfsStatvfsException:
        case GlfsInitException:
        case GlfsFiniException:
        case GlusterGeoRepSessionDeleteFailedException:
        case GlusterVolumeGeoRepSessionStopFailed:
            // Capture error from gluster command and record failure
            getVDSReturnValue().setVdsError(new VDSError(returnStatus, getReturnStatus().mMessage));
            getVDSReturnValue().setSucceeded(false);
            break;
        default:
            super.proceedProxyReturnValue();
            break;
        }
    }
}
