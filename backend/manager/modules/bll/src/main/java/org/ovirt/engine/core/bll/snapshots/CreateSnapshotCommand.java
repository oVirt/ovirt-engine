package org.ovirt.engine.core.bll.snapshots;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Set;

import javax.inject.Inject;

import org.ovirt.engine.core.bll.InternalCommandAttribute;
import org.ovirt.engine.core.bll.NonTransactiveCommandAttribute;
import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.bll.storage.disk.image.BaseImagesCommand;
import org.ovirt.engine.core.bll.storage.disk.image.ImagesHandler;
import org.ovirt.engine.core.bll.tasks.CommandCoordinatorUtil;
import org.ovirt.engine.core.common.VdcObjectType;
import org.ovirt.engine.core.common.action.ActionParametersBase;
import org.ovirt.engine.core.common.action.ActionType;
import org.ovirt.engine.core.common.action.CreateSnapshotParameters;
import org.ovirt.engine.core.common.action.DestroyImageParameters;
import org.ovirt.engine.core.common.asynctasks.AsyncTaskType;
import org.ovirt.engine.core.common.businessentities.StorageDomain;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.storage.DiskImage;
import org.ovirt.engine.core.common.businessentities.storage.VolumeClassification;
import org.ovirt.engine.core.common.businessentities.storage.VolumeFormat;
import org.ovirt.engine.core.common.businessentities.storage.VolumeType;
import org.ovirt.engine.core.common.errors.EngineError;
import org.ovirt.engine.core.common.errors.EngineException;
import org.ovirt.engine.core.common.vdscommands.CreateVolumeVDSCommandParameters;
import org.ovirt.engine.core.common.vdscommands.VDSCommandType;
import org.ovirt.engine.core.common.vdscommands.VDSReturnValue;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.DiskImageDao;
import org.ovirt.engine.core.dao.ImageDao;
import org.ovirt.engine.core.dao.StorageDomainDao;
import org.ovirt.engine.core.dao.VmDao;
import org.ovirt.engine.core.utils.transaction.TransactionSupport;

/**
 * This command responsible to creating snapshot from existing image and replace it to VM, holds the image. This command
 * legal only for images, appeared in Db
 */

@InternalCommandAttribute
@NonTransactiveCommandAttribute
public class CreateSnapshotCommand<T extends CreateSnapshotParameters> extends BaseImagesCommand<T> {

    @Inject
    private ImageDao imageDao;
    @Inject
    private DiskImageDao diskImageDao;
    @Inject
    private VmDao vmDao;
    @Inject
    private StorageDomainDao storageDomainDao;
    @Inject
    private CommandCoordinatorUtil commandCoordinatorUtil;
    @Inject
    private ImagesHandler imagesHandler;

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
        if (performImageVdsmOperation()) {
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
    protected boolean performImageVdsmOperation() {
        setDestinationImageId(Guid.isNullOrEmpty(getParameters().getDestinationImageId()) ?
                Guid.newGuid() : getParameters().getDestinationImageId());
        persistCommandIfNeeded();
        newDiskImage = cloneDiskImage(getDestinationImageId());
        newDiskImage.setStorageIds(new ArrayList<>(Arrays.asList(getDestinationStorageDomainId())));
        getParameters().setStorageDomainId(getDestinationStorageDomainId());
        getParameters().setImageId(getDestinationImageId());
        getParameters().setImageGroupID(getImageGroupId());
        setStoragePoolId(newDiskImage.getStoragePoolId() != null ? newDiskImage.getStoragePoolId()
                : Guid.Empty);
        getParameters().setStoragePoolId(getStoragePoolId());

        // override volume type and volume format to sparse and cow according to
        // storage team request
        newDiskImage.setVolumeType(VolumeType.Sparse);
        newDiskImage.setVolumeFormat(VolumeFormat.COW);
        try {
            Guid taskId = persistAsyncTaskPlaceHolder(getParameters().getParentCommand());

            VDSReturnValue vdsReturnValue =
                    runVdsCommand(
                            VDSCommandType.CreateVolume,
                            getCreateVDSCommandParameters());

            if (vdsReturnValue != null && vdsReturnValue.getSucceeded()) {
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
                return true;
            }
        } catch (Exception e) {
            log.error("Failed creating snapshot from image id '{}'", getImage().getImageId());
            commandCoordinatorUtil.logAndFailTaskOfCommandWithEmptyVdsmId(getAsyncTaskId(),
                    "Create snapshot failed at VDSM. DB task ID is " + getAsyncTaskId());
            throw new EngineException(EngineError.VolumeCreationError);
        }

        return false;
    }

    private CreateVolumeVDSCommandParameters getCreateVDSCommandParameters() {
        CreateVolumeVDSCommandParameters parameters = new CreateVolumeVDSCommandParameters(getStoragePoolId(),
                getDestinationStorageDomainId(),
                getImageGroupId(),
                getImage().getImageId(),
                getDiskImage().getSize(),
                newDiskImage.getVolumeType(),
                newDiskImage.getVolumeFormat(),
                getDiskImage().getId(),
                getDestinationImageId(),
                "",
                getStoragePool().getCompatibilityVersion(),
                getDiskImage().getContentType());
        if (getParameters().getInitialSizeInBytes() != null) {
            parameters.setImageInitialSizeInBytes(getParameters().getInitialSizeInBytes());
        }

        if (imagesHandler.shouldUseDiskBitmaps(getStoragePool().getCompatibilityVersion(), getDiskImage())
                && !getParameters().isLiveSnapshot()) {
            parameters.setShouldAddBitmaps(true);
        }
        return parameters;
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
        imageDao.update(getDiskImage().getImage());
        getCompensationContext().stateChanged();
    }

    @Override
    protected void endWithFailure() {
        revertTasks();

        List<VM> vms = vmDao.getVmsListForDisk(getDestinationDiskImage().getId(), false);
        if (getDestinationDiskImage() != null
                && !vms.isEmpty()
                && !isSnapshotUsed(vms.get(0))) {

            // Empty Guid, means new disk rather than snapshot, so no need to add a map to the db for new disk.
            if (!getDestinationDiskImage().getParentId().equals(Guid.Empty)) {
                if (!getDestinationDiskImage().getParentId().equals(getDestinationDiskImage().getImageTemplateId())) {
                    DiskImage previousSnapshot = diskImageDao.getSnapshotById(getDestinationDiskImage().getParentId());
                    previousSnapshot.setActive(true);

                    // If the old description of the snapshot got overriden, we should restore the previous description
                    if (getParameters().getOldLastModifiedValue() != null) {
                        previousSnapshot.setLastModified(getParameters().getOldLastModifiedValue());
                    }

                    imageDao.update(previousSnapshot.getImage());
                }
            }
            // Remove the image from the storage
            runInternalAction(ActionType.DestroyImage,
                    buildDestroyImageParameters(getParameters().getImageGroupID(),
                            Collections.singletonList(getParameters().getImageId())));

            super.endWithFailure();
        }

        if (!getParameters().isLeaveLocked()) {
            unLockImage();
        }
        setSucceeded(true);
    }

    private boolean isSnapshotUsed(VM vm) {
        if (vm.isRunningOrPaused()) {
            Set<Guid> volumeChain = imagesHandler.getVolumeChain(vm.getId(),
                    vm.getRunOnVds(),
                    getDiskImage());

            if (volumeChain != null && !volumeChain.contains(getDiskImage().getImageId())) {
                return false;
            } else {
                log.warn("Can not get image chain or image '{}' is still in the chain, skipping deletion",
                        getDiskImage().getImageId());
            }
        }

        return true;
    }

    protected DestroyImageParameters buildDestroyImageParameters(Guid imageGroupId, List<Guid> imageList) {
        StorageDomain storageDomain = getStorageDomain();
        DestroyImageParameters parameters = new DestroyImageParameters(
                getVdsId(),
                getVmId(),
                getParameters().getStoragePoolId(),
                storageDomain.getId(),
                imageGroupId,
                imageList,
                storageDomain.getWipeAfterDelete(),
                false);
        parameters.setEndProcedure(ActionParametersBase.EndProcedure.COMMAND_MANAGED);
        parameters.setParentParameters(getParameters());
        return parameters;
    }
}
