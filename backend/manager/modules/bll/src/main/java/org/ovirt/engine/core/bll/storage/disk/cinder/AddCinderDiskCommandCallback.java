package org.ovirt.engine.core.bll.storage.disk.cinder;

import java.util.List;

import org.ovirt.engine.core.bll.CommandBase;
import org.ovirt.engine.core.bll.ConcurrentChildCommandsExecutionCallback;
import org.ovirt.engine.core.common.action.AddDiskParameters;
import org.ovirt.engine.core.common.businessentities.storage.CinderDisk;
import org.ovirt.engine.core.common.businessentities.storage.DiskImage;
import org.ovirt.engine.core.common.businessentities.storage.ImageStatus;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.backendcompat.CommandExecutionStatus;

public class AddCinderDiskCommandCallback extends ConcurrentChildCommandsExecutionCallback {

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
        DiskImage disk = (CinderDisk) command.getDiskDao().get(diskId);
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
