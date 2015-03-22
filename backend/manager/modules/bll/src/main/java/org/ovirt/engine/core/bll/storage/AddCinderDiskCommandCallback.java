package org.ovirt.engine.core.bll.storage;

import org.ovirt.engine.core.bll.tasks.CommandCoordinatorUtil;
import org.ovirt.engine.core.common.action.AddDiskParameters;
import org.ovirt.engine.core.common.businessentities.storage.CinderDisk;
import org.ovirt.engine.core.common.businessentities.storage.DiskImage;
import org.ovirt.engine.core.common.businessentities.storage.ImageStatus;
import org.ovirt.engine.core.compat.CommandStatus;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;
import org.ovirt.engine.core.dao.DiskDao;

import java.util.List;

public class AddCinderDiskCommandCallback extends AbstractCinderDiskCommandCallback<AddCinderDiskCommand<AddDiskParameters>> {

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

        log.error("Failed adding disk to Cinder. ID: {}", getDiskId());
        getCommand().endAction();
        CommandCoordinatorUtil.removeAllCommandsInHierarchy(cmdId);
    }

    @Override
    public void onSucceeded(Guid cmdId, List<Guid> childCmdIds) {
        super.onSucceeded(cmdId, childCmdIds);

        log.error("Disk has been successfully added to Cinder. ID: {}", getDiskId());
        getCommand().endAction();
        CommandCoordinatorUtil.removeAllCommandsInHierarchy(cmdId);
    }

    @Override
    protected Guid getDiskId() {
        return getCommand().getParameters().getDiskInfo().getId();
    }

    protected CinderDisk getDisk() {
        if (disk == null) {
            disk = (CinderDisk) getDiskDao().get(getDiskId());
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
