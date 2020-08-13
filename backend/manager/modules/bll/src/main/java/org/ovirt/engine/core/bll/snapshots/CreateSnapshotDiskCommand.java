package org.ovirt.engine.core.bll.snapshots;

import static org.ovirt.engine.core.bll.storage.disk.image.DisksFilter.ONLY_ACTIVE;
import static org.ovirt.engine.core.bll.storage.disk.image.DisksFilter.ONLY_NOT_SHAREABLE;
import static org.ovirt.engine.core.bll.storage.disk.image.DisksFilter.ONLY_SNAPABLE;
import static org.ovirt.engine.core.bll.storage.disk.image.DisksFilter.filterManagedBlockStorageDisks;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.stream.Collectors;

import javax.enterprise.inject.Instance;
import javax.enterprise.inject.Typed;
import javax.inject.Inject;

import org.ovirt.engine.core.bll.ConcurrentChildCommandsExecutionCallback;
import org.ovirt.engine.core.bll.NonTransactiveCommandAttribute;
import org.ovirt.engine.core.bll.VmCommand;
import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.bll.job.ExecutionHandler;
import org.ovirt.engine.core.bll.storage.disk.image.DisksFilter;
import org.ovirt.engine.core.bll.tasks.CommandCoordinatorUtil;
import org.ovirt.engine.core.bll.tasks.interfaces.CommandCallback;
import org.ovirt.engine.core.common.action.ActionReturnValue;
import org.ovirt.engine.core.common.action.ActionType;
import org.ovirt.engine.core.common.action.CreateCinderSnapshotParameters;
import org.ovirt.engine.core.common.action.CreateManagedBlockStorageDiskSnapshotParameters;
import org.ovirt.engine.core.common.action.CreateSnapshotDiskParameters;
import org.ovirt.engine.core.common.action.CreateSnapshotParameters;
import org.ovirt.engine.core.common.action.LockProperties;
import org.ovirt.engine.core.common.businessentities.storage.CinderDisk;
import org.ovirt.engine.core.common.businessentities.storage.Disk;
import org.ovirt.engine.core.common.businessentities.storage.DiskImage;
import org.ovirt.engine.core.common.businessentities.storage.DiskStorageType;
import org.ovirt.engine.core.common.errors.EngineError;
import org.ovirt.engine.core.common.errors.EngineException;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.DiskDao;

@NonTransactiveCommandAttribute
public class CreateSnapshotDiskCommand<T extends CreateSnapshotDiskParameters> extends VmCommand<T> {
    private List<DiskImage> cachedSelectedActiveDisks;
    private List<DiskImage> cachedImagesDisks;

    @Inject
    private CommandCoordinatorUtil commandCoordinatorUtil;
    @Inject
    private DiskDao diskDao;
    @Inject
    @Typed(ConcurrentChildCommandsExecutionCallback.class)
    private Instance<ConcurrentChildCommandsExecutionCallback> callbackProvider;

    public CreateSnapshotDiskCommand(T parameters,
            CommandContext cmdContext) {
        super(parameters, cmdContext);
    }

    public CreateSnapshotDiskCommand(Guid commandId) {
        super(commandId);
    }

    @Override
    public CommandCallback getCallback() {
        return callbackProvider.get();
    }

    @Override
    protected void executeCommand() {
        for (DiskImage disk : getDisksList()) {
            if (disk.getDiskStorageType() == DiskStorageType.CINDER) {
                CreateCinderSnapshotParameters params = buildChildCommandParameters(disk);
                params.setQuotaId(disk.getQuotaId());

                Future<ActionReturnValue> future = commandCoordinatorUtil.executeAsyncCommand(
                        ActionType.CreateCinderSnapshot,
                        params,
                        cloneContext().withoutCompensationContext().withoutLock());
                try {
                    ActionReturnValue actionReturnValue = future.get();
                    if (!actionReturnValue.getSucceeded()) {
                        log.error("Error creating snapshot for Cinder disk '{}'", disk.getDiskAlias());
                        throw new EngineException(EngineError.CINDER_ERROR, "Failed to create snapshot!");
                    }
                } catch (InterruptedException | ExecutionException e) {
                    log.error("Error creating snapshot for Cinder disk '{}': {}", disk.getDiskAlias(), e.getMessage());
                    throw new EngineException(EngineError.CINDER_ERROR, "Failed to create snapshot!");
                }
                continue;
            } else if (disk.getDiskStorageType() == DiskStorageType.MANAGED_BLOCK_STORAGE) {
                createManagedBlockStorageSnapshot(disk);
                continue;
            }

            ActionReturnValue actionReturnValue = runInternalAction(
                    ActionType.CreateSnapshot,
                    buildCreateSnapshotParameters(disk),
                    ExecutionHandler.createDefaultContextForTasks(getContext()));

            if (actionReturnValue.getSucceeded()) {
                getTaskIdList().addAll(actionReturnValue.getInternalVdsmTaskIdList());
            } else {
                throw new EngineException(actionReturnValue.getFault().getError(),
                        "Failed to create snapshot!");
            }
        }

        setSucceeded(true);
    }

    /**
     * Filter all allowed snapshot disks.
     * @return list of disks to be snapshot.
     */
    protected List<DiskImage> getDisksList() {
        if (cachedSelectedActiveDisks == null) {
            List<DiskImage> imagesAndCinderForVm = getDiskImagesForVm();

            // Get disks from the specified parameters or according to the VM
            if (getParameters().getDiskIds() == null) {
                cachedSelectedActiveDisks = imagesAndCinderForVm;
            } else {
                // Get selected images from 'DiskImagesForVm' to ensure disks entities integrity
                // (i.e. only images' IDs and Cinders' IDs are relevant).
                cachedSelectedActiveDisks = getDiskImagesForVm().stream()
                        .filter(d -> getParameters().getDiskIds().contains(d.getId()))
                        .collect(Collectors.toList());
            }
        }
        return cachedSelectedActiveDisks;
    }

    private void createManagedBlockStorageSnapshot(DiskImage disk) {
        CreateManagedBlockStorageDiskSnapshotParameters params = new CreateManagedBlockStorageDiskSnapshotParameters();
        params.setStorageDomainId(disk.getStorageIds().get(0));
        params.setVolumeId(disk.getImageId());
        params.setVmId(getVmId());
        params.setVmSnapshotId(getParameters().getNewActiveSnapshotId());
        params.setParentCommand(getActionType());
        params.setParentParameters(getParameters());
        runInternalAction(ActionType.CreateManagedBlockStorageDiskSnapshot, params);
    }

    @Override
    protected LockProperties applyLockProperties(LockProperties lockProperties) {
        return lockProperties.withScope(LockProperties.Scope.Command);
    }

    @Override
    protected ActionType getChildActionType() {
        return ActionType.CreateSnapshot;
    }


    @Override
    protected void endVmCommand() {
        endActionOnDisks();
        setSucceeded(true);
    }

    private List<DiskImage> getDiskImagesForVm() {
        List<Disk> disks = diskDao.getAllForVm(getVmId());
        List<DiskImage> allDisks = new ArrayList<>(getDiskImages(disks));
        allDisks.addAll(imagesHandler.getCinderLeafImages(disks));
        allDisks.addAll(filterManagedBlockStorageDisks(disks));
        return allDisks;
    }

    private List<DiskImage> getDiskImages(List<Disk> disks) {
        if (cachedImagesDisks == null) {
            cachedImagesDisks = DisksFilter.filterImageDisks(disks, ONLY_NOT_SHAREABLE,
                    ONLY_SNAPABLE, ONLY_ACTIVE);
        }

        return cachedImagesDisks;
    }

    private CreateSnapshotParameters buildCreateSnapshotParameters(DiskImage image) {
        CreateSnapshotParameters result = new CreateSnapshotParameters(image.getImageId());
        result.setDescription(getParameters().getDescription());
        result.setSessionId(getParameters().getSessionId());
        result.setQuotaId(image.getQuotaId());
        result.setDiskProfileId(image.getDiskProfileId());
        result.setVmSnapshotId(getParameters().getNewActiveSnapshotId());
        result.setEntityInfo(getParameters().getEntityInfo());
        result.setParentCommand(getActionType());
        result.setParentParameters(getParameters());
        result.setLiveSnapshot(getParameters().isLiveSnapshot());
        DiskImage diskImage = getParameters().getDiskImagesMap().get(image.getId());
        if (diskImage != null) {
            result.setDestinationImageId(diskImage.getImageId());
            result.setInitialSizeInBytes(diskImage.getInitialSizeInBytes());
        }
        if (getParameters().getDiskIdsToIgnoreInChecks().contains(image.getId())) {
            result.setLeaveLocked(true);
        }
        return result;
    }

    private CreateCinderSnapshotParameters buildChildCommandParameters(DiskImage cinderDisk) {
        CreateCinderSnapshotParameters createParams =
                new CreateCinderSnapshotParameters(((CinderDisk) diskDao.get(cinderDisk.getId())).getImageId());
        createParams.setVmSnapshotId(getParameters().getNewActiveSnapshotId());
        createParams.setStorageDomainId(cinderDisk.getStorageIds().get(0));
        createParams.setDescription(getParameters().getDescription());
        createParams.setSnapshotType(getParameters().getSnapshotType());
        createParams.setParentCommand(getActionType());
        createParams.setParentParameters(getParameters());
        return createParams;
    }

}
