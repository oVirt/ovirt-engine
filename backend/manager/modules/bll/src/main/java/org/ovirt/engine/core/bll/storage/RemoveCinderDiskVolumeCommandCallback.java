package org.ovirt.engine.core.bll.storage;

import java.util.List;

import org.apache.commons.httpclient.HttpStatus;
import org.ovirt.engine.core.common.action.RemoveCinderDiskVolumeParameters;
import org.ovirt.engine.core.common.businessentities.storage.CinderDisk;
import org.ovirt.engine.core.common.businessentities.storage.ImageStatus;
import org.ovirt.engine.core.compat.CommandStatus;
import org.ovirt.engine.core.compat.Guid;

import com.woorea.openstack.base.client.OpenStackResponseException;

public class RemoveCinderDiskVolumeCommandCallback extends AbstractCinderDiskCommandCallback<RemoveCinderDiskVolumeCommand<RemoveCinderDiskVolumeParameters>> {

    @Override
    public void doPolling(Guid cmdId, List<Guid> childCmdIds) {
        super.doPolling(cmdId, childCmdIds);
        CinderDisk removedVolume = getCommand().getParameters().getRemovedVolume();

        // In case the volume/snapshot has been deleted from Cinder.
        if (!getCinderBroker().isVolumeExistsByClassificationType(removedVolume)) {
            getCommand().setCommandStatus(CommandStatus.SUCCEEDED);
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
        log.error("Volume/Snapshot id '{}' has failed to be deleted", getDiskId());
        getCommand().endAction();
    }

    @Override
    public void onSucceeded(Guid cmdId, List<Guid> childCmdIds) {
        super.onSucceeded(cmdId, childCmdIds);
        log.info("Volume/Snapshot id '{}' has been deleted successfully from Cinder.", getDiskId());
        getCommand().endAction();
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
