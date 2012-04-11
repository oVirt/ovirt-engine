package org.ovirt.engine.core.bll;

import org.ovirt.engine.core.common.action.RemoveImageParameters;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.asynctasks.AsyncTaskCreationInfo;
import org.ovirt.engine.core.common.asynctasks.AsyncTaskParameters;
import org.ovirt.engine.core.common.asynctasks.AsyncTaskType;
import org.ovirt.engine.core.common.businessentities.AsyncTaskResultEnum;
import org.ovirt.engine.core.common.businessentities.AsyncTaskStatusEnum;
import org.ovirt.engine.core.common.businessentities.DiskImage;
import org.ovirt.engine.core.common.businessentities.VmDeviceId;
import org.ovirt.engine.core.common.businessentities.async_tasks;
import org.ovirt.engine.core.common.businessentities.image_storage_domain_map_id;
import org.ovirt.engine.core.common.errors.VdcBLLException;
import org.ovirt.engine.core.common.vdscommands.DeleteImageGroupVDSCommandParameters;
import org.ovirt.engine.core.common.vdscommands.VDSCommandType;
import org.ovirt.engine.core.common.vdscommands.VDSReturnValue;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.TransactionScopeOption;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;
import org.ovirt.engine.core.utils.transaction.TransactionMethod;
import org.ovirt.engine.core.utils.transaction.TransactionSupport;

/**
 * This command responsible to removing image, contains all created snapshots.
 */
@InternalCommandAttribute
public class RemoveImageCommand<T extends RemoveImageParameters> extends BaseImagesCommand<T> {

    public RemoveImageCommand(T parameters) {
        super(parameters);
        setDiskImage(((getParameters().getDiskImage()) != null) ? getParameters().getDiskImage() : getDiskImage());
        if (getStoragePoolId() == null || Guid.Empty.equals(getStoragePoolId())) {
            setStoragePoolId(getDiskImage() != null && getDiskImage().getstorage_pool_id() != null ? getDiskImage()
                    .getstorage_pool_id().getValue() : Guid.Empty);
        }
        if ((getParameters().getStorageDomainId() == null || Guid.Empty.equals(getParameters().getStorageDomainId()))
                && getDiskImage() != null) {
            setStorageDomainId(getDiskImage().getstorage_ids().get(0));
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
            VDSReturnValue vdsReturnValue = performImageVdsmOperation();
            getReturnValue().getInternalTaskIdList().add(
                        CreateTask(vdsReturnValue.getCreationInfo(), getParameters().getParentCommand()));

            if (getParameters().getParentCommand() != VdcActionType.RemoveVmFromImportExport
                        && getParameters().getParentCommand() != VdcActionType.RemoveVmTemplateFromImportExport
                        && getParameters().getParentCommand() != VdcActionType.RemoveDisk) {
                removeImageFromDB();
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
                getParametersForTask(parentCommand, getParameters()), asyncTaskCreationInfo.getStepId()));
        p.setEntityId(getParameters().getEntityId());
        Guid ret = AsyncTaskManager.getInstance().CreateTask(AsyncTaskType.deleteImage, p, false);

        return ret;
    }

    private void removeImageFromDB() {
        TransactionSupport.executeInScope(TransactionScopeOption.Required,
                new TransactionMethod<Object>() {
                    @Override
                    public Object runInTransaction() {
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

                            getBaseDiskDao().remove(diskImage.getimage_group_id());
                            DbFacade.getInstance()
                                    .getVmDeviceDAO()
                                    .remove(new VmDeviceId(diskImage.getimage_group_id(),
                                            getParameters().getContainerId()));
                        } else {
                            log.warn("RemoveImageCommand::RemoveImageFromDB: DiskImage is null, nothing to remove.");
                        }
                        return null;
                    }
                });
    }

    @Override
    protected void EndSuccessfully() {
        endCommand();
    }

    @Override
    protected void EndWithFailure() {
        endCommand();
    }

    private void endCommand() {
        if (getParameters().getRemoveFromDB()) {
            removeImageFromDB();
        } else {
            DbFacade.getInstance()
                    .getImageStorageDomainMapDao()
                    .remove(
                            new image_storage_domain_map_id(getParameters().getImageId(),
                                    getParameters().getStorageDomainId()));
        }
        setSucceeded(true);
    }

    @Override
    protected VDSReturnValue performImageVdsmOperation() {
        boolean isShouldBeLocked = getParameters().getParentCommand() != VdcActionType.RemoveVmFromImportExport
                && getParameters().getParentCommand() != VdcActionType.RemoveVmTemplateFromImportExport;
        if (isShouldBeLocked) {
            LockImage();
        }
        // Releasing the lock for cases it was set by the parent command. The lock can be released because the image
        // status was already changed to lock.
        freeLock();
        try {
            VDSReturnValue returnValue = runVdsCommand(VDSCommandType.DeleteImageGroup,
                    new DeleteImageGroupVDSCommandParameters(getDiskImage().getstorage_pool_id().getValue(),
                            getStorageDomainId().getValue(), getDiskImage().getimage_group_id()
                                    .getValue(), getDiskImage().getwipe_after_delete(), getParameters()
                                    .getForceDelete(), getStoragePool().getcompatibility_version().toString()));
            return returnValue;
        } catch (VdcBLLException e) {
            if (isShouldBeLocked) {
                UnLockImage();
            }
            throw e;
        }
    }
}
