package org.ovirt.engine.core.bll.storage.disk.cinder;

import java.util.List;

import org.ovirt.engine.core.bll.CommandBase;
import org.ovirt.engine.core.bll.ConcurrentChildCommandsExecutionCallback;
import org.ovirt.engine.core.bll.storage.disk.image.BaseImagesCommand;
import org.ovirt.engine.core.common.action.ImagesContainterParametersBase;
import org.ovirt.engine.core.common.businessentities.storage.CinderDisk;
import org.ovirt.engine.core.common.businessentities.storage.DiskImage;
import org.ovirt.engine.core.common.businessentities.storage.ImageStatus;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.backendcompat.CommandExecutionStatus;

public class CloneSingleCinderDiskCommandCallback extends ConcurrentChildCommandsExecutionCallback {


    @Override
    protected void childCommandsExecutionEnded(CommandBase<?> command,
            boolean anyFailed,
            List<Guid> childCmdIds,
            CommandExecutionStatus status,
            int completedChildren) {

        BaseImagesCommand<ImagesContainterParametersBase> cloneCinderDiskCommand =
                (BaseImagesCommand<ImagesContainterParametersBase>) command;
        ImagesContainterParametersBase parameters = cloneCinderDiskCommand.getParameters();
        ImageStatus imageStatus = cloneCinderDiskCommand.getCinderBroker().getDiskStatus(parameters.getDestinationImageId());
        DiskImage disk = (CinderDisk) command.getDiskDao().get(parameters.getContainerId());
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
