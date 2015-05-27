package org.ovirt.engine.core.bll.storage;

import java.util.List;

import org.ovirt.engine.core.bll.RemoveCinderSnapshotDiskCommand;
import org.ovirt.engine.core.bll.tasks.CommandCoordinatorUtil;
import org.ovirt.engine.core.common.action.ImagesContainterParametersBase;
import org.ovirt.engine.core.common.businessentities.storage.CinderDisk;
import org.ovirt.engine.core.common.businessentities.storage.DiskImage;
import org.ovirt.engine.core.common.businessentities.storage.ImageStatus;
import org.ovirt.engine.core.compat.CommandStatus;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;
import org.ovirt.engine.core.dao.BaseDiskDao;
import org.ovirt.engine.core.dao.DiskDao;
import org.ovirt.engine.core.dao.DiskImageDAO;
import org.ovirt.engine.core.dao.ImageDao;

public class RemoveCinderSnapshotCommandCallback extends AbstractCinderDiskCommandCallback<RemoveCinderSnapshotDiskCommand<ImagesContainterParametersBase>> {
    @Override
    public void doPolling(Guid cmdId, List<Guid> childCmdIds) {
        super.doPolling(cmdId, childCmdIds);

        if (!getCinderBroker().isSnapshotExist(getDiskId())) {
            // Cinder snapshot has been deleted successfully
            getCommand().setCommandStatus(CommandStatus.SUCCEEDED);
            return;
        }
        ImageStatus imageStatus = getCinderBroker().getSnapshotStatus(getDiskId());
        DiskImage disk = getDisk();
        if (imageStatus != null && imageStatus != disk.getImageStatus()) {
            switch (imageStatus) {
            case ILLEGAL:
                getCommand().setCommandStatus(CommandStatus.FAILED);
                break;
            }
        }
    }

    @Override
    public void onFailed(Guid cmdId, List<Guid> childCmdIds) {
        super.onFailed(cmdId, childCmdIds);

        // If the cinder disk has a snapshot and it is not a part of a template.
        if ((!getDisk().getParentId().equals(Guid.Empty))
                && (!getDisk().getParentId().equals(getDisk().getImageTemplateId()))) {
            DiskImage previousSnapshot = getDiskImageDAO().getSnapshotById(getDisk().getParentId());
            previousSnapshot.setActive(true);
            getImageDao().update(previousSnapshot.getImage());
        }
        getCommand().getParameters().setTaskGroupSuccess(false);
        onFinish(cmdId);
    }

    @Override
    public void onSucceeded(Guid cmdId, List<Guid> childCmdIds) {
        super.onSucceeded(cmdId, childCmdIds);
        onFinish(cmdId);
    }

    private void onFinish(Guid cmdId) {
        getCommand().endAction();
        CommandCoordinatorUtil.removeAllCommandsInHierarchy(cmdId);
    }

    @Override
    protected Guid getDiskId() {
        return getCommand().getParameters().getDestinationImageId();
    }

    @Override
    protected CinderDisk getDisk() {
        if (disk == null) {
            disk = (CinderDisk) getDiskImageDAO().get(getCommand().getParameters().getDestinationImageId());
        }
        return disk;
    }

    protected DiskDao getDiskDao() {
        return DbFacade.getInstance().getDiskDao();
    }

    protected ImageDao getImageDao() {
        return DbFacade.getInstance().getImageDao();
    }

    protected BaseDiskDao getBaseDiskDao() {
        return DbFacade.getInstance().getBaseDiskDao();
    }

    protected DiskImageDAO getDiskImageDAO() {
        return DbFacade.getInstance().getDiskImageDao();
    }

    @Override
    protected CinderBroker getCinderBroker() {
        return getCommand().getCinderBroker();
    }
}
