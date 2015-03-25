package org.ovirt.engine.core.bll.storage;

import org.ovirt.engine.core.bll.ImagesHandler;
import org.ovirt.engine.core.bll.tasks.CommandCoordinatorUtil;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.action.RemoveDiskParameters;
import org.ovirt.engine.core.common.businessentities.storage.CinderDisk;
import org.ovirt.engine.core.common.businessentities.storage.DiskImage;
import org.ovirt.engine.core.common.businessentities.storage.ImageStatus;
import org.ovirt.engine.core.compat.CommandStatus;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.AuditLogDirector;

import java.util.List;

public class RemoveCinderDiskCommandCallback extends AbstractCinderDiskCommandCallback<RemoveCinderDiskCommand<RemoveDiskParameters>> {

    @Override
    public void doPolling(Guid cmdId, List<Guid> childCmdIds) {
        super.doPolling(cmdId, childCmdIds);
        CinderBroker cinderBroker = getCinderBroker();
        if (!cinderBroker.isDiskExist(getDiskId())) {
            // Disk has been deleted successfully
            getCommand().setCommandStatus(CommandStatus.SUCCEEDED);
            return;
        }

        ImageStatus imageStatus = cinderBroker.getDiskStatus(getDiskId());
        DiskImage disk = getDisk();
        if (imageStatus != null && imageStatus != disk.getImageStatus()) {
            switch (imageStatus) {
                case ILLEGAL:
                    getCommand().getParameters().setShouldBeLogged(true);
                    getCommand().setCommandStatus(CommandStatus.FAILED);
                    break;
            }
        }
    }

    @Override
    public void onFailed(Guid cmdId, List<Guid> childCmdIds) {
        super.onFailed(cmdId, childCmdIds);
        log.error("Failed deleting disk from Cinder. ID: {}", getDiskId());
        if (getCommand().getParameters().getShouldBeLogged()) {
            new AuditLogDirector().log(getCommand(), AuditLogType.USER_FINISHED_FAILED_REMOVE_DISK);
        }
        ImagesHandler.updateImageStatus(getDiskId(), ImageStatus.ILLEGAL);
        getCommand().endAction();
        CommandCoordinatorUtil.removeAllCommandsInHierarchy(cmdId);
    }

    @Override
    public void onSucceeded(Guid cmdId, List<Guid> childCmdIds) {
        super.onSucceeded(cmdId, childCmdIds);
        log.info("Disk has been successfully deleted from Cinder. ID: {}", getDiskId());
        if (getCommand().getParameters().getShouldBeLogged()) {
            new AuditLogDirector().log(getCommand(), AuditLogType.USER_FINISHED_REMOVE_DISK);
        }
        getCommand().removeDiskFromDb();
        getCommand().endAction();
        CommandCoordinatorUtil.removeAllCommandsInHierarchy(cmdId);
    }

    @Override
    protected Guid getDiskId() {
        return getCommand().getParameters().getDiskId();
    }

    @Override
    protected CinderDisk getDisk() {
        if (disk == null) {
            disk = (CinderDisk) getCommand().getDiskDao().get(getDiskId());
        }
        return disk;
    }

    @Override
    protected CinderBroker getCinderBroker() {
        return getCommand().getCinderBroker();
    }
}
