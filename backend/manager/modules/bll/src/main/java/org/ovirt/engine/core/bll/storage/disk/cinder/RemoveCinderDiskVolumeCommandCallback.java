package org.ovirt.engine.core.bll.storage.disk.cinder;

import java.util.List;

import javax.enterprise.inject.Typed;

import org.apache.http.HttpStatus;
import org.ovirt.engine.core.bll.CommandBase;
import org.ovirt.engine.core.bll.ConcurrentChildCommandsExecutionCallback;
import org.ovirt.engine.core.common.action.RemoveCinderDiskVolumeParameters;
import org.ovirt.engine.core.common.businessentities.storage.CinderDisk;
import org.ovirt.engine.core.common.businessentities.storage.ImageStatus;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.backendcompat.CommandExecutionStatus;

import com.woorea.openstack.base.client.OpenStackResponseException;

@Typed(RemoveCinderDiskVolumeCommandCallback.class)
public class RemoveCinderDiskVolumeCommandCallback extends ConcurrentChildCommandsExecutionCallback {

    @Override
    protected void childCommandsExecutionEnded(CommandBase<?> command,
            boolean anyFailed,
            List<Guid> childCmdIds,
            CommandExecutionStatus status,
            int completedChildren) {

        RemoveCinderDiskVolumeCommand<RemoveCinderDiskVolumeParameters> removeCinderDiskVolumeCommand =
                (RemoveCinderDiskVolumeCommand<RemoveCinderDiskVolumeParameters>) command;
        CinderDisk removedVolume = removeCinderDiskVolumeCommand.getParameters().getRemovedVolume();
        // In case the volume/snapshot has been deleted from Cinder.
        if (!removeCinderDiskVolumeCommand.getCinderBroker().isVolumeExistsByClassificationType(removedVolume)) {
            setCommandEndStatus(command, false, status, childCmdIds);
        }

        ImageStatus imageStatus = checkImageStatus(removedVolume, removeCinderDiskVolumeCommand);
        if (imageStatus != null && imageStatus != removedVolume.getImageStatus()) {
            switch (imageStatus) {
            case ILLEGAL:
                setCommandEndStatus(command, true, status, childCmdIds);
                break;
            }
        }
    }

    private ImageStatus checkImageStatus(CinderDisk removedVolume,
            RemoveCinderDiskVolumeCommand removeCinderDiskVolumeCommand) {
        try {
            return removeCinderDiskVolumeCommand.getCinderBroker().getImageStatusByClassificationType(removedVolume);
        } catch (OpenStackResponseException ex) {
            if (ex.getStatus() == HttpStatus.SC_NOT_FOUND) {
                // Send image status as OK, since the disk might already be deleted.
                log.info(
                        "Image status could not be provided since the cinder image might have already been removed from Cinder.");
                return ImageStatus.OK;
            }
            logError(removedVolume, ex);
        } catch (Exception e) {
            logError(removedVolume, e);
        }
        return ImageStatus.ILLEGAL;
    }

    private void logError(CinderDisk removedVolume, Exception ex) {
        log.error("An exception occurred while verifying status for volume id '{}' with the following exception: {}.",
                removedVolume.getImageId(),
                ex);
    }
}
