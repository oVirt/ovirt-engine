package org.ovirt.engine.core.bll;

import java.util.List;

import org.ovirt.engine.core.common.action.RemoveImageParameters;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.asynctasks.AsyncTaskCreationInfo;
import org.ovirt.engine.core.common.asynctasks.AsyncTaskParameters;
import org.ovirt.engine.core.common.asynctasks.AsyncTaskType;
import org.ovirt.engine.core.common.businessentities.AsyncTaskResultEnum;
import org.ovirt.engine.core.common.businessentities.AsyncTaskStatusEnum;
import org.ovirt.engine.core.common.businessentities.DiskImage;
import org.ovirt.engine.core.common.businessentities.async_tasks;
import org.ovirt.engine.core.common.vdscommands.DeleteImageGroupVDSCommandParameters;
import org.ovirt.engine.core.common.vdscommands.VDSCommandType;
import org.ovirt.engine.core.common.vdscommands.VDSReturnValue;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.LogCompat;
import org.ovirt.engine.core.compat.LogFactoryCompat;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;

/**
 * This command responcible to removing image, contains all created snapshots.
 */
@InternalCommandAttribute
public class RemoveImageCommand<T extends RemoveImageParameters> extends BaseImagesCommand<T> {
    public RemoveImageCommand(T parameters) {
        super(parameters);
        setDiskImage(((getParameters().getDiskImage()) != null) ? getParameters().getDiskImage() : getDiskImage());
        if (getStoragePoolId() == null
                || (getStoragePoolId() != null && getStoragePoolId().getValue().equals(Guid.Empty))) {
            setStoragePoolId(getDiskImage() != null && getDiskImage().getstorage_pool_id() != null ? getDiskImage()
                    .getstorage_pool_id().getValue() : Guid.Empty);
        }
    }

    @Override
    protected Guid getImageContainerId() {
        switch (getActionState()) {
        case EXECUTE:
            return getParameters().getContainerId();

        default:
            return super.getImageContainerId();
        }
    }

    @Override
    protected void executeCommand() {
        if (getDiskImage() != null) {
            VDSReturnValue vdsReturnValue = Backend
                    .getInstance()
                    .getResourceManager()
                    .RunVdsCommand(
                            VDSCommandType.DeleteImageGroup,
                            new DeleteImageGroupVDSCommandParameters(getDiskImage().getstorage_pool_id().getValue(),
                                    getDiskImage().getstorage_id().getValue(), getDiskImage().getimage_group_id()
                                            .getValue(), getDiskImage().getwipe_after_delete(), getParameters()
                                            .getForceDelete(), getStoragePool().getcompatibility_version().toString()));

            if (vdsReturnValue.getSucceeded()) {
                getReturnValue().getInternalTaskIdList().add(
                        CreateTask(vdsReturnValue.getCreationInfo(), getParameters().getParentCommand()));

                if (getParameters().getParentCommand() != VdcActionType.RemoveDisksFromVm
                        && getParameters().getParentCommand() != VdcActionType.RemoveVmFromImportExport
                        && getParameters().getParentCommand() != VdcActionType.RemoveVmTemplateFromImportExport) {
                    RemoveImageFromDB();
                }
            } else {
                return;
            }
        } else {
            log.warn("RemoveImageCommand::ExecuteCommand: DiskImage is null, nothing to remove");
        }
        setSucceeded(true);
    }

    @Override
    protected Guid ConcreteCreateTask(AsyncTaskCreationInfo asyncTaskCreationInfo, VdcActionType parentCommand) {
        AsyncTaskParameters p = new AsyncTaskParameters(asyncTaskCreationInfo, new async_tasks(parentCommand,
                AsyncTaskResultEnum.success, AsyncTaskStatusEnum.running, asyncTaskCreationInfo.getTaskID(),
                getParametersForTask(parentCommand, getParameters())));
        p.setEntityId(getParameters().getEntityId());
        Guid ret = AsyncTaskManager.getInstance().CreateTask(AsyncTaskType.deleteImage, p, false);

        return ret;
    }

    private void RemoveImageFromDB() {
        DiskImage diskImage = getDiskImage();
        if (diskImage != null) {
            DbFacade.getInstance().getDiskImageDynamicDAO().remove(diskImage.getId());
            Guid imageTemplate = diskImage.getit_guid();
            Guid currentGuid = diskImage.getId();
            // next 'while' statement removes snapshots from DB only (the
            // 'DeleteImageGroup'
            // VDS Command should take care of removing all the snapshots from
            // the storage).
            while (!imageTemplate.equals(currentGuid) && !currentGuid.equals(Guid.Empty)) {
                RemoveChildren(currentGuid);

                // DiskImage image = IrsBroker.getImageInfo(currentGuid);
                DiskImage image = DbFacade.getInstance().getDiskImageDAO().getSnapshotById(currentGuid);
                if (image != null) {
                    RemoveSnapshot(image);
                    currentGuid = image.getParentId();
                }

                else {
                    currentGuid = Guid.Empty;
                    log.warnFormat(
                            "RemoveImageCommand::RemoveImageFromDB: 'image' (snapshot of image '{0}') is null, cannot remove it.",
                            diskImage.getId());
                }
            }

            List<DiskImage> imagesForDisk =
                    DbFacade.getInstance()
                            .getDiskImageDAO()
                            .getAllSnapshotsForImageGroup(diskImage.getimage_group_id());
            if (imagesForDisk == null || imagesForDisk.isEmpty()) {
                DbFacade.getInstance().getDiskDao().remove(diskImage.getimage_group_id());
            }
        } else {
            log.warn("RemoveImageCommand::RemoveImageFromDB: DiskImage is null, nothing to remove.");
        }
    }

    @Override
    protected void EndSuccessfully() {
        if (getParameters() != null && getParameters().getParentCommand() == VdcActionType.RemoveDisksFromVm) {
            RemoveImageFromDB();
        }
        setSucceeded(true);
    }

    @Override
    protected void EndWithFailure() {
        if (getParameters() != null && getParameters().getParentCommand() == VdcActionType.RemoveDisksFromVm) {
            RemoveImageFromDB();
        }
        setSucceeded(true);
    }

    private static LogCompat log = LogFactoryCompat.getLog(RemoveImageCommand.class);
}
