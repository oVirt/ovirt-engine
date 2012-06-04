package org.ovirt.engine.core.bll;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;

import org.ovirt.engine.core.common.action.ImagesActionsParametersBase;
import org.ovirt.engine.core.common.action.ImagesContainterParametersBase;
import org.ovirt.engine.core.common.action.VdcActionParametersBase;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.asynctasks.AsyncTaskCreationInfo;
import org.ovirt.engine.core.common.asynctasks.AsyncTaskParameters;
import org.ovirt.engine.core.common.asynctasks.AsyncTaskType;
import org.ovirt.engine.core.common.businessentities.AsyncTaskResultEnum;
import org.ovirt.engine.core.common.businessentities.AsyncTaskStatusEnum;
import org.ovirt.engine.core.common.businessentities.DiskImage;
import org.ovirt.engine.core.common.businessentities.VolumeFormat;
import org.ovirt.engine.core.common.businessentities.VolumeType;
import org.ovirt.engine.core.common.businessentities.async_tasks;
import org.ovirt.engine.core.common.errors.VdcBLLException;
import org.ovirt.engine.core.common.errors.VdcBllErrors;
import org.ovirt.engine.core.common.vdscommands.CreateSnapshotVDSCommandParameters;
import org.ovirt.engine.core.common.vdscommands.VDSCommandType;
import org.ovirt.engine.core.common.vdscommands.VDSReturnValue;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;

/**
 * This command responsible to creating snapshot from existing image and replace it to VM, holds the image. This command
 * legal only for images, appeared in Db
 */

@InternalCommandAttribute
public class CreateSnapshotCommand<T extends ImagesActionsParametersBase> extends BaseImagesCommand<T> {
    protected DiskImage mNewCreatedDiskImage;

    public CreateSnapshotCommand(T parameters) {
        super(parameters);
        setSnapshotName(parameters.getDescription());
    }

    protected ImagesContainterParametersBase getImagesContainterParameters() {
        VdcActionParametersBase tempVar = getParameters();
        return (ImagesContainterParametersBase) ((tempVar instanceof ImagesContainterParametersBase) ? tempVar : null);
    }

    @Override
    protected void executeCommand() {
        super.executeCommand();
        if (CanCreateSnapshot()) {
            VDSReturnValue vdsReturnValue = performImageVdsmOperation();
            if (vdsReturnValue != null && vdsReturnValue.getSucceeded()) {
                /**
                 * Vitaly TODO: think about transactivity in DB
                 */
                ProcessOldImageFromDb();
                AddDiskImageToDb(mNewCreatedDiskImage, null);
                setActionReturnValue(mNewCreatedDiskImage);
                setSucceeded(true);
            }
        }

    }

    protected Guid getDestinationStorageDomainId() {
        return mNewCreatedDiskImage.getstorage_ids() != null ? mNewCreatedDiskImage.getstorage_ids().get(0).getValue()
                : Guid.Empty;
    }

    @Override
    protected VDSReturnValue performImageVdsmOperation() {
        setDestinationImageId(Guid.NewGuid());
        mNewCreatedDiskImage = CloneDiskImage(getDestinationImageId());
        mNewCreatedDiskImage.setstorage_ids(new ArrayList<Guid>(Arrays.asList(getDestinationStorageDomainId())));
        setStoragePoolId(mNewCreatedDiskImage.getstorage_pool_id() != null ? mNewCreatedDiskImage.getstorage_pool_id()
                .getValue() : Guid.Empty);
        getParameters().setStoragePoolId(getStoragePoolId().getValue());

        // override volume type and volume format to sparse and cow according to
        // storage team request
        mNewCreatedDiskImage.setvolume_type(VolumeType.Sparse);
        mNewCreatedDiskImage.setvolume_format(VolumeFormat.COW);
        VDSReturnValue vdsReturnValue = null;

        try {
           vdsReturnValue =
                    Backend
                            .getInstance()
                            .getResourceManager()
                            .RunVdsCommand(
                                    VDSCommandType.CreateSnapshot,
                                    new CreateSnapshotVDSCommandParameters(getStoragePoolId().getValue(),
                                            getDestinationStorageDomainId(),
                                            getImageGroupId(),
                                            getImage().getImageId(),
                                            getDiskImage().getsize(),
                                            mNewCreatedDiskImage.getvolume_type(),
                                            mNewCreatedDiskImage.getvolume_format(),
                                            getDiskImage().getimage_group_id().getValue(),
                                            getDestinationImageId(),
                                            "",
                                            getStoragePool().getcompatibility_version().toString()));

            if (vdsReturnValue.getSucceeded()) {
                getParameters().setTaskIds(new java.util.ArrayList<Guid>());
                getParameters().getTaskIds().add(
                        CreateTask(vdsReturnValue.getCreationInfo(), getParameters().getParentCommand()));
                getReturnValue().getInternalTaskIdList().add(getParameters().getTaskIds().get(0));

                // Shouldn't happen anymore:
                if (getDestinationImageId().equals(Guid.Empty)) {
                    throw new RuntimeException();
                }
            }
        } catch (java.lang.Exception e) {
            log.errorFormat(
                    "CreateSnapshotCommand::CreateSnapshotInIrsServer::Failed creating snapshot from image id -'{0}'",
                    getImage().getImageId());
            throw new VdcBLLException(VdcBllErrors.VolumeCreationError);
        }

        return vdsReturnValue;
    }

    @Override
    protected Guid ConcreteCreateTask(AsyncTaskCreationInfo asyncTaskCreationInfo, VdcActionType parentCommand) {
        VdcActionParametersBase parametersForTask = getParametersForTask(parentCommand, getParameters());
        AsyncTaskParameters p =
                new AsyncTaskParameters(asyncTaskCreationInfo, new async_tasks(parentCommand,
                        AsyncTaskResultEnum.success,
                        AsyncTaskStatusEnum.running,
                        asyncTaskCreationInfo.getTaskID(),
                        parametersForTask,
                        asyncTaskCreationInfo.getStepId(),
                        getCommandId()));
        p.setEntityId(getParameters().getEntityId());
        Guid ret = AsyncTaskManager.getInstance().CreateTask(AsyncTaskType.createVolume, p, false);
        //
        // VmId != Guid.Empty ? VmId :
        // ImageContainerId != Guid.Empty ? ImageContainerId :
        // DbFacade.Instance.GetVmByImageId(DiskImage.image_guid).vm_guid),
        //

        return ret;
    }

    /**
     * By default old image must be replaced by new one
     */
    protected void ProcessOldImageFromDb() {
        getParameters().setOldLastModifiedValue(getDiskImage().getlastModified());
        getDiskImage().setlastModified(new Date());
        getDiskImage().setactive(false);
        getImageDao().update(getDiskImage().getImage());
    }

    @Override
    protected void EndWithFailure() {
        RevertTasks();

        if (getDestinationDiskImage() != null
                && !DbFacade.getInstance().getVmDAO().getVmsListForDisk(getDestinationDiskImage().getId()).isEmpty()) {
            // Empty Guid, means new disk rather than snapshot, so no need to add a map to the db for new disk.
            if (!getDestinationDiskImage().getParentId().equals(Guid.Empty)) {
                if (!getDestinationDiskImage().getParentId().equals(getDestinationDiskImage().getit_guid())) {
                    DiskImage previousSnapshot = getDiskImageDao().getSnapshotById(
                            getDestinationDiskImage().getParentId());
                    previousSnapshot.setactive(true);

                    // If the old description of the snapshot got overriden, we should restore the previous description
                    if (getParameters().getOldLastModifiedValue() != null) {
                        previousSnapshot.setlastModified(getParameters().getOldLastModifiedValue());
                    }

                    getImageDao().update(previousSnapshot.getImage());
                }
            }
        }

        super.EndWithFailure();
    }
}
