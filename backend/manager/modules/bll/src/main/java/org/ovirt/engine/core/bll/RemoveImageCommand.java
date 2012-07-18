package org.ovirt.engine.core.bll;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.ovirt.engine.core.common.VdcObjectType;
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
import org.ovirt.engine.core.dao.VmDeviceDAO;
import org.ovirt.engine.core.utils.transaction.TransactionMethod;
import org.ovirt.engine.core.utils.transaction.TransactionSupport;

/**
 * This command responsible to removing image, contains all created snapshots.
 */
@SuppressWarnings("serial")
@InternalCommandAttribute
@NonTransactiveCommandAttribute
public class RemoveImageCommand<T extends RemoveImageParameters> extends BaseImagesCommand<T> {

    public RemoveImageCommand(T parameters) {
        super(parameters);
        initImage();
        initStoragePoolId();
        initStorageDomainId();
    }

    protected void initImage() {
        setDiskImage(((getParameters().getDiskImage()) != null) ? getParameters().getDiskImage() : getImage());
    }

    protected void initStoragePoolId() {
        if (getStoragePoolId() == null || Guid.Empty.equals(getStoragePoolId())) {
            setStoragePoolId(getDiskImage() != null && getDiskImage().getstorage_pool_id() != null ? getDiskImage()
                    .getstorage_pool_id().getValue() : Guid.Empty);
        }
    }

    protected void initStorageDomainId() {
        if ((getParameters().getStorageDomainId() == null || Guid.Empty.equals(getParameters().getStorageDomainId()))
                && getDiskImage() != null) {
            setStorageDomainId(getDiskImage().getstorage_ids().get(0));
        }
    }

    @Override
    protected void executeCommand() {
        if (getDiskImage() != null) {
            VDSReturnValue vdsReturnValue = performImageVdsmOperation();
            getReturnValue().getInternalTaskIdList().add(
                    CreateTask(vdsReturnValue.getCreationInfo(),
                            getParameters().getParentCommand(),
                            VdcObjectType.Storage,
                            getParameters().getStorageDomainId()));

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
    protected SPMAsyncTask ConcreteCreateTask(AsyncTaskCreationInfo asyncTaskCreationInfo, VdcActionType parentCommand) {
        AsyncTaskParameters p = new AsyncTaskParameters(asyncTaskCreationInfo, new async_tasks(parentCommand,
                AsyncTaskResultEnum.success, AsyncTaskStatusEnum.running, asyncTaskCreationInfo.getTaskID(),
                getParametersForTask(parentCommand, getParameters()), asyncTaskCreationInfo.getStepId(),
                getCommandId()));
        p.setEntityId(getParameters().getEntityId());
        return AsyncTaskManager.getInstance().CreateTask(AsyncTaskType.deleteImage, p);
    }

    private void removeImageFromDB() {
        TransactionSupport.executeInScope(TransactionScopeOption.Required,
                new TransactionMethod<Object>() {
                    @Override
                    public Object runInTransaction() {
                        DiskImage diskImage = getDiskImage();
                        if (diskImage != null) {
                            getDiskImageDynamicDAO().remove(diskImage.getImageId());
                            Guid imageTemplate = diskImage.getit_guid();
                            Guid currentGuid = diskImage.getImageId();
                            // next 'while' statement removes snapshots from DB only (the
                            // 'DeleteImageGroup'
                            // VDS Command should take care of removing all the snapshots from
                            // the storage).
                            while (!imageTemplate.equals(currentGuid) && !currentGuid.equals(Guid.Empty)) {
                                RemoveChildren(currentGuid);

                                DiskImage image = getDiskImageDao().getSnapshotById(currentGuid);
                                if (image != null) {
                                    RemoveSnapshot(image);
                                    currentGuid = image.getParentId();
                                }

                                else {
                                    currentGuid = Guid.Empty;
                                    log.warnFormat(
                                            "RemoveImageCommand::RemoveImageFromDB: 'image' (snapshot of image '{0}') is null, cannot remove it.",
                                            diskImage.getImageId());
                                }
                            }

                            getBaseDiskDao().remove(diskImage.getId());
                            getVmDeviceDAO().remove(new VmDeviceId(diskImage.getId(), null));
                        } else {
                            log.warn("RemoveImageCommand::RemoveImageFromDB: DiskImage is null, nothing to remove.");
                        }
                        return null;
                    }
                });
    }

    private void GetImageChildren(Guid snapshot, List<Guid> children) {
        List<Guid> list = new ArrayList<Guid>();
        for (DiskImage image : getDiskImageDAO().getAllSnapshotsForParent(snapshot)) {
            list.add(image.getImageId());
        }
        children.addAll(list);
        for (Guid snapshotId : list) {
            GetImageChildren(snapshotId, children);
        }
    }

    private void RemoveChildren(Guid snapshot) {
        List<Guid> children = new ArrayList<Guid>();
        GetImageChildren(snapshot, children);
        Collections.reverse(children);
        for (Guid child : children) {
            RemoveSnapshot(getDiskImageDao().getSnapshotById(child));
        }
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
            getImageStorageDomainMapDao().remove(
                    new image_storage_domain_map_id(getParameters().getImageId(),
                            getParameters().getStorageDomainId()));
            UnLockImage();
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
                            getStorageDomainId().getValue(), getDiskImage().getId()
                                    .getValue(), getDiskImage().isWipeAfterDelete(), getParameters()
                                    .getForceDelete(), getStoragePool().getcompatibility_version().toString()));
            return returnValue;
        } catch (VdcBLLException e) {
            if (isShouldBeLocked) {
                UnLockImage();
            }
            throw e;
        }
    }

    protected VmDeviceDAO getVmDeviceDAO() {
        return getDbFacade().getVmDeviceDAO();
    }
}
