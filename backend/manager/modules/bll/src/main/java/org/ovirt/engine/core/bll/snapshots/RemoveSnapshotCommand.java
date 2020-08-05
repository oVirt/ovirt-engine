package org.ovirt.engine.core.bll.snapshots;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.enterprise.inject.Instance;
import javax.enterprise.inject.Typed;
import javax.inject.Inject;

import org.apache.commons.lang.StringUtils;
import org.ovirt.engine.core.bll.ConcurrentChildCommandsExecutionCallback;
import org.ovirt.engine.core.bll.DisableInPrepareMode;
import org.ovirt.engine.core.bll.LockMessagesMatchUtil;
import org.ovirt.engine.core.bll.ValidationResult;
import org.ovirt.engine.core.bll.VmCommand;
import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.bll.quota.QuotaConsumptionParameter;
import org.ovirt.engine.core.bll.quota.QuotaStorageDependent;
import org.ovirt.engine.core.bll.storage.disk.image.DisksFilter;
import org.ovirt.engine.core.bll.storage.disk.image.ImagesHandler;
import org.ovirt.engine.core.bll.tasks.CommandCoordinatorUtil;
import org.ovirt.engine.core.bll.tasks.interfaces.CommandCallback;
import org.ovirt.engine.core.bll.validator.VmValidator;
import org.ovirt.engine.core.bll.validator.storage.DiskImagesValidator;
import org.ovirt.engine.core.bll.validator.storage.MultipleStorageDomainsValidator;
import org.ovirt.engine.core.bll.validator.storage.StoragePoolValidator;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.VdcObjectType;
import org.ovirt.engine.core.common.action.ActionParametersBase;
import org.ovirt.engine.core.common.action.ActionParametersBase.EndProcedure;
import org.ovirt.engine.core.common.action.ActionReturnValue;
import org.ovirt.engine.core.common.action.ActionType;
import org.ovirt.engine.core.common.action.ImagesContainterParametersBase;
import org.ovirt.engine.core.common.action.LockProperties;
import org.ovirt.engine.core.common.action.LockProperties.Scope;
import org.ovirt.engine.core.common.action.RemoveMemoryVolumesParameters;
import org.ovirt.engine.core.common.action.RemoveSnapshotParameters;
import org.ovirt.engine.core.common.action.RemoveSnapshotSingleDiskParameters;
import org.ovirt.engine.core.common.asynctasks.EntityInfo;
import org.ovirt.engine.core.common.businessentities.Snapshot;
import org.ovirt.engine.core.common.businessentities.Snapshot.SnapshotStatus;
import org.ovirt.engine.core.common.businessentities.SubchainInfo;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.storage.CinderDisk;
import org.ovirt.engine.core.common.businessentities.storage.Disk;
import org.ovirt.engine.core.common.businessentities.storage.DiskImage;
import org.ovirt.engine.core.common.businessentities.storage.DiskStorageType;
import org.ovirt.engine.core.common.businessentities.storage.ImageStatus;
import org.ovirt.engine.core.common.businessentities.storage.ManagedBlockStorageDisk;
import org.ovirt.engine.core.common.errors.EngineError;
import org.ovirt.engine.core.common.errors.EngineException;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.common.locks.LockingGroup;
import org.ovirt.engine.core.common.utils.Pair;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.AuditLogDirector;
import org.ovirt.engine.core.dao.DiskDao;
import org.ovirt.engine.core.dao.DiskImageDao;
import org.ovirt.engine.core.dao.ImageDao;
import org.ovirt.engine.core.dao.SnapshotDao;
import org.ovirt.engine.core.dao.VmTemplateDao;
import org.ovirt.engine.core.utils.ovf.OvfManager;
import org.ovirt.engine.core.utils.transaction.TransactionSupport;

/**
 * Merges snapshots either live or non-live based on VM status
 */
@DisableInPrepareMode
public class RemoveSnapshotCommand<T extends RemoveSnapshotParameters> extends VmCommand<T>
        implements QuotaStorageDependent {
    private List<DiskImage> _sourceImages = null;

    @Inject
    private AuditLogDirector auditLogDirector;

    @Inject
    private OvfManager ovfManager;
    @Inject
    private DiskImageDao diskImageDao;
    @Inject
    private ImageDao imageDao;
    @Inject
    private DiskDao diskDao;
    @Inject
    private VmTemplateDao vmTemplateDao;
    @Inject
    private SnapshotDao snapshotDao;
    @Inject
    private CommandCoordinatorUtil commandCoordinatorUtil;
    @Inject
    @Typed(ConcurrentChildCommandsExecutionCallback.class)
    private Instance<ConcurrentChildCommandsExecutionCallback> callbackProvider;

    public RemoveSnapshotCommand(T parameters, CommandContext context) {
        super(parameters, context);
    }

    public RemoveSnapshotCommand(Guid commandId) {
        super(commandId);
    }

    @Override
    protected LockProperties applyLockProperties(LockProperties lockProperties) {
        return lockProperties.withScope(Scope.Execution);
    }

    private void initializeObjectState() {
        if (StringUtils.isEmpty(getSnapshotName())) {
            Snapshot snapshot = snapshotDao.get(getParameters().getSnapshotId());
            if (snapshot != null) {
                setSnapshotName(snapshot.getDescription());
                getParameters().setUseCinderCommandCallback(
                        !DisksFilter.filterCinderDisks(getSourceImages()).isEmpty());
            }
        }
        setStoragePoolId(getVm().getStoragePoolId());
    }

    @Override
    public Map<String, String> getJobMessageProperties() {
        if (jobProperties == null) {
            jobProperties = super.getJobMessageProperties();
            initializeObjectState();
            jobProperties.put(VdcObjectType.Snapshot.name().toLowerCase(), getSnapshotName());
        }
        return jobProperties;
    }

    /**
     * @return The image snapshots associated with the VM snapshot.
     * Note that the first time this method is run it issues Dao call.
     */
    protected List<DiskImage> getSourceImages() {
        if (_sourceImages == null) {
            _sourceImages = diskImageDao.getAllSnapshotsForVmSnapshot(getParameters().getSnapshotId());
        }
        return _sourceImages;
    }

    @Override
    protected void executeCommand() {
        if (!getVm().isDown() && !getVm().isQualifiedForSnapshotMerge()) {
            log.error("Cannot remove VM snapshot. Vm is not Down, Up or Paused");
            throw new EngineException(EngineError.VM_NOT_QUALIFIED_FOR_SNAPSHOT_MERGE);
        }

        final Snapshot snapshot = snapshotDao.get(getParameters().getSnapshotId());

        boolean snapshotHasImages = hasImages();
        boolean removeSnapshotMemory = isMemoryVolumeRemovable(snapshot);

        // If the VM hasn't got any images and memory - simply remove the snapshot.
        // No need for locking, VDSM tasks, and all that jazz.
        if (!snapshotHasImages && !removeSnapshotMemory) {
            snapshotDao.remove(getParameters().getSnapshotId());
            setSucceeded(true);
            return;
        }

        lockSnapshot(snapshot);
        freeLock();

        getParameters().setEntityInfo(new EntityInfo(VdcObjectType.VM, getVmId()));

        boolean useTaskManagerToRemoveMemory = false;
        if (snapshotHasImages) {
            removeImages();

            if (getSnapshotActionType() == ActionType.RemoveSnapshotSingleDiskLive) {
                persistCommand(getParameters().getParentCommand(), true);
                useTaskManagerToRemoveMemory = true;
            }
        }

        if (removeSnapshotMemory) {
            removeMemory(snapshot, useTaskManagerToRemoveMemory);
            if (!snapshotHasImages) {
                // no async tasks - ending command manually
                endVmCommand();
            }
        }

        setSucceeded(true);
    }

    /**
     * There is a one to many relation between memory volumes and snapshots, so memory
     * volumes should be removed only if the only snapshot that points to them is removed.
     */
    private boolean isMemoryVolumeRemovable(Snapshot snapshot) {
        return snapshot.containsMemory() && snapshotDao.getNumOfSnapshotsByDisks(snapshot) == 1;
    }

    private void removeMemory(final Snapshot snapshot, boolean useTaskManager) {
        RemoveMemoryVolumesParameters parameters = new RemoveMemoryVolumesParameters(snapshot, getVmId());
        if (useTaskManager) {
            commandCoordinatorUtil.executeAsyncCommand(ActionType.RemoveMemoryVolumes, parameters, cloneContextAndDetachFromParent());
        } else {
            ActionReturnValue ret = runInternalAction(ActionType.RemoveMemoryVolumes, parameters);
            if (!ret.getSucceeded()) {
                log.error("Cannot remove memory volumes for snapshot '{}'", snapshot.getId());
            }
        }
    }

    private void removeImages() {
        List<CinderDisk> cinderDisks = new ArrayList<>();
        List <ManagedBlockStorageDisk> managedBlockDisks = new ArrayList<>();
        List<Guid> imageIdsToUnlock = new ArrayList<>();
        for (final DiskImage source : getSourceImages()) {
            imageIdsToUnlock.add(source.getId());

            if (source.getDiskStorageType() == DiskStorageType.MANAGED_BLOCK_STORAGE) {
                managedBlockDisks.add((ManagedBlockStorageDisk) source);
                continue;
            }
            if (source.getDiskStorageType() == DiskStorageType.CINDER) {
                cinderDisks.add((CinderDisk) source);
                continue;
            }

            // The following is ok because we have tested in the validate that the vm
            // is not a template and the vm is not in preview mode and that
            // this is not the active snapshot.
            List<DiskImage> images = diskImageDao.getAllSnapshotsForParent(source.getImageId());
            DiskImage dest = null;
            if (!images.isEmpty()) {
                dest = images.get(0);
            }

            if (getSnapshotActionType() == ActionType.RemoveSnapshotSingleDiskLive) {
                commandCoordinatorUtil.executeAsyncCommand(
                        getSnapshotActionType(),
                        buildRemoveSnapshotSingleDiskParameters(source, dest, getSnapshotActionType()),
                        cloneContextAndDetachFromParent());
            } else {
                RemoveSnapshotSingleDiskParameters parameters = buildRemoveSnapshotSingleDiskParameters(
                        source, dest, getSnapshotActionType());
                ActionReturnValue actionReturnValueturnValue = runInternalActionWithTasksContext(
                        getSnapshotActionType(), parameters);
                getTaskIdList().addAll(actionReturnValueturnValue.getInternalVdsmTaskIdList());
            }

            List<Guid> quotasToRemoveFromCache = new ArrayList<>();
            quotasToRemoveFromCache.add(source.getQuotaId());
            if (dest != null) {
                quotasToRemoveFromCache.add(dest.getQuotaId());
            }
            getQuotaManager().removeQuotaFromCache(getStoragePoolId(), quotasToRemoveFromCache);
        }
        if (!cinderDisks.isEmpty()) {
            handleCinderSnapshotDisks(cinderDisks);
        }

        managedBlockDisks.forEach(this::removeManagedBlockSnapshot);
        getParameters().setImageGroupIds(imageIdsToUnlock);
    }

    private void removeManagedBlockSnapshot(ManagedBlockStorageDisk disk) {
        ImagesContainterParametersBase params = buildRemoveCinderSnapshotDiskParameters(disk);
        params.setImageGroupID(disk.getId());
        params.setEndProcedure(EndProcedure.COMMAND_MANAGED);
        runInternalAction(ActionType.RemoveManagedBlockStorageSnapshot, params);
    }

    private void handleCinderSnapshotDisks(List<CinderDisk> cinderDisks) {
        for (CinderDisk cinderDisk : cinderDisks) {
            ActionReturnValue actionReturnValue = runInternalAction(
                    ActionType.RemoveCinderSnapshotDisk,
                    buildRemoveCinderSnapshotDiskParameters(cinderDisk),
                    cloneContextAndDetachFromParent());
            if (!actionReturnValue.getSucceeded()) {
                log.error("Error removing snapshots for Cinder disk");
            }
        }
    }

    private void lockSnapshot(final Snapshot snapshot) {
        TransactionSupport.executeInNewTransaction(() -> {
            getCompensationContext().snapshotEntityStatus(snapshot);
            snapshotDao.updateStatus(getParameters().getSnapshotId(), SnapshotStatus.LOCKED);
            getCompensationContext().stateChanged();
            if (getParameters().isNeedsLocking()) {
                getSourceImages().forEach(diskImage ->
                        imageDao.updateStatusOfImagesByImageGroupId(diskImage.getId(), ImageStatus.LOCKED));
            }
            return null;
        });
    }

    private RemoveSnapshotSingleDiskParameters buildRemoveSnapshotSingleDiskParameters(final DiskImage source,
            DiskImage dest, ActionType snapshotActionType) {
        RemoveSnapshotSingleDiskParameters parameters =
                new RemoveSnapshotSingleDiskParameters(source.getImageId(), getVmId());
        parameters.setStorageDomainId(source.getStorageIds().get(0));
        parameters.setDestinationImageId(dest != null ? dest.getImageId() : null);
        parameters.setEntityInfo(getParameters().getEntityInfo());
        parameters.setParentParameters(getParameters());
        parameters.setParentCommand(getActionType());
        parameters.setCommandType(snapshotActionType);
        parameters.setVdsId(getVm().getRunOnVds());
        parameters.setEndProcedure(EndProcedure.COMMAND_MANAGED);
        return parameters;
    }

    private ImagesContainterParametersBase buildRemoveCinderSnapshotDiskParameters(DiskImage disk) {
        ImagesContainterParametersBase removeCinderSnapshotParams =
                new ImagesContainterParametersBase(disk.getImageId());
        removeCinderSnapshotParams.setDestinationImageId(disk.getImageId());
        removeCinderSnapshotParams.setStorageDomainId(disk.getStorageIds().get(0));
        removeCinderSnapshotParams.setParentCommand(getActionType());
        removeCinderSnapshotParams.setParentParameters(getParameters());
        return removeCinderSnapshotParams;
    }

    @Override
    protected void endVmCommand() {
        initializeObjectState();
        if (getParameters().getTaskGroupSuccess()) {
            snapshotDao.remove(getParameters().getSnapshotId());
        } else {
            List<String> failedToRemoveDisks = new ArrayList<>();
            Snapshot snapshot = snapshotDao.get(getParameters().getSnapshotId());

            for (ActionParametersBase parameters : getParameters().getImagesParameters()) {
                ImagesContainterParametersBase imagesParams = parameters instanceof ImagesContainterParametersBase ?
                        (ImagesContainterParametersBase) parameters : null;

                if (imagesParams == null) {
                    // Shouldn't happen as for now ImagesParameters list contains only
                    // instances of ImagesContainterParametersBase objects.
                    continue;
                }

                if (imagesParams.getTaskGroupSuccess()) {
                    snapshot = imagesHandler.prepareSnapshotConfigWithoutImageSingleImage(
                            snapshot, imagesParams.getImageId(), ovfManager);
                } else {
                    log.error("Could not delete image '{}' from snapshot '{}'",
                            imagesParams.getImageId(), getParameters().getSnapshotId());

                    DiskImage diskImage = diskImageDao.getSnapshotById(imagesParams.getImageId());
                    failedToRemoveDisks.add(diskImage.getDiskAlias());
                }
            }

            // Remove memory volume and update the dao.
            // Note: on failure, we can treat memory volume deletion as deleting an image
            // and remove it from the snapshot entity (rollback isn't applicable).
            snapshot.setMemoryDiskId(null);
            snapshot.setMetadataDiskId(null);
            snapshotDao.update(snapshot);

            if (!failedToRemoveDisks.isEmpty()) {
                addCustomValue("DiskAliases", StringUtils.join(failedToRemoveDisks, ", "));
                auditLogDirector.log(this, AuditLogType.USER_REMOVE_SNAPSHOT_FINISHED_FAILURE_PARTIAL_SNAPSHOT);
            }

            snapshotDao.updateStatus(getParameters().getSnapshotId(), SnapshotStatus.OK);
        }

        super.endVmCommand();
    }

    /**
     * @return Don't override the child success, we want merged image chains to be so also in the DB, or else we will be
     *         out of sync with the storage and this is not a good situation.
     */
    @Override
    protected boolean overrideChildCommandSuccess() {
        return false;
    }

    @Override
    protected boolean validate() {
        initializeObjectState();

        if (getVm() == null) {
            return failValidation(EngineMessage.ACTION_TYPE_FAILED_VM_NOT_FOUND);
        }

        if (!canRunActionOnNonManagedVm()) {
            return false;
        }

        VmValidator vmValidator = createVmValidator(getVm());
        if (!validate(new StoragePoolValidator(getStoragePool()).existsAndUp()) ||
                !validateVmSnapshotDisksNotDuringMerge() ||
                !validateVmNotInPreview() ||
                !validateSnapshotExists() ||
                !validateSnapshotType() ||
                !validate(vmValidator.vmQualifiedForSnapshotMerge()) ||
                !validate(vmValidator.vmNotHavingDeviceSnapshotsAttachedToOtherVms(false))) {
            return false;
        }

        if (hasImages()) {
            // Check the VM's images
            if (!validateImages()) {
                return false;
            }

            // check that we are not deleting the template
            if (!validateImageNotInTemplate()) {
                return failValidation(EngineMessage.ACTION_TYPE_FAILED_CANNOT_REMOVE_IMAGE_TEMPLATE);
            }

            if (!validateStorageDomains()) {
                return false;
            }
        }

        return true;
    }

    /**
     * Validates the storage domains.
     *
     * Each domain is validated for status, threshold and for enough free space to perform removeSnapshot.
     * The remove snapshot logic in VDSM includes creating a new temporary volume which might be as large as the disk's
     * actual size.
     * Hence, as part of the validation, we sum up all the disks virtual sizes, for each storage domain.
     *
     * @return True if there is enough space in all relevant storage domains. False otherwise.
     */
    private boolean validateStorageDomains() {
        MultipleStorageDomainsValidator storageDomainsValidator = getStorageDomainsValidator(getStoragePoolId(), getStorageDomainsIds());
        if (DisksFilter.filterImageDisks(getSourceImages()).isEmpty()) {
            return true;
        }

        return validate(storageDomainsValidator.allDomainsExistAndActive())
                && validate(storageDomainsValidator.allDomainsWithinThresholds())
                && validate(storageDomainsValidator.allDomainsHaveSpaceForMerge(getAllDisksSnapshot(getSourceImages()), getSnapshotActionType()))
                && validate(storageDomainsValidator.isSupportedByManagedBlockStorageDomains(getActionType()));
    }

    /**
     * The base snapshot is the parent of the top snapshot.
     *
     * @param snapshots list of the parent snapshot disks
     * @return list of subchains which contain the base and top snapshots.
     */
    private List<SubchainInfo> getAllDisksSnapshot(List<DiskImage> snapshots) {
        Set<DiskImage> topSnapshots = diskImageDao.getAllSnapshotsForParents(
                snapshots
                    .stream()
                    .map(DiskImage::getImageId)
                    .collect(Collectors.toList())
        );

        Map<Guid, DiskImage> baseSnapshotMap = snapshots
                .stream()
                .collect(Collectors.toMap(DiskImage::getImageId, Function.identity()));

        return topSnapshots
            .stream()
            .map(topSnapshot -> new SubchainInfo(baseSnapshotMap.get(topSnapshot.getParentId()), topSnapshot))
            .collect(Collectors.toList());
    }

    protected Collection<Guid> getStorageDomainsIds() {
        return ImagesHandler.getAllStorageIdsForImageIds(getSourceImages());
    }

    protected MultipleStorageDomainsValidator getStorageDomainsValidator(Guid spId, Collection<Guid> sdIds) {
        return new MultipleStorageDomainsValidator(spId, sdIds);
    }

    @Override
    protected void setActionMessageParameters() {
        addValidationMessage(EngineMessage.VAR__TYPE__SNAPSHOT);
        addValidationMessage(EngineMessage.VAR__ACTION__REMOVE);
    }

    private boolean validateVmSnapshotDisksNotDuringMerge() {
        return validate(snapshotsValidator.vmSnapshotDisksNotDuringMerge(getVmId(), getParameters().getSnapshotId()));
    }

    private boolean validateVmNotInPreview() {
        return validate(snapshotsValidator.vmNotInPreview(getVmId()));
    }

    private boolean validateSnapshotExists() {
        return validate(snapshotsValidator.snapshotExists(getVmId(), getParameters().getSnapshotId()));
    }

    protected boolean validateSnapshotType() {
        Snapshot snapshot = snapshotDao.get(getParameters().getSnapshotId());
        return validate(snapshotsValidator.isRegularSnapshot(snapshot));
    }

    protected boolean validateImages() {
        List<DiskImage> allDiskImagesInSrcAndDstToValidate = getAllDiskImagesInSrcAndDstToValidate();
        DiskImagesValidator allDiskImagesInChainValidator = new DiskImagesValidator(allDiskImagesInSrcAndDstToValidate);

        return validateImagesNotLocked(allDiskImagesInChainValidator) &&
                (getVm().isQualifiedForLiveSnapshotMerge() || validate(allDiskImagesInChainValidator.diskImagesNotIllegal())) &&
                (!getVm().isQualifiedForLiveSnapshotMerge() || validateSnapshotDisksArePlugged());
    }

    private boolean validateImagesNotLocked(DiskImagesValidator diskImagesValidator) {
        return !getParameters().isNeedsLocking() || validate(diskImagesValidator.diskImagesNotLocked());
    }

    private List<DiskImage> getAllDiskImagesInSrcAndDstToValidate() {
        List<Guid> parentsIds = getSourceImages().stream().map(DiskImage::getImageId).collect(Collectors.toList());
        List<DiskImage> allDiskImages = new ArrayList<>(diskImageDao.getAllSnapshotsForParents(parentsIds));
        allDiskImages.addAll(getSourceImages());
        return allDiskImages;
    }

    private boolean validateSnapshotDisksArePlugged() {
        Map<Guid, Disk> vmDisks = diskDao.getAllForVm(getVmId())
                .stream()
                .collect(Collectors.toMap(Disk::getId, Function.identity()));

        // If there is an unattached disk, it will not be included in vmDisks, hence it is
        // retrieved by the diskDao. This is less likely to happen as it is not possible
        // to unattach disks with snapshots.
        String unpluggedDisks = getSourceImages()
                .stream()
                .map(DiskImage::getId)
                .map(vmDiskId -> vmDisks.getOrDefault(vmDiskId, diskDao.get(vmDiskId)))
                .filter(disk -> !disk.getPlugged())
                .map(Disk::getDiskAlias)
                .collect(Collectors.joining(System.lineSeparator()));

        if (!unpluggedDisks.isEmpty()) {
            return validate(new ValidationResult(EngineMessage.ACTION_TYPE_FAILED_VM_SNAPSHOT_HAS_UNPLUGGED_OR_UNATTACHED_DISKS,
                    String.format("$diskAliases %s", unpluggedDisks)));
        }

        return true;
    }

    protected boolean validateImageNotInTemplate() {
        return vmTemplateDao.get(getRepresentativeSourceImageId()) == null;
    }

    private boolean hasImages() {
        return !getSourceImages().isEmpty();
    }

    private Guid getRepresentativeSourceImageId() {
        return getSourceImages().get(0).getImageId();
    }

    protected VmValidator createVmValidator(VM vm) {
        return new VmValidator(vm);
    }

    @Override
    public AuditLogType getAuditLogTypeValue() {
        switch (getActionState()) {
        case EXECUTE:
            return getSucceeded() ? AuditLogType.USER_REMOVE_SNAPSHOT : AuditLogType.USER_FAILED_REMOVE_SNAPSHOT;

        case END_SUCCESS:
            return getSucceeded() ? AuditLogType.USER_REMOVE_SNAPSHOT_FINISHED_SUCCESS
                    : AuditLogType.USER_REMOVE_SNAPSHOT_FINISHED_FAILURE;

        default:
            return AuditLogType.USER_REMOVE_SNAPSHOT_FINISHED_FAILURE;
        }
    }

    private ActionType getSnapshotActionType() {
        if (getVm().isQualifiedForLiveSnapshotMerge()) {
            return ActionType.RemoveSnapshotSingleDiskLive;
        }
        return ActionType.ColdMergeSnapshotSingleDisk;
    }

    @Override
    protected Map<String, Pair<String, String>> getExclusiveLocks() {
        return getParameters().isNeedsLocking() ?
                Collections.singletonMap(getVmId().toString(),
                        LockMessagesMatchUtil.makeLockingPair(LockingGroup.VM, EngineMessage.ACTION_TYPE_FAILED_OBJECT_LOCKED))
                : null;
    }

    @Override
    public List<QuotaConsumptionParameter> getQuotaStorageConsumptionParameters() {
        //return empty list - the command only release quota so it could never fail the quota check
        return new ArrayList<>();
    }

    @Override
    public CommandCallback getCallback() {
        VM vm = getVm();
        if (vm == null) {
            // We are getting here probably due to a fail of merge process of a VM that already deleted.
            log.warn("The VM that was involved in the merge process doesn't exists anymore. Please cleanup the command_entities table in the database.");
            return null;
        }

        if (vm.isQualifiedForLiveSnapshotMerge() || getParameters().isUseCinderCommandCallback()
                || getSnapshotActionType() == ActionType.ColdMergeSnapshotSingleDisk) {
            return callbackProvider.get();
        }
        return null;
    }

    @Override
    public void endWithFailure() {
        // Some Live Merge failure cases leave a subset of images illegal;
        // They should remain illegal while the other images on the chain are unlocked.
        getSourceImages().stream()
                .map(disk -> diskImageDao.getAllSnapshotsForImageGroup(disk.getId()))
                .forEach(snapshotDisks -> {
                    snapshotDisks.stream()
                            .filter(snapshotDisk -> snapshotDisk.getImageStatus() == ImageStatus.LOCKED)
                            .forEach(snapshotDisk -> imageDao.updateStatus(snapshotDisk.getImageId(), ImageStatus.OK));
                });
        super.endWithFailure();
    }

    @Override
    public void endSuccessfully() {
        getParameters().getImageGroupIds().forEach(id -> {
            imageDao.updateStatusOfImagesByImageGroupId(id, ImageStatus.OK);
        });
        super.endSuccessfully();
    }
}
