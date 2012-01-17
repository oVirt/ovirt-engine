package org.ovirt.engine.core.bll;

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
import org.ovirt.engine.core.common.businessentities.image_vm_map;
import org.ovirt.engine.core.common.businessentities.image_vm_map_id;
import org.ovirt.engine.core.common.errors.VdcBLLException;
import org.ovirt.engine.core.common.errors.VdcBllErrors;
import org.ovirt.engine.core.common.vdscommands.CreateSnapshotVDSCommandParameters;
import org.ovirt.engine.core.common.vdscommands.VDSCommandType;
import org.ovirt.engine.core.common.vdscommands.VDSReturnValue;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.LogCompat;
import org.ovirt.engine.core.compat.LogFactoryCompat;
import org.ovirt.engine.core.compat.StringHelper;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;

/**
 * This command responsible to creating snapshot from existing image and replace it to VM, holds the image. This command
 * legal only for images, appeared in Db
 */

@InternalCommandAttribute
public class CreateSnapshotCommand<T extends ImagesActionsParametersBase> extends BaseImagesCommand<T> {
    protected DiskImage mNewCreatedDiskImage;
    private String mDescription = "";

    public CreateSnapshotCommand(T parameters) {
        super(parameters);
        mDescription = parameters.getDescription();
        setSnapshotName(mDescription);
    }

    protected ImagesContainterParametersBase getImagesContainterParameters() {
        VdcActionParametersBase tempVar = getParameters();
        return (ImagesContainterParametersBase) ((tempVar instanceof ImagesContainterParametersBase) ? tempVar : null);
    }

    @Override
    protected void executeCommand() {
        super.executeCommand();
        if (CanCreateSnapshot()) {
            if (CreateSnapshotInIrsServer()) {
                /**
                 * Vitaly TODO: think about transactivity in DB
                 */
                ProcessOldImageFromDb();
                AddDiskImageToDb(mNewCreatedDiskImage);
                setSucceeded(true);
            }
        } else {
            setActionReturnValue(Guid.Empty);
        }

    }

    protected Guid getDestinationStorageDomainId() {
        return mNewCreatedDiskImage.getstorage_id() != null ? mNewCreatedDiskImage.getstorage_id().getValue()
                : Guid.Empty;
    }

    protected boolean CreateSnapshotInIrsServer() {
        setDestinationImageId(Guid.NewGuid());
        mNewCreatedDiskImage = CloneDiskImage(getDestinationImageId());
        mNewCreatedDiskImage.setstorage_id(getDestinationStorageDomainId());
        setStoragePoolId(mNewCreatedDiskImage.getstorage_pool_id() != null ? mNewCreatedDiskImage.getstorage_pool_id()
                .getValue() : Guid.Empty);
        getParameters().setStoragePoolId(getStoragePoolId().getValue());

        // override volume type and volume format to sparse and cow according to
        // storage team request
        mNewCreatedDiskImage.setvolume_type(VolumeType.Sparse);
        mNewCreatedDiskImage.setvolume_format(VolumeFormat.COW);

        try {
            VDSReturnValue vdsReturnValue =
                    Backend
                            .getInstance()
                            .getResourceManager()
                            .RunVdsCommand(
                                    VDSCommandType.CreateSnapshot,
                                    new CreateSnapshotVDSCommandParameters(getStoragePoolId().getValue(),
                                            getDestinationStorageDomainId(),
                                            getImageGroupId(),
                                            getImage().getId(),
                                            getDiskImage().getsize(),
                                            mNewCreatedDiskImage.getvolume_type(),
                                            mNewCreatedDiskImage.getvolume_format(),
                                            mNewCreatedDiskImage.getdisk_type(),
                                            getDiskImage().getimage_group_id().getValue(),
                                            getDestinationImageId(),
                                            CalculateImageDescription(),
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

            else {
                return false;
            }
        } catch (java.lang.Exception e) {
            log.errorFormat(
                    "CreateSnapshotCommand::CreateSnapshotInIrsServer::Failed creating snapshot from image id -'{0}'",
                    getImage().getId());
            throw new VdcBLLException(VdcBllErrors.VolumeCreationError);
        }

        return true;
    }

    @Override
    protected Guid ConcreteCreateTask(AsyncTaskCreationInfo asyncTaskCreationInfo, VdcActionType parentCommand) {
        VdcActionParametersBase parametersForTask = getParametersForTask(parentCommand, getParameters());
        AsyncTaskParameters p =
                new AsyncTaskParameters(asyncTaskCreationInfo, new async_tasks(parentCommand,
                        AsyncTaskResultEnum.success,
                        AsyncTaskStatusEnum.running,
                        asyncTaskCreationInfo.getTaskID(),
                        parametersForTask));
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
        // removing the old C drive from image table
        /**
         * Vitaly TODO: check if can use Image variable
         */
        if (!StringHelper.EqOp(mDescription, "")) {
            getParameters().setOldDescription(getImage().getdescription());
            getImage().setdescription(mDescription);
        }
        getParameters().setOldLastModifiedValue(getDiskImage().getlastModified());
        getDiskImage().setlastModified(getNow());
        DbFacade.getInstance().getDiskImageDAO().update(getDiskImage());
        DbFacade.getInstance()
                .getImageVmMapDAO()
                .remove(new image_vm_map_id(getImage().getId(), getImageContainerId()));
    }

    @Override
    protected void EndWithFailure() {
        RevertTasks();

        if (getDestinationDiskImage() != null
                && DbFacade.getInstance().getVmStaticDAO().get(getDestinationDiskImage().getcontainer_guid()) != null) {
            DbFacade.getInstance()
                    .getImageVmMapDAO()
                    .remove(new image_vm_map_id(
                            getDestinationImageId(), getDestinationDiskImage().getcontainer_guid()));

            // Empty Guid, means new disk rather than snapshot, so no need to add a map to the db for new disk.
            if (!getDestinationDiskImage().getParentId().equals(Guid.Empty)) {
                DbFacade.getInstance().getImageVmMapDAO().save(
                        new image_vm_map(true, getDestinationDiskImage().getParentId(), getDestinationDiskImage()
                                .getcontainer_guid()));
                if (!getDestinationDiskImage().getParentId().equals(getDestinationDiskImage().getit_guid())) {
                    // If the old description of the snapshot got overriden, we should restore the previous description
                    if (getParameters().getOldDescription() != null
                            && !getParameters().getDescription().equals(getParameters().getOldDescription())) {
                        DiskImage previousSnapshot =
                                DbFacade.getInstance().getDiskImageDAO().get(getDestinationDiskImage().getParentId());
                        previousSnapshot.setdescription(getParameters().getOldDescription());
                        previousSnapshot.setlastModified(getParameters().getOldLastModifiedValue());
                        DbFacade.getInstance().getDiskImageDAO().update(previousSnapshot);
                    }
                }
            }
        }

        super.EndWithFailure();
    }

    private static LogCompat log = LogFactoryCompat.getLog(CreateSnapshotCommand.class);
}
