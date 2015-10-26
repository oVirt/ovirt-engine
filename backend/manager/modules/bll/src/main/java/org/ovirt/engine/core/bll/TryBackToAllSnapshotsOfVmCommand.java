package org.ovirt.engine.core.bll;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.bll.snapshots.SnapshotsManager;
import org.ovirt.engine.core.bll.snapshots.SnapshotsValidator;
import org.ovirt.engine.core.bll.storage.CINDERStorageHelper;
import org.ovirt.engine.core.bll.tasks.CommandCoordinatorUtil;
import org.ovirt.engine.core.bll.validator.VmValidator;
import org.ovirt.engine.core.bll.validator.storage.CinderDisksValidator;
import org.ovirt.engine.core.bll.validator.storage.DiskImagesValidator;
import org.ovirt.engine.core.bll.validator.storage.DiskSnapshotsValidator;
import org.ovirt.engine.core.bll.validator.storage.MultipleStorageDomainsValidator;
import org.ovirt.engine.core.bll.validator.storage.StoragePoolValidator;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.FeatureSupported;
import org.ovirt.engine.core.common.VdcObjectType;
import org.ovirt.engine.core.common.action.CloneCinderDisksParameters;
import org.ovirt.engine.core.common.action.ImagesContainterParametersBase;
import org.ovirt.engine.core.common.action.LockProperties;
import org.ovirt.engine.core.common.action.LockProperties.Scope;
import org.ovirt.engine.core.common.action.TryBackToAllSnapshotsOfVmParameters;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.action.VdcReturnValueBase;
import org.ovirt.engine.core.common.asynctasks.EntityInfo;
import org.ovirt.engine.core.common.businessentities.Snapshot;
import org.ovirt.engine.core.common.businessentities.Snapshot.SnapshotStatus;
import org.ovirt.engine.core.common.businessentities.Snapshot.SnapshotType;
import org.ovirt.engine.core.common.businessentities.storage.CinderDisk;
import org.ovirt.engine.core.common.businessentities.storage.DiskImage;
import org.ovirt.engine.core.common.businessentities.storage.DiskStorageType;
import org.ovirt.engine.core.common.errors.EngineError;
import org.ovirt.engine.core.common.errors.EngineException;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.common.locks.LockingGroup;
import org.ovirt.engine.core.common.utils.Pair;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;
import org.ovirt.engine.core.utils.transaction.TransactionMethod;
import org.ovirt.engine.core.utils.transaction.TransactionSupport;

@NonTransactiveCommandAttribute(forceCompensation = true)
public class TryBackToAllSnapshotsOfVmCommand<T extends TryBackToAllSnapshotsOfVmParameters> extends VmCommand<T> {

    private final SnapshotsManager snapshotsManager = new SnapshotsManager();
    private Snapshot cachedSnapshot;
    private List<DiskImage> imagesToPreview;

    /**
     * Constructor for command creation when compensation is applied on startup
     * @param commandId
     */
    protected TryBackToAllSnapshotsOfVmCommand(Guid commandId) {
        super(commandId);
    }

    public TryBackToAllSnapshotsOfVmCommand(T parameters) {
        this(parameters, null);
    }

    public TryBackToAllSnapshotsOfVmCommand(T parameters, CommandContext commandContext) {
        super(parameters, commandContext);
        parameters.setEntityInfo(new EntityInfo(VdcObjectType.VM, getVmId()));
    }

    @Override
    protected LockProperties applyLockProperties(LockProperties lockProperties) {
        return lockProperties.withScope(Scope.Execution);
    }

    @Override
    public Map<String, String> getJobMessageProperties() {
        if (jobProperties == null) {
            jobProperties = super.getJobMessageProperties();
            if (getSnapshotName() != null) {
                jobProperties.put(VdcObjectType.Snapshot.name().toLowerCase(), getSnapshotName());
            }
        }
        return jobProperties;
    }

    @Override
    protected void endWithFailure() {
        Snapshot previouslyActiveSnapshot =
                getSnapshotDao().get(getVmId(), SnapshotType.PREVIEW, SnapshotStatus.LOCKED);
        getSnapshotDao().remove(previouslyActiveSnapshot.getId());
        getSnapshotDao().remove(getSnapshotDao().getId(getVmId(), SnapshotType.ACTIVE));

        snapshotsManager.addActiveSnapshot(previouslyActiveSnapshot.getId(), getVm(),
                previouslyActiveSnapshot.getMemoryVolume(), getCompensationContext());

        super.endWithFailure();
    }

    @Override
    protected void endSuccessfully() {
        getVmStaticDao().incrementDbGeneration(getVm().getId());
        endActionOnDisks();

        if (getVm() != null) {
            VmHandler.unlockVm(getVm(), getCompensationContext());
            restoreVmConfigFromSnapshot();
        } else {
            setCommandShouldBeLogged(false);
            log.warn("VmCommand::EndVmCommand: Vm is null - not performing endAction on Vm");
        }

        setSucceeded(true);
    }

    private void restoreVmConfigFromSnapshot() {
        getSnapshotDao().updateStatus(getParameters().getDstSnapshotId(), SnapshotStatus.IN_PREVIEW);
        getSnapshotDao().updateStatus(getSnapshotDao().getId(getVm().getId(),
                SnapshotType.PREVIEW,
                SnapshotStatus.LOCKED),
                SnapshotStatus.OK);

        snapshotsManager.attempToRestoreVmConfigurationFromSnapshot(getVm(),
                getDstSnapshot(),
                getSnapshotDao().getId(getVm().getId(), SnapshotType.ACTIVE),
                getImagesToPreview(),
                getCompensationContext(), getVm().getVdsGroupCompatibilityVersion(), getCurrentUser());
    }

    @Override
    protected void executeVmCommand() {
        final boolean restoreMemory = getParameters().isRestoreMemory() &&
                FeatureSupported.memorySnapshot(getVm().getVdsGroupCompatibilityVersion()) &&
                FeatureSupported.isMemorySnapshotSupportedByArchitecture(
                        getVm().getClusterArch(),
                        getVm().getVdsGroupCompatibilityVersion());

        final Guid newActiveSnapshotId = Guid.newGuid();
        final Snapshot snapshotToBePreviewed = getDstSnapshot();

        final Snapshot previousActiveSnapshot = getSnapshotDao().get(getVmId(), SnapshotType.ACTIVE);
        final Guid previousActiveSnapshotId = previousActiveSnapshot.getId();

        final List<DiskImage> images = getImagesToPreview();

        // Images list without those that are excluded from preview
        final List<DiskImage> filteredImages = (List<DiskImage>) CollectionUtils.subtract(
                images, getImagesExcludedFromPreview(images, previousActiveSnapshotId, newActiveSnapshotId));
        final List<CinderDisk> cinderDisks = new ArrayList<>();
        TransactionSupport.executeInNewTransaction(new TransactionMethod<Void>() {
            @Override
            public Void runInTransaction() {
                getCompensationContext().snapshotEntity(previousActiveSnapshot);
                getSnapshotDao().remove(previousActiveSnapshotId);
                snapshotsManager.addSnapshot(previousActiveSnapshotId,
                        "Active VM before the preview",
                        SnapshotType.PREVIEW,
                        getVm(),
                        previousActiveSnapshot.getMemoryVolume(),
                        getCompensationContext());
                snapshotsManager.addActiveSnapshot(newActiveSnapshotId,
                        getVm(),
                        restoreMemory ? snapshotToBePreviewed.getMemoryVolume() : StringUtils.EMPTY,
                        images,
                        getCompensationContext());

                // if there are no images there's no reason to save the compensation data to DB as the update is
                // being executed in the same transaction so we can restore the vm config and end the command.
                if (!filteredImages.isEmpty()) {
                    getCompensationContext().stateChanged();
                } else {
                    getVmStaticDao().incrementDbGeneration(getVm().getId());
                    restoreVmConfigFromSnapshot();
                }
                return null;
            }
        });

        if (!filteredImages.isEmpty()) {
            VmHandler.lockVm(getVm().getDynamicData(), getCompensationContext());
            freeLock();
            TransactionSupport.executeInNewTransaction(new TransactionMethod<Void>() {
                @Override
                public Void runInTransaction() {
                    for (DiskImage image : filteredImages) {
                        if (image.getDiskStorageType() == DiskStorageType.CINDER) {
                            cinderDisks.add((CinderDisk)image);
                            continue;
                        }
                        VdcReturnValueBase vdcReturnValue =
                                runInternalActionWithTasksContext(VdcActionType.TryBackToSnapshot,
                                        buildTryBackToSnapshotParameters(newActiveSnapshotId, image));

                        if (vdcReturnValue.getSucceeded()) {
                            getTaskIdList().addAll(vdcReturnValue.getInternalVdsmTaskIdList());
                        } else if (vdcReturnValue.getFault() != null) {
                            // if we have a fault, forward it to the user
                            throw new EngineException(vdcReturnValue.getFault().getError(),
                                    vdcReturnValue.getFault().getMessage());
                        } else {
                            log.error("Cannot create snapshot");
                            throw new EngineException(EngineError.IRS_IMAGE_STATUS_ILLEGAL);
                        }
                    }
                    if (!cinderDisks.isEmpty() &&
                            !tryBackAllCinderDisks(cinderDisks, newActiveSnapshotId)) {
                        throw new EngineException(EngineError.CINDER_ERROR, "Failed to preview a snapshot!");
                    }
                    return null;
                }

                private ImagesContainterParametersBase buildTryBackToSnapshotParameters(
                        final Guid newActiveSnapshotId, DiskImage image) {
                    ImagesContainterParametersBase params = new ImagesContainterParametersBase(image.getImageId());
                    params.setParentCommand(VdcActionType.TryBackToAllSnapshotsOfVm);
                    params.setVmSnapshotId(newActiveSnapshotId);
                    params.setEntityInfo(getParameters().getEntityInfo());
                    params.setParentParameters(getParameters());
                    params.setQuotaId(image.getQuotaId());
                    return params;
                }
            });
        }
        setSucceeded(true);
    }

    protected boolean tryBackAllCinderDisks( List<CinderDisk> cinderDisks, Guid newSnapshotId) {
        Future<VdcReturnValueBase> future = CommandCoordinatorUtil.executeAsyncCommand(
                VdcActionType.TryBackToAllCinderSnapshots,
                buildCinderChildCommandParameters(cinderDisks, newSnapshotId),
                cloneContextAndDetachFromParent(),
                CINDERStorageHelper.getStorageEntities(cinderDisks));
        try {
            VdcReturnValueBase vdcReturnValueBase = future.get();
            if (!vdcReturnValueBase.getSucceeded()) {
                getReturnValue().setFault(vdcReturnValueBase.getFault());
                log.error("Error preview snapshot");
                return false;
            }
        } catch (InterruptedException | ExecutionException e) {
            log.error("Error cloning Cinder disks for preview snapshot", e);
            return false;
        }
        return true;
    }

    private CloneCinderDisksParameters buildCinderChildCommandParameters(List<CinderDisk> cinderDisks, Guid newSnapshotId) {
        CloneCinderDisksParameters createParams = new CloneCinderDisksParameters();
        createParams.setCinderDisks(cinderDisks);
        createParams.setVmSnapshotId(newSnapshotId);
        createParams.setParentHasTasks(!getReturnValue().getVdsmTaskIdList().isEmpty());
        return withRootCommandInfo(createParams);
    }

    private List<DiskImage> getImagesToPreview() {
        if (imagesToPreview == null) {
            imagesToPreview = getParameters().getDisks() != null ? getParameters().getDisks() :
                    getDbFacade().getDiskImageDao().getAllSnapshotsForVmSnapshot(getDstSnapshot().getId());

            // Filter out shareable/nonsnapable disks
            List<CinderDisk> CinderImagesToPreview = ImagesHandler.filterDisksBasedOnCinder(imagesToPreview);
            imagesToPreview = ImagesHandler.filterImageDisks(imagesToPreview, true, true, false);
            imagesToPreview.addAll(CinderImagesToPreview);
        }
        return imagesToPreview;
    }

    /**
     * Returns the list of images that haven't been selected for preview (remain the images from current active VM).
     */
    private List<DiskImage> getImagesExcludedFromPreview(List<DiskImage> images, Guid previousActiveSnapshotId, Guid newActiveSnapshotId) {
        List<DiskImage> excludedImages = new ArrayList<>();

        for (DiskImage image : images) {
            if (image.getDiskStorageType().isInternal() && image.getVmSnapshotId().equals(previousActiveSnapshotId)) {
                // Image is already active, hence only update snapshot ID.
                getImageDao().updateImageVmSnapshotId(image.getImageId(), newActiveSnapshotId);
                excludedImages.add(image);
            }
        }

        return excludedImages;
    }

    @Override
    public AuditLogType getAuditLogTypeValue() {
        switch (getActionState()) {
        case EXECUTE:
            return getSucceeded() ? AuditLogType.USER_TRY_BACK_TO_SNAPSHOT
                    : AuditLogType.USER_FAILED_TRY_BACK_TO_SNAPSHOT;

        case END_SUCCESS:
            return getSucceeded() ? AuditLogType.USER_TRY_BACK_TO_SNAPSHOT_FINISH_SUCCESS
                    : AuditLogType.USER_TRY_BACK_TO_SNAPSHOT_FINISH_FAILURE;

        default:
            return AuditLogType.USER_TRY_BACK_TO_SNAPSHOT_FINISH_FAILURE;
        }
    }

    @Override
    protected boolean canDoAction() {
        if (Guid.Empty.equals(getParameters().getDstSnapshotId())) {
            return failCanDoAction(EngineMessage.ACTION_TYPE_FAILED_CORRUPTED_VM_SNAPSHOT_ID);
        }
        SnapshotsValidator snapshotsValidator = new SnapshotsValidator();
        VmValidator vmValidator = new VmValidator(getVm());
        if (!validate(vmValidator.vmDown())
                || !validate(snapshotsValidator.snapshotExists(getVmId(), getParameters().getDstSnapshotId()))
                || !validate(snapshotsValidator.vmNotDuringSnapshot(getVmId()))
                || !validate(snapshotsValidator.vmNotInPreview(getVmId()))) {
            return false;
        }

        updateVmDisksFromDb();
        List<DiskImage> diskImages =
                ImagesHandler.filterImageDisks(getVm().getDiskMap().values(), true, true, true);
        diskImages.addAll(ImagesHandler.filterDisksBasedOnCinder(getVm().getDiskMap().values(), true));
        if (!diskImages.isEmpty()) {
          if (!validate(new StoragePoolValidator(getStoragePool()).isUp())) {
              return false;
          }

          DiskImagesValidator diskImagesValidator = new DiskImagesValidator(diskImages);
            if (!validate(diskImagesValidator.diskImagesNotIllegal()) ||
                    !validate(diskImagesValidator.diskImagesNotLocked())) {
              return false;
          }

          DiskImagesValidator diskImagesToPreviewValidator = new DiskImagesValidator(getImagesToPreview());
          if (!validate(diskImagesToPreviewValidator.diskImagesNotIllegal()) ||
                  !validate(diskImagesToPreviewValidator.diskImagesNotLocked())) {
              return false;
          }

          Set<Guid> storageIds = ImagesHandler.getAllStorageIdsForImageIds(diskImages);
          MultipleStorageDomainsValidator storageValidator =
                    new MultipleStorageDomainsValidator(getVm().getStoragePoolId(), storageIds);
            if (!validate(new StoragePoolValidator(getStoragePool()).isUp())
                    || !validate(storageValidator.allDomainsExistAndActive())
                    || !validate(storageValidator.allDomainsWithinThresholds())
                    || !validateCinder()) {
                return false;
            }
        }

        DiskSnapshotsValidator diskSnapshotsValidator = new DiskSnapshotsValidator(getParameters().getDisks());
        if (!validate(diskSnapshotsValidator.canDiskSnapshotsBePreviewed(getParameters().getDstSnapshotId()))) {
            return false;
        }

        return true;
    }

    public boolean validateCinder() {
        List<CinderDisk> cinderDisks = ImagesHandler.filterDisksBasedOnCinder(DbFacade.getInstance().getDiskDao().getAllForVm(getVmId()));
        if (!cinderDisks.isEmpty()) {
            CinderDisksValidator cinderDisksValidator = getCinderDisksValidator(cinderDisks);
            return validate(cinderDisksValidator.validateCinderDiskLimits());
        }
        return true;
    }

    protected CinderDisksValidator getCinderDisksValidator(List<CinderDisk> cinderDisks) {
        return new CinderDisksValidator(cinderDisks);
    }

    private Snapshot getDstSnapshot() {
        if (cachedSnapshot == null) {
            cachedSnapshot = getSnapshotDao().get(getParameters().getDstSnapshotId());
        }
        return cachedSnapshot;
    }

    @Override
    protected void setActionMessageParameters() {
        addCanDoActionMessage(EngineMessage.VAR__ACTION__PREVIEW);
        addCanDoActionMessage(EngineMessage.VAR__TYPE__SNAPSHOT);
    }

    protected void updateVmDisksFromDb() {
        VmHandler.updateDisksFromDb(getVm());
    }

    @Override
    protected VdcActionType getChildActionType() {
        return VdcActionType.TryBackToSnapshot;
    }

    @Override
    protected Map<String, Pair<String, String>> getExclusiveLocks() {
        return Collections.singletonMap(getVmId().toString(),
                LockMessagesMatchUtil.makeLockingPair(LockingGroup.VM, EngineMessage.ACTION_TYPE_FAILED_OBJECT_LOCKED));
    }

    @Override
    public String getSnapshotName() {
        if (super.getSnapshotName() == null) {
            final Snapshot snapshot = getDstSnapshot();
            if (snapshot != null) {
                setSnapshotName(snapshot.getDescription());
            }
        }

        return super.getSnapshotName();
    }

}
