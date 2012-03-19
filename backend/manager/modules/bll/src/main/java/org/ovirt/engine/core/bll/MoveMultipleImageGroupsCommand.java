package org.ovirt.engine.core.bll;

import java.util.List;

import org.ovirt.engine.core.common.action.MoveMultipleImageGroupsParameters;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.asynctasks.AsyncTaskCreationInfo;
import org.ovirt.engine.core.common.asynctasks.AsyncTaskParameters;
import org.ovirt.engine.core.common.asynctasks.AsyncTaskType;
import org.ovirt.engine.core.common.businessentities.AsyncTaskResultEnum;
import org.ovirt.engine.core.common.businessentities.AsyncTaskStatusEnum;
import org.ovirt.engine.core.common.businessentities.DiskImage;
import org.ovirt.engine.core.common.businessentities.ImageStatus;
import org.ovirt.engine.core.common.businessentities.async_tasks;
import org.ovirt.engine.core.common.businessentities.image_storage_domain_map;
import org.ovirt.engine.core.common.vdscommands.MoveMultipleImageGroupsVDSCommandParameters;
import org.ovirt.engine.core.common.vdscommands.VDSCommandType;
import org.ovirt.engine.core.common.vdscommands.VDSReturnValue;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;

public class MoveMultipleImageGroupsCommand<T extends MoveMultipleImageGroupsParameters> extends BaseImagesCommand<T> {
    public MoveMultipleImageGroupsCommand(T parameters) {
        super(parameters);
    }

    @Override
    protected Guid getImageContainerId() {
        return getParameters() != null ? getParameters().getContainerId() : super.getImageContainerId();
    }

    @Override
    protected void executeCommand() {
        // lock images
        for (DiskImage disk : getParameters().getImagesList()) {
            SetImageStatus(disk, ImageStatus.LOCKED);
        }
        VDSReturnValue vdsReturnValue = performImageVdsmOperation();
        if (vdsReturnValue.getSucceeded()) {
            AsyncTaskCreationInfo taskCreationInfo = vdsReturnValue.getCreationInfo();
            getReturnValue().getInternalTaskIdList().add(
                    CreateTask(taskCreationInfo, getParameters().getParentCommand()));
            setSucceeded(true);
        }
    }

    @Override
    protected Guid ConcreteCreateTask(AsyncTaskCreationInfo asyncTaskCreationInfo, VdcActionType parentCommand) {
        AsyncTaskParameters p = new AsyncTaskParameters(asyncTaskCreationInfo, new async_tasks(parentCommand,
                AsyncTaskResultEnum.success, AsyncTaskStatusEnum.running, asyncTaskCreationInfo.getTaskID(),
                getParameters(), asyncTaskCreationInfo.getStepId()));
        p.setEntityId(getParameters().getEntityId());
        Guid ret = AsyncTaskManager.getInstance().CreateTask(AsyncTaskType.moveImage, p, false);

        return ret;
    }

    @Override
    protected void EndWithFailure() {
        // unlock images
        for (DiskImage disk : getParameters().getImagesList()) {
            SetImageStatus(disk, ImageStatus.OK);
        }
        setSucceeded(true);
    }

    @Override
    protected void EndSuccessfully() {
        List<DiskImage> snapshots = null;
        for (DiskImage disk : getParameters().getImagesList()) {
            snapshots = DbFacade
                    .getInstance()
                    .getDiskImageDAO()
                    .getAllSnapshotsForImageGroup(
                            disk.getimage_group_id().getValue());

            // unlock and change to new domain
            for (DiskImage snapshot : snapshots) {
                DbFacade.getInstance().getImageStorageDomainMapDao().remove(snapshot.getId());
                DbFacade.getInstance()
                        .getImageStorageDomainMapDao()
                        .save(new image_storage_domain_map(snapshot.getId(),
                                getParameters().getStorageDomainId()));
                SetImageStatus(snapshot, ImageStatus.OK);
            }
        }
        setSucceeded(true);
    }

    @Override
    protected VDSReturnValue performImageVdsmOperation() {
        VDSReturnValue vdsReturnValue = Backend
        .getInstance()
        .getResourceManager()
        .RunVdsCommand(
                VDSCommandType.MoveMultipleImageGroups,
                new MoveMultipleImageGroupsVDSCommandParameters(getParameters().getImagesList().get(0)
                        .getstorage_pool_id().getValue(), getParameters().getImagesList().get(0)
                        .getstorage_ids().get(0), getParameters().getImagesList(), getParameters()
                        .getStorageDomainId(), getParameters().getContainerId()));
        return vdsReturnValue;

    }
}
