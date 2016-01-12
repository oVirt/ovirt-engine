package org.ovirt.engine.core.bll.storage.disk.cinder;

import java.util.List;

import org.ovirt.engine.core.bll.storage.disk.image.BaseImagesCommand;
import org.ovirt.engine.core.common.action.ImagesContainterParametersBase;
import org.ovirt.engine.core.common.businessentities.storage.CinderDisk;
import org.ovirt.engine.core.common.businessentities.storage.DiskImage;
import org.ovirt.engine.core.common.businessentities.storage.ImageStatus;
import org.ovirt.engine.core.compat.CommandStatus;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;
import org.ovirt.engine.core.dao.DiskDao;

public class CloneSingleCinderDiskCommandCallback<T extends BaseImagesCommand<? extends ImagesContainterParametersBase>> extends AbstractCinderDiskCommandCallback<T> {

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
        getCommand().getParameters().setTaskGroupSuccess(false);
        getCommand().endAction();
    }

    @Override
    public void onSucceeded(Guid cmdId, List<Guid> childCmdIds) {
        super.onSucceeded(cmdId, childCmdIds);
        getCommand().endAction();
    }

    @Override
    protected Guid getDiskId() {
        return getCommand().getParameters().getDestinationImageId();
    }

    @Override
    protected CinderDisk getDisk() {
        if (disk == null) {
            disk = (CinderDisk) getDiskDao().get(getCommand().getParameters().getContainerId());
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
