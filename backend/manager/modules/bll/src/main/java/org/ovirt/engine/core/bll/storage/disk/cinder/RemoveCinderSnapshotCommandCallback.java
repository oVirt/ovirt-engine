package org.ovirt.engine.core.bll.storage.disk.cinder;

import java.util.List;

import javax.enterprise.inject.Typed;
import javax.inject.Inject;

import org.ovirt.engine.core.bll.CommandBase;
import org.ovirt.engine.core.bll.ConcurrentChildCommandsExecutionCallback;
import org.ovirt.engine.core.common.action.ImagesContainterParametersBase;
import org.ovirt.engine.core.common.businessentities.storage.DiskImage;
import org.ovirt.engine.core.common.businessentities.storage.ImageStatus;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.backendcompat.CommandExecutionStatus;
import org.ovirt.engine.core.dao.DiskImageDao;

@Typed(RemoveCinderSnapshotCommandCallback.class)
public class RemoveCinderSnapshotCommandCallback extends ConcurrentChildCommandsExecutionCallback {

    @Inject
    private DiskImageDao diskImageDao;

    @Override
    protected void childCommandsExecutionEnded(CommandBase<?> command,
            boolean anyFailed,
            List<Guid> childCmdIds,
            CommandExecutionStatus status,
            int completedChildren) {

        RemoveCinderSnapshotDiskCommand<ImagesContainterParametersBase> removeCinderSnapshotDiskCommand =
                (RemoveCinderSnapshotDiskCommand<ImagesContainterParametersBase>) command;
        ImagesContainterParametersBase parameters = removeCinderSnapshotDiskCommand.getParameters();
        Guid diskId = parameters.getDestinationImageId();
        if (!removeCinderSnapshotDiskCommand.getCinderBroker().isSnapshotExist(diskId)) {
            // Cinder snapshot has been deleted successfully
            setCommandEndStatus(command, false, status, childCmdIds);
            return;
        }
        ImageStatus imageStatus = removeCinderSnapshotDiskCommand.getCinderBroker().getSnapshotStatus(diskId);
        DiskImage disk = diskImageDao.getSnapshotById(diskId);
        if (imageStatus != null && imageStatus != disk.getImageStatus()) {
            switch (imageStatus) {
            case ILLEGAL:
                setCommandEndStatus(command, true, status, childCmdIds);
                break;
            }
        }
    }
}
