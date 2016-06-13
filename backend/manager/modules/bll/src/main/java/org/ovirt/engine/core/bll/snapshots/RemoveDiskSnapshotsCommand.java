package org.ovirt.engine.core.bll.snapshots;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.stream.Collectors;

import org.apache.commons.lang.StringUtils;
import org.ovirt.engine.core.bll.ConcurrentChildCommandsExecutionCallback;
import org.ovirt.engine.core.bll.LockMessagesMatchUtil;
import org.ovirt.engine.core.bll.NonTransactiveCommandAttribute;
import org.ovirt.engine.core.bll.ValidationResult;
import org.ovirt.engine.core.bll.VmHandler;
import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.bll.storage.disk.image.BaseImagesCommand;
import org.ovirt.engine.core.bll.storage.disk.image.ImagesHandler;
import org.ovirt.engine.core.bll.tasks.CommandCoordinatorUtil;
import org.ovirt.engine.core.bll.tasks.SPMAsyncTaskHandler;
import org.ovirt.engine.core.bll.tasks.TaskHandlerCommand;
import org.ovirt.engine.core.bll.tasks.interfaces.CommandCallback;
import org.ovirt.engine.core.bll.utils.PermissionSubject;
import org.ovirt.engine.core.bll.validator.VmValidator;
import org.ovirt.engine.core.bll.validator.storage.DiskImagesValidator;
import org.ovirt.engine.core.bll.validator.storage.DiskSnapshotsValidator;
import org.ovirt.engine.core.bll.validator.storage.StorageDomainValidator;
import org.ovirt.engine.core.bll.validator.storage.StoragePoolValidator;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.VdcObjectType;
import org.ovirt.engine.core.common.action.LockProperties;
import org.ovirt.engine.core.common.action.LockProperties.Scope;
import org.ovirt.engine.core.common.action.RemoveAllVmCinderDisksParameters;
import org.ovirt.engine.core.common.action.RemoveDiskSnapshotsParameters;
import org.ovirt.engine.core.common.action.RemoveSnapshotSingleDiskParameters;
import org.ovirt.engine.core.common.action.VdcActionParametersBase.EndProcedure;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.action.VdcReturnValueBase;
import org.ovirt.engine.core.common.asynctasks.AsyncTaskCreationInfo;
import org.ovirt.engine.core.common.businessentities.Snapshot;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VmDevice;
import org.ovirt.engine.core.common.businessentities.storage.CinderDisk;
import org.ovirt.engine.core.common.businessentities.storage.DiskImage;
import org.ovirt.engine.core.common.businessentities.storage.DiskStorageType;
import org.ovirt.engine.core.common.businessentities.storage.ImageStatus;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.common.locks.LockingGroup;
import org.ovirt.engine.core.common.utils.Pair;
import org.ovirt.engine.core.compat.Guid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@NonTransactiveCommandAttribute(forceCompensation = true)
public class RemoveDiskSnapshotsCommand<T extends RemoveDiskSnapshotsParameters> extends BaseImagesCommand<T>
        implements TaskHandlerCommand<RemoveDiskSnapshotsParameters> {

    private static final Logger log = LoggerFactory.getLogger(RemoveDiskSnapshotsCommand.class);
    private List<DiskImage> images;
    private SnapshotsValidator snapshotsValidator;
    private StorageDomainValidator storageDomainValidator;

    public RemoveDiskSnapshotsCommand(T parameters, CommandContext cmdContext) {
        super(parameters, cmdContext);
    }

    @Override
    protected LockProperties applyLockProperties(LockProperties lockProperties) {
        return lockProperties.withScope(Scope.Execution);
    }

    @Override
    protected void init(T parameters) {
        super.init(parameters);

        // Images must be specified in parameters and belong to a single Disk;
        // Otherwise, we'll fail on validate.
        if (getRepresentativeImage().isPresent()) {
            DiskImage representativeImage = getRepresentativeImage().get();
            setImage(representativeImage);
            getParameters().setStorageDomainId(representativeImage.getStorageIds().get(0));
            getParameters().setDiskAlias(representativeImage.getDiskAlias());
            getParameters().setUseCinderCommandCallback(!ImagesHandler.filterDisksBasedOnCinder(getImages()).isEmpty());

            if (Guid.isNullOrEmpty(getParameters().getContainerId())) {
                List<VM> listVms = getVmDao().getVmsListForDisk(representativeImage.getId(), false);
                if (!listVms.isEmpty()) {
                    VM vm = listVms.get(0);
                    setVm(vm);
                    getParameters().setContainerId(vm.getId());
                }
            }
        }

        setVmId(getParameters().getContainerId());
        setStorageDomainId(getParameters().getStorageDomainId());

        // It would be better to not add the task handlers in the first place, but at
        // the time they are added (via super.init()), setVm() hasn't been called and
        // thus initTaskHandlers() can't yet tell if this is a live or cold merge.
        if (isLiveMerge()) {
            clearTaskHandlers();
        }
    }

    protected List<DiskImage> getImages() {
        if (images == null) {
            images = new ArrayList<>();
            for (Guid imageId : getParameters().getImageIds()) {
                if (imageId == null) {
                    // Disks existence is validated in validate
                    continue;
                }

                DiskImage image = getDiskImageDao().getSnapshotById(imageId);
                if (image != null) {
                    images.add(image);
                }
            }
        }
        return images;
    }

    /**
     * Returns the images chain of the disk.
     */
    protected List<DiskImage> getAllImagesForDisk() {
        return getDiskImageDao().getAllSnapshotsForImageGroup(getImageGroupId());
    }

    protected SnapshotsValidator getSnapshotsValidator() {
        if (snapshotsValidator == null) {
            snapshotsValidator = new SnapshotsValidator();
        }
        return snapshotsValidator;
    }

    protected StorageDomainValidator getStorageDomainValidator() {
        if (storageDomainValidator == null) {
            storageDomainValidator = new StorageDomainValidator(getStorageDomain());
        }
        return storageDomainValidator;
    }

    @Override
    protected boolean validate() {
        if (getVm() == null) {
            return failValidation(EngineMessage.ACTION_TYPE_FAILED_VM_NOT_FOUND);
        }

        DiskSnapshotsValidator diskSnapshotsValidator = createDiskSnapshotsValidator(getImages());
        if (!validate(diskSnapshotsValidator.diskSnapshotsNotExist(getParameters().getImageIds())) ||
                !validate(diskSnapshotsValidator.diskImagesBelongToSameImageGroup()) ||
                !validate(diskSnapshotsValidator.imagesAreSnapshots())) {
            return false;
        }

        // Validate all chain of images in the disk
        if (!validateAllDiskImages()) {
            return false;
        }

        DiskImagesValidator diskImagesValidator = createDiskImageValidator(getImages());
        if (!validate(diskImagesValidator.diskImagesSnapshotsNotAttachedToOtherVms(false))) {
            return false;
        }

        if (!canRunActionOnNonManagedVm()) {
            return false;
        }

        if (isDiskPlugged()) {
            VmValidator vmValidator = createVmValidator(getVm());
            if (!validate(vmValidator.vmQualifiedForSnapshotMerge())) {
                return false;
            }
        }

        if (!validate(new StoragePoolValidator(getStoragePool()).isUp()) ||
                !validateVmNotDuringSnapshot() ||
                !validateVmNotInPreview() ||
                !validateSnapshotExists() ||
                !validateStorageDomainActive()) {
            return false;
        }

        if (!validateStorageDomainAvailableSpace()) {
            return false;
        }

        return true;
    }

    @Override
    protected void setActionMessageParameters() {
        addValidationMessage(EngineMessage.VAR__ACTION__REMOVE);
        addValidationMessage(EngineMessage.VAR__TYPE__DISK__SNAPSHOT);
    }

    @Override
    protected List<SPMAsyncTaskHandler> initTaskHandlers() {
        List<SPMAsyncTaskHandler> taskHandlers = new ArrayList<>();

        // Sort images from parent to leaf (active) - needed only once as the sorted list is
        // being saved in the parameters.  The conditions to check vary between cold and live
        // merge (and we can't yet run isLiveMerge()), so we just use an explicit flag.
        if (!getParameters().isImageIdsSorted()) {
            // Retrieve and sort the entire chain of images
            List<DiskImage> images = getAllImagesForDisk();
            ImagesHandler.sortImageList(images);

            // Get a sorted list of the selected images
            List<DiskImage> sortedImages =
                    images.stream().filter(image -> getImages().contains(image)).collect(Collectors.toList());
            getParameters().setImageIds(new ArrayList<>(ImagesHandler.getDiskImageIds(sortedImages)));
            getParameters().setImageIdsSorted(true);
        }

        ArrayList<Guid> imageIds = new ArrayList<>();
        for (DiskImage diskImage : getImages()) {
            if (diskImage.getDiskStorageType() == DiskStorageType.IMAGE) {
                imageIds.add(diskImage.getImageId());
            }
        }
        getParameters().setImageIds(imageIds);

        for (Guid imageId : imageIds) {
            taskHandlers.add(new RemoveDiskSnapshotTaskHandler(this, imageId, getImageGroupId(), getVmId()));
        }

        return !taskHandlers.isEmpty() ? taskHandlers : null;
    }

    @Override
    public CommandCallback getCallback() {
        // Handle first execution based on vm status, and recovery based on isLiveMerge (VM may be down)
        if (isLiveMerge()) {
            return new RemoveDiskSnapshotsCommandCallback();
        } else if (getParameters().isUseCinderCommandCallback()) {
            return new ConcurrentChildCommandsExecutionCallback();
        }
        return null;
    }

    private boolean isLiveMerge() {
        return (getParameters().isLiveMerge() || (getVm() != null && getVm().isQualifiedForLiveSnapshotMerge()))
                && !ImagesHandler.filterImageDisks(getImages(), false, false, false).isEmpty();
    }

    @Override
    protected void executeCommand() {
        if (isLiveMerge()) {
            getParameters().setLiveMerge(true);
            persistCommand(getParameters().getParentCommand(), true);
        }
        removeCinderSnapshotDisks();
        setSucceeded(true);
    }

    public void startNextLiveMerge(int completedChildren) {
        if (completedChildren == 0) {
            // Lock all disk images in advance
            ImagesHandler.updateAllDiskImageSnapshotsStatus(getImageGroupId(), ImageStatus.LOCKED);
        } else {
            checkImageIdConsistency(completedChildren - 1);
        }

        Guid imageId = getParameters().getImageIds().get(completedChildren);
        log.info("Starting child command {} of {}, image '{}'",
                completedChildren + 1, getParameters().getImageIds().size(), imageId);

        RemoveSnapshotSingleDiskParameters parameters = buildRemoveSnapshotSingleDiskLiveParameters(imageId);
        parameters.setEndProcedure(EndProcedure.COMMAND_MANAGED);
        if (getParameters().getChildImageIds() == null) {
            getParameters().setChildImageIds(Arrays.asList(new Guid[getParameters().getImageIds().size()]));
        }
        getParameters().getChildImageIds().set(completedChildren, parameters.getDestinationImageId());
        persistCommand(getParameters().getParentCommand(), true);

        CommandCoordinatorUtil.executeAsyncCommand(
                VdcActionType.RemoveSnapshotSingleDiskLive,
                parameters,
                cloneContextAndDetachFromParent());
    }

    /**
     * Ensures that after a backwards merge (in which the current snapshot's image takes the
     * place of the next snapshot's image), subsequent task handlers will refer to the correct
     * image id and not the one that has been removed.
     */
    private void checkImageIdConsistency(int completedImageIndex) {
        Guid imageId = getParameters().getImageIds().get(completedImageIndex);
        Guid childImageId = getParameters().getChildImageIds().get(completedImageIndex);
        if (getDiskImageDao().get(childImageId) == null) {
            // Swap instances of the removed id with our id
            for (int i = completedImageIndex + 1; i < getParameters().getImageIds().size(); i++) {
                if (getParameters().getImageIds().get(i).equals(childImageId)) {
                    getParameters().getImageIds().set(i, imageId);
                    log.info("Switched child command {} image id from '{}' to '{}' due to backwards merge",
                            i + 1, childImageId, imageId);
                    persistCommand(getParameters().getParentCommand(), true);
                }
            }
        }
    }

    private RemoveSnapshotSingleDiskParameters buildRemoveSnapshotSingleDiskLiveParameters(Guid imageId) {
        DiskImage dest = getDiskImageDao().getAllSnapshotsForParent(imageId).get(0);
        RemoveSnapshotSingleDiskParameters parameters =
                new RemoveSnapshotSingleDiskParameters(imageId, getVmId());
        parameters.setDestinationImageId(dest.getImageId());
        parameters.setEntityInfo(getParameters().getEntityInfo());
        parameters.setParentParameters(getParameters());
        parameters.setParentCommand(getActionType());
        parameters.setCommandType(VdcActionType.RemoveSnapshotSingleDiskLive);
        parameters.setVdsId(getVm().getRunOnVds());
        parameters.setSessionId(getParameters().getSessionId());
        return parameters;
    }

    protected void updateSnapshotVmConfiguration() {
        Guid imageId = getParameters().getImageIds().get(getParameters().getExecutionIndex());
        Snapshot snapshot = getSnapshotDao().get(getSnapshotId());

        Snapshot snapshotWithoutImage = ImagesHandler.prepareSnapshotConfigWithoutImageSingleImage(snapshot, imageId);
        getSnapshotDao().update(snapshotWithoutImage);
    }

    @Override
    public void taskEndSuccessfully() {
        lockVmSnapshotsWithWait(getVm());
        updateSnapshotVmConfiguration();
        if (getSnapshotsEngineLock() != null) {
            getLockManager().releaseLock(getSnapshotsEngineLock());
        }
    }

    @Override
    protected void endSuccessfully() {
        unlockImages();
        setSucceeded(true);
    }

    @Override
    protected void endWithFailure() {
        unlockImages();
        setSucceeded(true);
    }

    private void removeCinderSnapshotDisks() {
        List<CinderDisk> cinderDisks = ImagesHandler.filterDisksBasedOnCinder(getImages());
        if (cinderDisks.isEmpty()) {
            return;
        }
        Future<VdcReturnValueBase> future = CommandCoordinatorUtil.executeAsyncCommand(
                VdcActionType.RemoveAllCinderSnapshotDisks,
                buildRemoveCinderSnapshotDiskParameters(cinderDisks),
                cloneContextAndDetachFromParent());
        try {
            VdcReturnValueBase vdcReturnValueBase = future.get();
            if (!vdcReturnValueBase.getSucceeded()) {
                log.error("Error removing snapshots for Cinder disks");
                if (!hasTaskHandlers()) {
                    endWithFailure();
                    getParameters().setTaskGroupSuccess(false);
                }
            } else {
                Snapshot snapshotWithoutImage = null;
                Snapshot snapshot = getSnapshotDao().get(cinderDisks.get(0).getSnapshotId());
                lockVmSnapshotsWithWait(getVm());
                for (CinderDisk cinderDisk : cinderDisks) {
                    snapshotWithoutImage = ImagesHandler.prepareSnapshotConfigWithoutImageSingleImage(
                            snapshot, cinderDisk.getImageId());
                }
                getSnapshotDao().update(snapshotWithoutImage);
                if (getSnapshotsEngineLock() != null) {
                    getLockManager().releaseLock(getSnapshotsEngineLock());
                }
                if (!hasTaskHandlers()) {
                    endSuccessfully();
                    getParameters().setTaskGroupSuccess(true);
                }
            }
        } catch (InterruptedException | ExecutionException e) {
            log.error("Error removing snapshots for Cinder disks");
            if (!hasTaskHandlers()) {
                endWithFailure();
                getParameters().setTaskGroupSuccess(false);
            }
        }
    }

    private RemoveAllVmCinderDisksParameters buildRemoveCinderSnapshotDiskParameters(List<CinderDisk> cinderDisks) {
        RemoveAllVmCinderDisksParameters params = new RemoveAllVmCinderDisksParameters();
        params.setCinderDisks(cinderDisks);
        params.setParentCommand(getActionType());
        params.setParentParameters(getParameters());
        params.setSessionId(getParameters().getSessionId());
        params.setInvokeEndActionOnParent(false);
        params.setEndProcedure(EndProcedure.COMMAND_MANAGED);
        return params;
    }

    private void unlockImages() {
        // Some Live Merge failure cases leave a subset of images illegal;
        // they should remain illegal while the others are unlocked.
        List<DiskImage> images = getAllImagesForDisk();
        for (DiskImage image : images) {
            if (image.getImageStatus() == ImageStatus.LOCKED) {
                getImageDao().updateStatus(image.getImageId(), ImageStatus.OK);
            }
        }
    }

    @Override
    public Map<String, String> getJobMessageProperties() {
        if (jobProperties == null) {
            jobProperties = super.getJobMessageProperties();
            jobProperties.put("snapshots", StringUtils.join(getSnapshotsNames(), ", "));
        }
        return jobProperties;
    }

    private void addAuditLogCustomValues() {
        this.addCustomValue("DiskAlias", getParameters().getDiskAlias());
        this.addCustomValue("Snapshots", StringUtils.join(getSnapshotsNames(), ", "));
    }

    private List<String> getSnapshotsNames() {
        // The caching is done during initial command execution (called by addAuditLogCustomValues)
        // which will prevent audit logging of '<UNKNOWN>' when the command completes.
        if (getParameters().getSnapshotNames() == null) {
            getParameters().setSnapshotNames(new LinkedList<>());
            for (DiskImage image : getImages()) {
                Snapshot snapshot = getSnapshotDao().get(image.getSnapshotId());
                if (snapshot != null) {
                    getParameters().getSnapshotNames().add(snapshot.getDescription());
                }
            }
        }
        return getParameters().getSnapshotNames();
    }

    protected boolean canRunActionOnNonManagedVm() {
        ValidationResult nonManagedVmValidationResult = VmHandler.canRunActionOnNonManagedVm(getVm(), this.getActionType());
        if (!nonManagedVmValidationResult.isValid()) {
            return failValidation(nonManagedVmValidationResult.getMessage());
        }
        return true;
    }

    protected boolean validateVmNotDuringSnapshot() {
        return validate(getSnapshotsValidator().vmNotDuringSnapshot(getVmId()));
    }

    protected boolean validateVmNotInPreview() {
        return validate(getSnapshotsValidator().vmNotInPreview(getVmId()));
    }

    protected boolean validateSnapshotExists() {
        return validate(getSnapshotsValidator().snapshotExists(getVmId(), getSnapshotId()));
    }

    protected boolean validateAllDiskImages() {
        List<DiskImage> images = getDiskImageDao().getAllSnapshotsForImageGroup(getDiskImage().getId());
        DiskImagesValidator diskImagesValidator = new DiskImagesValidator(images);

        return validate(diskImagesValidator.diskImagesNotLocked()) &&
                validate(diskImagesValidator.diskImagesNotIllegal());
    }

    protected boolean validateStorageDomainActive() {
        return validate(getStorageDomainValidator().isDomainExistAndActive());
    }

    protected boolean validateStorageDomainAvailableSpace() {
        // What should be checked here is that there's enough space for removing a set of disk snapshots consecutively.
        // Worst-case scenario when merging a snapshot in terms of space, is the outcome volume, along with the not-yet-deleted volumes.
        // The following implementation does just that. In this case only snapshots are passed to the validation
        // (as opposed to the whole chain).
        List<DiskImage> disksList = ImagesHandler.getSnapshotsDummiesForStorageAllocations(getImages());
        return validate(getStorageDomainValidator().hasSpaceForClonedDisks(disksList));
    }

    protected DiskImagesValidator createDiskImageValidator(List<DiskImage> disksList) {
        return new DiskImagesValidator(disksList);
    }

    protected DiskSnapshotsValidator createDiskSnapshotsValidator(List<DiskImage> images) {
        return new DiskSnapshotsValidator(images);
    }

    protected VmValidator createVmValidator(VM vm) {
        return new VmValidator(vm);
    }

    private Optional<DiskImage> getRepresentativeImage() {
        return getImages().stream().findFirst();
    }

    @Override
    protected Guid getImageGroupId() {
        return getRepresentativeImage().map(DiskImage::getId).orElse(Guid.Empty);
    }

    protected boolean isDiskPlugged() {
        List<VmDevice> devices = getVmDeviceDao().getVmDevicesByDeviceId(getImageGroupId(), getVmId());
        return !devices.isEmpty() && devices.get(0).getIsPlugged();
    }

    @Override
    public List<PermissionSubject> getPermissionCheckSubjects() {
        List<PermissionSubject> permissionList = new ArrayList<>();
        permissionList.add(new PermissionSubject(getVmId(),
                VdcObjectType.VM,
                getActionType().getActionGroup()));
        return permissionList;
    }

    @Override
    public AuditLogType getAuditLogTypeValue() {
        addAuditLogCustomValues();
        switch (getActionState()) {
            case EXECUTE:
                if (!hasTaskHandlers() && !getParameters().isUseCinderCommandCallback()) {
                    return getParameters().getTaskGroupSuccess() ?
                            AuditLogType.USER_REMOVE_DISK_SNAPSHOT :
                            AuditLogType.USER_REMOVE_DISK_SNAPSHOT_FINISHED_FAILURE;
                }
                if (isFirstTaskHandler() && getSucceeded()) {
                    return AuditLogType.USER_REMOVE_DISK_SNAPSHOT;
                }
                if (!getParameters().getTaskGroupSuccess()) {
                    return AuditLogType.USER_FAILED_REMOVE_DISK_SNAPSHOT;
                }
                break;

            case END_SUCCESS:
                return AuditLogType.USER_REMOVE_DISK_SNAPSHOT_FINISHED_SUCCESS;

            case END_FAILURE:
                return AuditLogType.USER_REMOVE_DISK_SNAPSHOT_FINISHED_FAILURE;
        }
        return AuditLogType.UNASSIGNED;
    }

    @Override
    protected Map<String, Pair<String, String>> getExclusiveLocks() {
        return Collections.singletonMap(getImageGroupId().toString(),
                LockMessagesMatchUtil.makeLockingPair(LockingGroup.DISK, EngineMessage.ACTION_TYPE_FAILED_DISKS_LOCKED));
    }

    @Override
    public Guid createTask(Guid taskId, AsyncTaskCreationInfo asyncTaskCreationInfo, VdcActionType parentCommand) {
        return super.createTask(taskId, asyncTaskCreationInfo, parentCommand);
    }

    @Override
    public Guid createTask(
            Guid taskId,
            AsyncTaskCreationInfo asyncTaskCreationInfo,
            VdcActionType parentCommand,
            VdcObjectType entityType,
            Guid... entityIds) {
        return super.createTask(taskId, asyncTaskCreationInfo, parentCommand, entityType, entityIds);
    }

    @Override
    public ArrayList<Guid> getTaskIdList() {
        return super.getTaskIdList();
    }

    @Override
    public void preventRollback() {
        getParameters().setExecutionIndex(0);
    }

    @Override
    public Guid persistAsyncTaskPlaceHolder() {
        return super.persistAsyncTaskPlaceHolder(getActionType());
    }

    @Override
    public Guid persistAsyncTaskPlaceHolder(String taskKey) {
        return super.persistAsyncTaskPlaceHolder(getActionType(), taskKey);
    }

    protected Guid getSnapshotId() {
        return getImage() != null ? getImage().getSnapshotId() : null;
    }

    private boolean isFirstTaskHandler() {
        return getParameters().getExecutionIndex() == 0;
    }
}
