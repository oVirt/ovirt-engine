package org.ovirt.engine.core.bll.storage.disk.cinder;

import java.util.List;

import javax.enterprise.inject.Typed;
import javax.inject.Inject;

import org.ovirt.engine.core.bll.CommandBase;
import org.ovirt.engine.core.bll.ConcurrentChildCommandsExecutionCallback;
import org.ovirt.engine.core.bll.storage.disk.image.ImagesHandler;
import org.ovirt.engine.core.bll.tasks.CommandCoordinatorUtil;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.action.UpdateDiskParameters;
import org.ovirt.engine.core.common.businessentities.storage.CinderDisk;
import org.ovirt.engine.core.common.businessentities.storage.ImageStatus;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.backendcompat.CommandExecutionStatus;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.AuditLogDirector;
import org.ovirt.engine.core.dao.DiskDao;

@Typed(ExtendCinderDiskCommandCallback.class)
public class ExtendCinderDiskCommandCallback extends ConcurrentChildCommandsExecutionCallback {

    @Inject
    private CommandCoordinatorUtil commandCoordinatorUtil;

    @Inject
    private DiskDao diskDao;

    @Inject
    private AuditLogDirector auditLogDirector;

    @Inject
    private ImagesHandler imagesHandler;

    @Override
    protected void childCommandsExecutionEnded(CommandBase<?> command,
            boolean anyFailed,
            List<Guid> childCmdIds,
            CommandExecutionStatus status,
            int completedChildren) {

        ExtendCinderDiskCommand<UpdateDiskParameters> extendCinderDiskCommand =
                (ExtendCinderDiskCommand<UpdateDiskParameters>) command;
        ImageStatus imageStatus = extendCinderDiskCommand.getCinderBroker()
                .getDiskStatus(getDiskId(extendCinderDiskCommand));
        if (imageStatus != null && imageStatus != getDisk(extendCinderDiskCommand).getImageStatus()) {
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

    @Override
    public void onFailed(Guid cmdId, List<Guid> childCmdIds) {
        ExtendCinderDiskCommand command = getCommand(cmdId);
        imagesHandler.updateImageStatus(getDiskId(command), ImageStatus.ILLEGAL);
        log.error("Failed extending disk. ID: {}", getDiskId(command));
        updateAuditLog(command, AuditLogType.USER_EXTEND_DISK_SIZE_FAILURE, command.getNewDiskSizeInGB());

        super.onFailed(cmdId, childCmdIds);
    }

    @Override
    public void onSucceeded(Guid cmdId, List<Guid> childCmdIds) {
        ExtendCinderDiskCommand command = getCommand(cmdId);
        command.performDiskUpdate();
        log.error("Disk has been successfully extended. ID: {}", getDiskId(command));
        updateAuditLog(command, AuditLogType.USER_EXTEND_DISK_SIZE_SUCCESS, command.getNewDiskSizeInGB());

        super.onSucceeded(cmdId, childCmdIds);
    }

    private void updateAuditLog(ExtendCinderDiskCommand command, AuditLogType auditLogType, Long imageSizeInGigabytes) {
        command.addCustomValue("DiskAlias", getDisk(command).getDiskAlias());
        command.addCustomValue("NewSize", String.valueOf(imageSizeInGigabytes));
        auditLogDirector.log(command, auditLogType);
    }

    protected Guid getDiskId(ExtendCinderDiskCommand<UpdateDiskParameters> command) {
        return command.getParameters().getDiskInfo().getId();
    }

    protected CinderDisk getDisk(ExtendCinderDiskCommand<UpdateDiskParameters> command) {
        return (CinderDisk) diskDao.get(getDiskId(command));
    }

    @Override
    protected ExtendCinderDiskCommand<UpdateDiskParameters> getCommand(Guid cmdId) {
        return commandCoordinatorUtil.retrieveCommand(cmdId);
    }
}
