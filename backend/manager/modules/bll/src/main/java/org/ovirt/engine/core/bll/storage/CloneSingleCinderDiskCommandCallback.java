package org.ovirt.engine.core.bll.storage;

import org.ovirt.engine.core.bll.ImagesHandler;
import org.ovirt.engine.core.bll.tasks.CommandCoordinatorUtil;
import org.ovirt.engine.core.common.action.ImagesContainterParametersBase;
import org.ovirt.engine.core.common.businessentities.storage.CinderDisk;
import org.ovirt.engine.core.common.businessentities.storage.DiskImage;
import org.ovirt.engine.core.common.businessentities.storage.ImageStatus;
import org.ovirt.engine.core.compat.CommandStatus;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;
import org.ovirt.engine.core.dao.DiskDao;

import java.util.List;

public class CloneSingleCinderDiskCommandCallback extends AbstractCinderDiskCommandCallback<CloneSingleCinderDiskCommand<ImagesContainterParametersBase>> {

    @Override
    public void doPolling(Guid cmdId, List<Guid> childCmdIds) {
        super.doPolling(cmdId, childCmdIds);

        ImageStatus imageStatus = getCinderBroker().getDiskStatus(getDiskId());
        DiskImage disk = getDisk();
        if (imageStatus != null && imageStatus != disk.getImageStatus()) {
            switch (imageStatus) {
            case OK:
                getCommand().setCommandStatus(CommandStatus.SUCCEEDED);
                break;
            case ILLEGAL:
                getCommand().setCommandStatus(CommandStatus.FAILED);
                break;
            }
        }
    }

    @Override
    public void onFailed(Guid cmdId, List<Guid> childCmdIds) {
        super.onFailed(cmdId, childCmdIds);
        ImagesHandler.updateImageStatus(getDiskId(), ImageStatus.ILLEGAL);
        getCommand().getParameters().setTaskGroupSuccess(false);
        onFinish(cmdId);
    }

    @Override
    public void onSucceeded(Guid cmdId, List<Guid> childCmdIds) {
        super.onSucceeded(cmdId, childCmdIds);
        ImagesHandler.updateImageStatus(getDiskId(), ImageStatus.OK);
        onFinish(cmdId);
    }

    private void onFinish(Guid cmdId) {
        ImagesHandler.updateImageStatus(getCommand().getParameters().getImageId(), ImageStatus.OK);
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
            disk = (CinderDisk) getDiskDao().get(getCommand().getParameters().getDestinationImageId());
        }
        return disk;
    }

    protected DiskDao getDiskDao() {
        return DbFacade.getInstance().getDiskDao();
    }

    @Override
    protected CinderBroker getCinderBroker() {
        return getCommand().getCinderBroker();
    }
}
