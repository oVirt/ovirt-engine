package org.ovirt.engine.core.bll;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;

import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.bll.tasks.CommandCoordinatorUtil;
import org.ovirt.engine.core.common.VdcObjectType;
import org.ovirt.engine.core.common.action.ImagesActionsParametersBase;
import org.ovirt.engine.core.common.asynctasks.AsyncTaskType;
import org.ovirt.engine.core.common.businessentities.DiskImage;
import org.ovirt.engine.core.common.businessentities.VolumeFormat;
import org.ovirt.engine.core.common.businessentities.VolumeType;
import org.ovirt.engine.core.common.errors.VdcBLLException;
import org.ovirt.engine.core.common.errors.VdcBllErrors;
import org.ovirt.engine.core.common.vdscommands.CreateSnapshotVDSCommandParameters;
import org.ovirt.engine.core.common.vdscommands.VDSCommandType;
import org.ovirt.engine.core.common.vdscommands.VDSReturnValue;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;
import org.ovirt.engine.core.utils.transaction.TransactionMethod;
import org.ovirt.engine.core.utils.transaction.TransactionSupport;

/**
 * This command responsible to creating snapshot from existing image and replace it to VM, holds the image. This command
 * legal only for images, appeared in Db
 */

@InternalCommandAttribute
@NonTransactiveCommandAttribute
public class CreateSnapshotCommand<T extends ImagesActionsParametersBase> extends BaseImagesCommand<T> {
    protected DiskImage newDiskImage;

    protected CreateSnapshotCommand(Guid commandId) {
        super(commandId);
    }

    public CreateSnapshotCommand(T parameters, CommandContext cmdContext) {
        super(parameters, cmdContext);
        setSnapshotName(parameters.getDescription());
    }

    @Override
    protected void insertAsyncTaskPlaceHolders() {
        persistAsyncTaskPlaceHolder(getParameters().getParentCommand());
    }

    @Override
    protected void executeCommand() {
        if (canCreateSnapshot()) {
            VDSReturnValue vdsReturnValue = performImageVdsmOperation();
            if (vdsReturnValue != null && vdsReturnValue.getSucceeded()) {
                TransactionSupport.executeInNewTransaction(new TransactionMethod<Void>() {
                    @Override
                    public Void runInTransaction() {
                        processOldImageFromDb();
                        addDiskImageToDb(newDiskImage, getCompensationContext());
                        setActionReturnValue(newDiskImage);
                        setSucceeded(true);
                        return null;
                    }
                });

            }
        }

    }

    protected Guid getDestinationStorageDomainId() {
        return newDiskImage.getStorageIds() != null ? newDiskImage.getStorageIds().get(0)
                : Guid.Empty;
    }

    @Override
    protected VDSReturnValue performImageVdsmOperation() {
        setDestinationImageId(Guid.newGuid());
        newDiskImage = cloneDiskImage(getDestinationImageId());
        newDiskImage.setStorageIds(new ArrayList<Guid>(Arrays.asList(getDestinationStorageDomainId())));
        setStoragePoolId(newDiskImage.getStoragePoolId() != null ? newDiskImage.getStoragePoolId()
                : Guid.Empty);
        getParameters().setStoragePoolId(getStoragePoolId());

        // override volume type and volume format to sparse and cow according to
        // storage team request
        newDiskImage.setVolumeType(VolumeType.Sparse);
        newDiskImage.setvolumeFormat(VolumeFormat.COW);
        VDSReturnValue vdsReturnValue = null;

        try {
            Guid taskId = getAsyncTaskId();

            vdsReturnValue =
                    runVdsCommand(
                                    VDSCommandType.CreateSnapshot,
                                    new CreateSnapshotVDSCommandParameters(getStoragePoolId(),
                                            getDestinationStorageDomainId(),
                                            getImageGroupId(),
                                            getImage().getImageId(),
                                            getDiskImage().getSize(),
                                            newDiskImage.getVolumeType(),
                                            newDiskImage.getVolumeFormat(),
                                            getDiskImage().getId(),
                                            getDestinationImageId(),
                                            ""));

            if (vdsReturnValue.getSucceeded()) {
                getParameters().setVdsmTaskIds(new ArrayList<Guid>());
                getParameters().getVdsmTaskIds().add(
                        createTask(taskId,
                        vdsReturnValue.getCreationInfo(),
                        getParameters().getParentCommand(),
                        VdcObjectType.Storage,
                        getParameters().getStorageDomainId(),
                        getParameters().getDestinationImageId()));
                getReturnValue().getInternalVdsmTaskIdList().add(getParameters().getVdsmTaskIds().get(0));

                // Shouldn't happen anymore:
                if (getDestinationImageId().equals(Guid.Empty)) {
                    throw new RuntimeException();
                }
            }
        } catch (Exception e) {
            log.error("Failed creating snapshot from image id '{}'", getImage().getImageId());
            CommandCoordinatorUtil.logAndFailTaskOfCommandWithEmptyVdsmId(getAsyncTaskId(),
                    "Create snapshot failed at VDSM. DB task ID is " + getAsyncTaskId());
            throw new VdcBLLException(VdcBllErrors.VolumeCreationError);
        }

        return vdsReturnValue;
    }

    @Override
    protected AsyncTaskType getTaskType() {
        return AsyncTaskType.createVolume;
    }

    /**
     * By default old image must be replaced by new one
     */
    protected void processOldImageFromDb() {
        getCompensationContext().snapshotEntity(getDiskImage().getImage());
        getParameters().setOldLastModifiedValue(getDiskImage().getLastModified());
        getDiskImage().setLastModified(new Date());
        getDiskImage().setActive(false);
        getImageDao().update(getDiskImage().getImage());
        getCompensationContext().stateChanged();
    }

    @Override
    protected void endWithFailure() {
        revertTasks();

        if (getDestinationDiskImage() != null
                && !DbFacade.getInstance().getVmDao().getVmsListForDisk(getDestinationDiskImage().getId(), false).isEmpty()) {
            // Empty Guid, means new disk rather than snapshot, so no need to add a map to the db for new disk.
            if (!getDestinationDiskImage().getParentId().equals(Guid.Empty)) {
                if (!getDestinationDiskImage().getParentId().equals(getDestinationDiskImage().getImageTemplateId())) {
                    DiskImage previousSnapshot = getDiskImageDao().getSnapshotById(
                            getDestinationDiskImage().getParentId());
                    previousSnapshot.setActive(true);

                    // If the old description of the snapshot got overriden, we should restore the previous description
                    if (getParameters().getOldLastModifiedValue() != null) {
                        previousSnapshot.setLastModified(getParameters().getOldLastModifiedValue());
                    }

                    getImageDao().update(previousSnapshot.getImage());
                }
            }
        }

        super.endWithFailure();
    }
}
