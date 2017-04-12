package org.ovirt.engine.core.bll.storage.disk.cinder;

import java.util.List;

import javax.enterprise.inject.Typed;
import javax.inject.Inject;

import org.ovirt.engine.core.bll.CommandBase;
import org.ovirt.engine.core.bll.ConcurrentChildCommandsExecutionCallback;
import org.ovirt.engine.core.common.action.CreateCinderSnapshotParameters;
import org.ovirt.engine.core.common.businessentities.Snapshot;
import org.ovirt.engine.core.common.businessentities.storage.DiskImage;
import org.ovirt.engine.core.common.businessentities.storage.ImageStatus;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.backendcompat.CommandExecutionStatus;
import org.ovirt.engine.core.dao.DiskImageDao;

@Typed(CreateCinderSnapshotCommandCallback.class)
public class CreateCinderSnapshotCommandCallback extends ConcurrentChildCommandsExecutionCallback {

    @Inject
    private DiskImageDao diskImageDao;

    @Override
    protected void childCommandsExecutionEnded(CommandBase<?> command,
            boolean anyFailed,
            List<Guid> childCmdIds,
            CommandExecutionStatus status,
            int completedChildren) {

        CreateCinderSnapshotCommand<CreateCinderSnapshotParameters> createCinderSnapshotCommand =
                (CreateCinderSnapshotCommand<CreateCinderSnapshotParameters>) command;
        CreateCinderSnapshotParameters parameters = createCinderSnapshotCommand.getParameters();
        Guid diskId = parameters.getDestinationImageId();
        ImageStatus imageStatus;
        if (parameters.getSnapshotType().equals(Snapshot.SnapshotType.STATELESS)) {
            imageStatus = createCinderSnapshotCommand.getCinderBroker().getDiskStatus(diskId);
        } else {
            imageStatus = createCinderSnapshotCommand.getCinderBroker().getSnapshotStatus(diskId);
        }

        DiskImage disk = diskImageDao.getSnapshotById(diskId);
        if (imageStatus != null && imageStatus != disk.getImageStatus()) {
            switch (imageStatus) {
            case OK:
                setCommandEndStatus(command, false, status, childCmdIds);
                break;
            case ILLEGAL:
                setCommandEndStatus(command, true, status, childCmdIds);
                break;
            }
        }
    }
}
