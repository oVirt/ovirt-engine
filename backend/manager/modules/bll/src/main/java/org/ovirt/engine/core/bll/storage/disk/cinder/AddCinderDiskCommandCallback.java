package org.ovirt.engine.core.bll.storage.disk.cinder;

import java.util.List;

import javax.enterprise.inject.Typed;
import javax.inject.Inject;

import org.ovirt.engine.core.bll.CommandBase;
import org.ovirt.engine.core.bll.ConcurrentChildCommandsExecutionCallback;
import org.ovirt.engine.core.common.action.AddDiskParameters;
import org.ovirt.engine.core.common.businessentities.storage.CinderDisk;
import org.ovirt.engine.core.common.businessentities.storage.DiskImage;
import org.ovirt.engine.core.common.businessentities.storage.ImageStatus;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.backendcompat.CommandExecutionStatus;
import org.ovirt.engine.core.dao.DiskDao;

@Typed(AddCinderDiskCommandCallback.class)
public class AddCinderDiskCommandCallback extends ConcurrentChildCommandsExecutionCallback {

    @Inject
    private DiskDao diskDao;

    @Override
    protected void childCommandsExecutionEnded(CommandBase<?> command,
            boolean anyFailed,
            List<Guid> childCmdIds,
            CommandExecutionStatus status,
            int completedChildren) {

        AddCinderDiskCommand<AddDiskParameters> addCinderDiskCommand =
                (AddCinderDiskCommand<AddDiskParameters>) command;
        Guid diskId = getDiskId(addCinderDiskCommand);
        ImageStatus imageStatus = addCinderDiskCommand.getCinderBroker().getDiskStatus(diskId);
        DiskImage disk = (CinderDisk) diskDao.get(diskId);
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

    protected Guid getDiskId(AddCinderDiskCommand<AddDiskParameters> addCinderDiskCommand) {
        return addCinderDiskCommand.getParameters().getDiskInfo().getId();
    }

}
