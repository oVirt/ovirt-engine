package org.ovirt.engine.core.bll.snapshots;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;

import org.ovirt.engine.core.bll.InternalCommandAttribute;
import org.ovirt.engine.core.bll.NonTransactiveCommandAttribute;
import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.bll.storage.disk.image.BaseImagesCommand;
import org.ovirt.engine.core.bll.tasks.CommandCoordinatorUtil;
import org.ovirt.engine.core.common.VdcObjectType;
import org.ovirt.engine.core.common.action.ImagesActionsParametersBase;
import org.ovirt.engine.core.common.asynctasks.AsyncTaskType;
import org.ovirt.engine.core.common.businessentities.storage.DiskImage;
import org.ovirt.engine.core.common.businessentities.storage.VolumeClassification;
import org.ovirt.engine.core.common.businessentities.storage.VolumeFormat;
import org.ovirt.engine.core.common.businessentities.storage.VolumeType;
import org.ovirt.engine.core.common.errors.EngineError;
import org.ovirt.engine.core.common.errors.EngineException;
import org.ovirt.engine.core.common.vdscommands.CreateSnapshotVDSCommandParameters;
import org.ovirt.engine.core.common.vdscommands.VDSCommandType;
import org.ovirt.engine.core.common.vdscommands.VDSReturnValue;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;
import org.ovirt.engine.core.utils.transaction.TransactionSupport;

/**
 * This command responsible to creating snapshot from existing image and replace it to VM, holds the image. This command
 * legal only for images, appeared in Db
 */

@InternalCommandAttribute
@NonTransactiveCommandAttribute
public class CreateSnapshotCommand<T extends ImagesActionsParametersBase> extends BaseImagesCommand<T> {
    protected DiskImage newDiskImage;

    public CreateSnapshotCommand(Guid commandId) {
        super(commandId);
    }

    public CreateSnapshotCommand(T parameters, CommandContext cmdContext) {
        super(parameters, cmdContext);
        setSnapshotName(parameters.getDescription());
    }

    public CreateSnapshotCommand(T parameters) {
        this(parameters, null);
    }

    @Override
    protected void executeCommand() {
        VDSReturnValue vdsReturnValue = performImageVdsmOperation();
        if (vdsReturnValue != null && vdsReturnValue.getSucceeded()) {
            TransactionSupport.executeInNewTransaction(() -> {
                processOldImageFromDb();
                addDiskImageToDb(newDiskImage, getCompensationContext(), Boolean.TRUE);
                setActionReturnValue(newDiskImage);
                setSucceeded(true);
                return null;
            });
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
        newDiskImage.setStorageIds(new ArrayList<>(Arrays.asList(getDestinationStorageDomainId())));
        setStoragePoolId(newDiskImage.getStoragePoolId() != null ? newDiskImage.getStoragePoolId()
                : Guid.Empty);
        getParameters().setStoragePoolId(getStoragePoolId());

        // override volume type and volume format to sparse and cow according to
        // storage team request
        newDiskImage.setVolumeType(VolumeType.Sparse);
        newDiskImage.setVolumeFormat(VolumeFormat.COW);
        VDSReturnValue vdsReturnValue = null;

        try {
            Guid taskId = persistAsyncTaskPlaceHolder(getParameters().getParentCommand());

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
                getParameters().setVdsmTaskIds(new ArrayList<>());
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
            throw new EngineException(EngineError.VolumeCreationError);
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
        getDiskImage().setVolumeClassification(VolumeClassification.Snapshot);
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
