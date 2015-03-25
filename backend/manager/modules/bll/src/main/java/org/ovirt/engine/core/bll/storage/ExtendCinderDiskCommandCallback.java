package org.ovirt.engine.core.bll.storage;

import org.ovirt.engine.core.bll.tasks.CommandCoordinatorUtil;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.action.UpdateVmDiskParameters;
import org.ovirt.engine.core.common.businessentities.storage.CinderDisk;
import org.ovirt.engine.core.common.businessentities.storage.DiskImage;
import org.ovirt.engine.core.common.businessentities.storage.ImageStatus;
import org.ovirt.engine.core.compat.CommandStatus;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.AuditLogDirector;

import java.util.List;

public class ExtendCinderDiskCommandCallback extends AbstractCinderDiskCommandCallback<ExtendCinderDiskCommand<UpdateVmDiskParameters>> {

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

        log.error("Failed extending disk. ID: {}", getDiskId());
        updateAuditLog(AuditLogType.USER_EXTEND_DISK_SIZE_FAILURE, getCommand().getNewDiskSizeInGB());

        getCommand().endAction();
        CommandCoordinatorUtil.removeAllCommandsInHierarchy(cmdId);
    }

    @Override
    public void onSucceeded(Guid cmdId, List<Guid> childCmdIds) {
        super.onSucceeded(cmdId, childCmdIds);

        getCommand().performDiskUpdate();
        log.error("Disk has been successfully extended. ID: {}", getDiskId());
        updateAuditLog(AuditLogType.USER_EXTEND_DISK_SIZE_SUCCESS, getCommand().getNewDiskSizeInGB());

        getCommand().endAction();
        CommandCoordinatorUtil.removeAllCommandsInHierarchy(cmdId);
    }

    private void updateAuditLog(AuditLogType auditLogType, Long imageSizeInGigabytes) {
        getCommand().addCustomValue("DiskAlias", getDisk().getDiskAlias());
        getCommand().addCustomValue("NewSize", String.valueOf(imageSizeInGigabytes));
        new AuditLogDirector().log(getCommand(), auditLogType);
    }

    @Override
    protected Guid getDiskId() {
        return getCommand().getParameters().getDiskId();
    }

    @Override
    protected CinderDisk getDisk() {
        if (disk == null) {
            disk = (CinderDisk) getCommand().getDiskDao().get(getDiskId());
        }
        return disk;
    }

    @Override
    protected CinderBroker getCinderBroker() {
        return getCommand().getCinderBroker();
    }
}
