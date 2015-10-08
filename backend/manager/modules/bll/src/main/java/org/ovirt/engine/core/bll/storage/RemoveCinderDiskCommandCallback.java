package org.ovirt.engine.core.bll.storage;

import java.util.List;

import org.apache.commons.httpclient.HttpStatus;
import org.ovirt.engine.core.bll.tasks.CommandCoordinatorUtil;
import org.ovirt.engine.core.common.action.RemoveCinderDiskParameters;
import org.ovirt.engine.core.common.businessentities.storage.CinderDisk;
import org.ovirt.engine.core.common.businessentities.storage.ImageStatus;
import org.ovirt.engine.core.compat.CommandStatus;
import org.ovirt.engine.core.compat.Guid;

import com.woorea.openstack.base.client.OpenStackResponseException;

public class RemoveCinderDiskCommandCallback extends AbstractCinderDiskCommandCallback<RemoveCinderDiskCommand<RemoveCinderDiskParameters>> {

    @Override
    public void doPolling(Guid cmdId, List<Guid> childCmdIds) {
        super.doPolling(cmdId, childCmdIds);
        CinderDisk removedVolume = getCommand().getParameters().getRemovedVolume();

        if (!getCinderBroker().isVolumeExistsByClassificationType(removedVolume)) {
            // Disk has been deleted successfully
            getCommand().setCommandStatus(CommandStatus.SUCCEEDED);
            return;
        }

        ImageStatus imageStatus = checkImageStatus(removedVolume);
        if (imageStatus != null && imageStatus != getDisk().getImageStatus()) {
            switch (imageStatus) {
            case ILLEGAL:
                getCommand().setCommandStatus(CommandStatus.FAILED);
                break;
            }
        }
    }

    private ImageStatus checkImageStatus(CinderDisk removedVolume) {
        try {
            return getCinderBroker().getImageStatusByClassificationType(removedVolume);
        } catch (OpenStackResponseException ex) {
            if (ex.getStatus() == HttpStatus.SC_NOT_FOUND) {
                // Send image status as OK, since the disk might already be deleted.
                log.info("Image status could not be provided since the cinder image might have already been removed from Cinder.");
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

    @Override
    public void onFailed(Guid cmdId, List<Guid> childCmdIds) {
        super.onFailed(cmdId, childCmdIds);
        getCommand().getParameters().setTaskGroupSuccess(false);
        log.error("Failed deleting volume/snapshot from Cinder. ID: {}", getDiskId());
        getCommand().endAction();
        CommandCoordinatorUtil.removeAllCommandsInHierarchy(cmdId);
    }

    @Override
    public void onSucceeded(Guid cmdId, List<Guid> childCmdIds) {
        super.onSucceeded(cmdId, childCmdIds);
        log.info("Volume/Snapshot has been successfully deleted from Cinder. ID: {}", getDiskId());
        getCommand().endAction();
        CommandCoordinatorUtil.removeAllCommandsInHierarchy(cmdId);
    }

    @Override
    protected Guid getDiskId() {
        return getCommand().getParameters().getRemovedVolume().getImageId();
    }

    @Override
    protected CinderDisk getDisk() {
        return getCommand().getParameters().getRemovedVolume();
    }

    @Override
    protected CinderBroker getCinderBroker() {
        return getCommand().getCinderBroker();
    }
}
