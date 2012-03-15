package org.ovirt.engine.core.bll;

import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.ovirt.engine.core.common.action.MoveOrCopyImageGroupParameters;
import org.ovirt.engine.core.common.action.RemoveImageParameters;
import org.ovirt.engine.core.common.action.VdcActionParametersBase;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.action.VdcReturnValueBase;
import org.ovirt.engine.core.common.asynctasks.AsyncTaskCreationInfo;
import org.ovirt.engine.core.common.asynctasks.AsyncTaskParameters;
import org.ovirt.engine.core.common.asynctasks.AsyncTaskType;
import org.ovirt.engine.core.common.businessentities.AsyncTaskResultEnum;
import org.ovirt.engine.core.common.businessentities.AsyncTaskStatusEnum;
import org.ovirt.engine.core.common.businessentities.DiskImage;
import org.ovirt.engine.core.common.businessentities.ImageOperation;
import org.ovirt.engine.core.common.businessentities.async_tasks;
import org.ovirt.engine.core.common.businessentities.image_storage_domain_map;
import org.ovirt.engine.core.common.vdscommands.CopyImageVDSCommandParameters;
import org.ovirt.engine.core.common.vdscommands.MoveImageGroupVDSCommandParameters;
import org.ovirt.engine.core.common.vdscommands.VDSCommandType;
import org.ovirt.engine.core.common.vdscommands.VDSReturnValue;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;

@InternalCommandAttribute
public class MoveOrCopyImageGroupCommand<T extends MoveOrCopyImageGroupParameters> extends BaseImagesCommand<T> {
    public MoveOrCopyImageGroupCommand(T parameters) {
        super(parameters);
    }

    private DiskImage _diskImage;

    @Override
    protected DiskImage getImage() {
        switch (getActionState()) {
        case END_SUCCESS:
        case END_FAILURE:
            if (_diskImage == null) {
                List<DiskImage> diskImages =
                        DbFacade.getInstance().getDiskImageDAO().getAllSnapshotsForImageGroup(getParameters().getImageGroupID());
                _diskImage = (diskImages.isEmpty()) ? null : diskImages.get(0);
            }

            return _diskImage;

        default:
            return super.getImage();
        }
    }

    @Override
    protected Guid getImageContainerId() {
        return getParameters() != null ? getParameters().getContainerId() : super.getImageContainerId();
    }

    protected ImageOperation getMoveOrCopyImageOperation() {
        return getParameters().getOperation();
    }

    @Override
    protected void executeCommand() {
        LockImage();
        VDSReturnValue vdsReturnValue = null;

        if (getParameters().getUseCopyCollapse()) {
            vdsReturnValue =
                    Backend
                            .getInstance()
                            .getResourceManager()
                            .RunVdsCommand(
                                    VDSCommandType.CopyImage,
                                    new CopyImageVDSCommandParameters(getDiskImage().getstorage_pool_id().getValue(),
                                            getParameters().getSourceDomainId() != null ? getParameters().getSourceDomainId()
                                                    .getValue()
                                                    : getDiskImage().getstorage_ids().get(0),
                                            getParameters()
                                                    .getContainerId(),
                                            getParameters().getImageGroupID(),
                                            getImage()
                                                    .getId(),
                                            getParameters().getDestImageGroupId(),
                                            getParameters().getDestinationImageId(),
                                            StringUtils.defaultString(getImage()
                                                    .getdescription()),
                                            getParameters().getStorageDomainId(),
                                            getParameters()
                                                    .getCopyVolumeType(),
                                            getParameters().getVolumeFormat(),
                                            getParameters()
                                                    .getVolumeType(),
                                            getParameters().getPostZero(),
                                            getParameters()
                                                    .getForceOverride(),
                                            getStoragePool().getcompatibility_version().toString()));
        } else {
            vdsReturnValue = Backend
                    .getInstance()
                    .getResourceManager()
                    .RunVdsCommand(
                            VDSCommandType.MoveImageGroup,
                            new MoveImageGroupVDSCommandParameters(getDiskImage().getstorage_pool_id().getValue(),
                                    getParameters().getSourceDomainId() != null ? getParameters().getSourceDomainId()
                                            .getValue() : getDiskImage().getstorage_ids().get(0), getDiskImage()
                                            .getimage_group_id().getValue(), getParameters().getStorageDomainId(),
                                    getParameters().getContainerId(), getParameters().getOperation(), getParameters()
                                            .getPostZero(), getParameters().getForceOverride(), getStoragePool()
                                            .getcompatibility_version().toString()));
        }

        if (vdsReturnValue.getSucceeded()) {
            AsyncTaskCreationInfo taskCreationInfo = vdsReturnValue.getCreationInfo();
            getReturnValue().getInternalTaskIdList().add(
                    CreateTask(taskCreationInfo, getParameters().getParentCommand()));

            // change storage domain in db only if object moved
            if (getParameters().getOperation() == ImageOperation.Move
                    || getParameters().getParentCommand() == VdcActionType.ImportVm
                    || getParameters().getParentCommand() == VdcActionType.ImportVmTemplate) {
                List<DiskImage> snapshots = getDiskImageDao()
                        .getAllSnapshotsForImageGroup(getParameters().getImageGroupID());
                for (DiskImage snapshot : snapshots) {
                    DbFacade.getInstance()
                            .getStorageDomainDAO()
                            .removeImageStorageDomainMap(new image_storage_domain_map(snapshot.getId(),
                                    snapshot.getstorage_ids().get(0)));
                    DbFacade.getInstance()
                            .getStorageDomainDAO()
                            .addImageStorageDomainMap(new image_storage_domain_map(snapshot.getId(),
                                    getParameters().getStorageDomainId()));
                }
            } else if (getParameters().getAddImageDomainMapping()) {
                DbFacade.getInstance().getStorageDomainDAO().addImageStorageDomainMap(
                        new image_storage_domain_map(getParameters().getImageId(), getParameters()
                                .getStorageDomainId()));
            }

            setSucceeded(true);
        }
    }

    @Override
    protected Guid ConcreteCreateTask(AsyncTaskCreationInfo asyncTaskCreationInfo, VdcActionType parentCommand) {
        VdcActionParametersBase commandParams = getParametersForTask(parentCommand, getParameters());
        AsyncTaskParameters p = new AsyncTaskParameters(asyncTaskCreationInfo, new async_tasks(parentCommand,
                AsyncTaskResultEnum.success, AsyncTaskStatusEnum.running, asyncTaskCreationInfo.getTaskID(),
                commandParams, asyncTaskCreationInfo.getStepId()));
        p.setEntityId(getParameters().getEntityId());
        Guid ret = AsyncTaskManager.getInstance().CreateTask(AsyncTaskType.moveImage, p, false);

        return ret;
    }

    @Override
    protected void EndWithFailure() {
        if (getMoveOrCopyImageOperation() == ImageOperation.Copy) {
            UnLockImage();
            if (getParameters().getAddImageDomainMapping()) {
                // remove iamge-storage mapping
                DbFacade.getInstance().getStorageDomainDAO().removeImageStorageDomainMap(
                        new image_storage_domain_map(getParameters().getImageId(), getParameters()
                                .getStorageDomainId()));
            }
            RevertTasks();
        }

        else {
            MarkImageAsIllegal();
        }

        setSucceeded(true);
    }

    @Override
    protected void RevertTasks() {
        Guid destImageId = getParameters().getDestinationImageId();
        RemoveImageParameters removeImageParams =
                new RemoveImageParameters(destImageId, getParameters().getContainerId());
        removeImageParams.setParentParemeters(getParameters());
        removeImageParams.setParentCommand(VdcActionType.MoveOrCopyImageGroup);
        removeImageParams.setEntityId(getDestinationImageId());
        // Setting the image as the monitored entity, so there will not be dependency
        VdcReturnValueBase returnValue =
                checkAndPerformRollbackUsingCommand(VdcActionType.RemoveImage, removeImageParams);
        if (returnValue.getSucceeded()) {
            // Starting to monitor the the tasks - RemoveImage is an internal command
            // which adds the taskId on the internal task ID list
            startPollingAsyncTasks(returnValue.getInternalTaskIdList());
        }
    }

    @Override
    protected boolean canPerformRollbackUsingCommand(VdcActionType commandType, VdcActionParametersBase params) {
        return DbFacade.getInstance().getDiskImageDAO().get(getParameters().getDestinationImageId()) != null;
    }

}
