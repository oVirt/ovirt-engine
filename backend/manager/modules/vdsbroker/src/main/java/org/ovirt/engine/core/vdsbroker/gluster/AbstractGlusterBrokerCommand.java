package org.ovirt.engine.core.vdsbroker.gluster;

import org.ovirt.engine.core.common.errors.EngineError;
import org.ovirt.engine.core.common.errors.VDSError;
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
        EngineError returnStatus = getReturnValueFromStatus(getReturnStatus());
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
        case GlusterVolumeReplaceBrickFailed:
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
        case GlusterVolumeEmptyCheckFailed:
        case GlusterGeoRepPublicKeyFileCreateFailed:
        case GlusterGeoRepPublicKeyFileReadError:
        case GlusterGeoRepUserNotFound:
        case GlusterGeoRepPublicKeyWriteFailed:
        case GlusterGeoRepExecuteMountBrokerOptFailed:
        case GlusterGeoRepExecuteMountBrokerUserAddFailed:
        case GlusterMountBrokerRootCreateFailed:
        case GlusterGeoRepSessionCreateFailed:
        case GlusterVolumeGeoRepSessionResumeFailed:
        case GlusterGeoRepException:
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
        case GlusterSnapshotException:
        case GlusterSnapshotInfoFailedException:
        case GlusterSnapshotDeleteFailedException:
        case GlusterSnapshotActivateFailedException:
        case GlusterSnapshotDeactivateFailedException:
        case GlusterSnapshotRestoreFailedException:
        case GlusterSnapshotCreateFailedException:
        case GlusterSnapshotConfigFailedException:
        case GlusterSnapshotConfigSetFailedException:
        case GlusterSnapshotConfigGetFailedException:
        case GlusterHostStorageDeviceNotFoundException:
        case GlusterHostStorageDeviceInUseException:
        case GlusterHostStorageDeviceMountFailedException:
        case GlusterHostStorageDeviceFsTabFoundException:
        case GlusterHostStorageDevicePVCreateFailedException:
        case GlusterHostStorageDeviceLVConvertFailedException:
        case GlusterHostStorageDeviceLVChangeFailedException:
        case GlusterHostStorageDeviceMakeDirsFailedException:
        case GlusterHostStorageMountPointInUseException:
        case GlusterHostStorageDeviceVGCreateFailedException:
        case GlusterHostStorageDeviceVGScanFailedException:
        case GlusterHostFailedToSetSelinuxContext:
        case GlusterHostFailedToRunRestorecon:
        case GlusterSnapshotScheduleFlagUpdateFailedException:
        case GlusterDisableSnapshotScheduleFailedException:
        case GlusterMetaVolumeMountFailedException:
        case GlusterMetaVolumeFstabUpdateFailedException:
        case GlusterProcessesStopFailedException:
        case GlusterVolumeHealInfoFailedException:
        case GlusterEventException:
        case GlusterWebhookAddException:
        case GlusterWebhookSyncException:
        case GlusterWebhookUpdateException:
        case GlusterWebhookDeleteException:
        case GlusterVolumeResetBrickStartFailed:
        case GlusterVolumeResetBrickFailed:
            // Capture error from gluster command and record failure
            getVDSReturnValue().setVdsError(new VDSError(returnStatus, getReturnStatus().message));
            getVDSReturnValue().setSucceeded(false);
            break;
        default:
            super.proceedProxyReturnValue();
            break;
        }
    }
}
