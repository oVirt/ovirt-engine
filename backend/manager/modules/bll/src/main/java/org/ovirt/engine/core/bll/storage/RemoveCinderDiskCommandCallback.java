package org.ovirt.engine.core.bll.storage;

import java.util.List;

import org.ovirt.engine.core.bll.tasks.CommandCoordinatorUtil;
import org.ovirt.engine.core.common.action.RemoveCinderDiskParameters;
import org.ovirt.engine.core.common.businessentities.storage.CinderDisk;
import org.ovirt.engine.core.common.businessentities.storage.ImageStatus;
import org.ovirt.engine.core.compat.CommandStatus;
import org.ovirt.engine.core.compat.Guid;

public class RemoveCinderDiskCommandCallback extends AbstractCinderDiskCommandCallback<RemoveCinderDiskCommand<RemoveCinderDiskParameters>> {

    @Override
    public void doPolling(Guid cmdId, List<Guid> childCmdIds) {
        super.doPolling(cmdId, childCmdIds);
        CinderDisk removedVolume = getCommand().getParameters().getRemovedVolume();

        if (getCinderBroker().isVolumeExistsByClassificationType(removedVolume)) {
            // Disk has been deleted successfully
            getCommand().setCommandStatus(CommandStatus.SUCCEEDED);
            return;
        }

        ImageStatus imageStatus = checkImageStatus(removedVolume);
        if (imageStatus != null && imageStatus != disk.getImageStatus()) {
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
        } catch (Exception e) {
            log.error("An exception occured while verifying status for volume id '{0}' with the following exception: {1}.",
                    removedVolume.getImageId(),
                    e);
            return ImageStatus.ILLEGAL;
        }
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
