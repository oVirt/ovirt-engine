package org.ovirt.engine.core.bll.snapshots;

import static org.ovirt.engine.core.bll.storage.disk.image.DisksFilter.ONLY_ACTIVE;
import static org.ovirt.engine.core.bll.storage.disk.image.DisksFilter.ONLY_NOT_SHAREABLE;
import static org.ovirt.engine.core.bll.storage.disk.image.DisksFilter.ONLY_PLUGGED;
import static org.ovirt.engine.core.bll.storage.disk.image.DisksFilter.ONLY_SNAPABLE;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import javax.enterprise.inject.Instance;
import javax.enterprise.inject.Typed;
import javax.inject.Inject;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.ovirt.engine.core.bll.ConcurrentChildCommandsExecutionCallback;
import org.ovirt.engine.core.bll.LockMessagesMatchUtil;
import org.ovirt.engine.core.bll.NonTransactiveCommandAttribute;
import org.ovirt.engine.core.bll.UpdateVmCommand;
import org.ovirt.engine.core.bll.VmCommand;
import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.bll.job.ExecutionHandler;
import org.ovirt.engine.core.bll.network.VmInterfaceManager;
import org.ovirt.engine.core.bll.storage.disk.image.DisksFilter;
import org.ovirt.engine.core.bll.storage.disk.image.ImagesHandler;
import org.ovirt.engine.core.bll.storage.ovfstore.OvfHelper;
import org.ovirt.engine.core.bll.tasks.CommandCoordinatorUtil;
import org.ovirt.engine.core.bll.tasks.interfaces.CommandCallback;
import org.ovirt.engine.core.bll.validator.VmValidator;
import org.ovirt.engine.core.bll.validator.storage.CinderDisksValidator;
import org.ovirt.engine.core.bll.validator.storage.DiskImagesValidator;
import org.ovirt.engine.core.bll.validator.storage.DiskSnapshotsValidator;
import org.ovirt.engine.core.bll.validator.storage.MultipleStorageDomainsValidator;
import org.ovirt.engine.core.bll.validator.storage.StoragePoolValidator;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.FeatureSupported;
import org.ovirt.engine.core.common.VdcObjectType;
import org.ovirt.engine.core.common.action.ActionReturnValue;
import org.ovirt.engine.core.common.action.ActionType;
import org.ovirt.engine.core.common.action.CreateCinderSnapshotParameters;
import org.ovirt.engine.core.common.action.CreateManagedBlockStorageDiskSnapshotParameters;
import org.ovirt.engine.core.common.action.ImagesContainterParametersBase;
import org.ovirt.engine.core.common.action.LockProperties;
import org.ovirt.engine.core.common.action.LockProperties.Scope;
import org.ovirt.engine.core.common.action.RestoreAllSnapshotsParameters;
import org.ovirt.engine.core.common.action.TryBackToAllSnapshotsOfVmParameters;
import org.ovirt.engine.core.common.action.TryBackToAllSnapshotsOfVmParameters.LeaseAction;
import org.ovirt.engine.core.common.action.VmManagementParametersBase;
import org.ovirt.engine.core.common.asynctasks.EntityInfo;
import org.ovirt.engine.core.common.businessentities.Snapshot;
import org.ovirt.engine.core.common.businessentities.Snapshot.SnapshotStatus;
import org.ovirt.engine.core.common.businessentities.Snapshot.SnapshotType;
import org.ovirt.engine.core.common.businessentities.SnapshotActionEnum;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VmStatic;
import org.ovirt.engine.core.common.businessentities.storage.CinderDisk;
import org.ovirt.engine.core.common.businessentities.storage.DiskImage;
import org.ovirt.engine.core.common.businessentities.storage.DiskStorageType;
import org.ovirt.engine.core.common.businessentities.storage.ManagedBlockStorageDisk;
import org.ovirt.engine.core.common.errors.EngineError;
import org.ovirt.engine.core.common.errors.EngineException;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.common.locks.LockingGroup;
import org.ovirt.engine.core.common.utils.Pair;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.Version;
import org.ovirt.engine.core.dao.DiskDao;
import org.ovirt.engine.core.dao.DiskImageDao;
import org.ovirt.engine.core.dao.ImageDao;
import org.ovirt.engine.core.dao.SnapshotDao;
import org.ovirt.engine.core.dao.VmStaticDao;
import org.ovirt.engine.core.utils.OvfUtils;
import org.ovirt.engine.core.utils.lock.EngineLock;
import org.ovirt.engine.core.utils.lock.LockManager;
import org.ovirt.engine.core.utils.ovf.OvfReaderException;
import org.ovirt.engine.core.utils.transaction.TransactionMethod;
import org.ovirt.engine.core.utils.transaction.TransactionSupport;

@NonTransactiveCommandAttribute(forceCompensation = true)
public class TryBackToAllSnapshotsOfVmCommand<T extends TryBackToAllSnapshotsOfVmParameters> extends VmCommand<T> {

    private Snapshot cachedSnapshot;
    private List<DiskImage> imagesToPreview;

    @Inject
    private LockManager lockManager;
    @Inject
    private OvfHelper ovfHelper;
    @Inject
    private VmStaticDao vmStaticDao;
    @Inject
    private DiskImageDao diskImageDao;
    @Inject
    private ImageDao imageDao;
    @Inject
    private DiskDao diskDao;
    @Inject
    private SnapshotDao snapshotDao;
    @Inject
    private CommandCoordinatorUtil commandCoordinatorUtil;
    @Inject
    @Typed(ConcurrentChildCommandsExecutionCallback.class)
    private Instance<ConcurrentChildCommandsExecutionCallback> callbackProvider;

    /**
     * Constructor for command creation when compensation is applied on startup
     */
    public TryBackToAllSnapshotsOfVmCommand(Guid commandId) {
        super(commandId);
    }

    public TryBackToAllSnapshotsOfVmCommand(T parameters, CommandContext commandContext) {
        super(parameters, commandContext);
        parameters.setEntityInfo(new EntityInfo(VdcObjectType.VM, getVmId()));
    }

    @Override
    public void init() {
        // No need to filter the images for partial preview as being done in the execute phase since the callback can
        // also support no child commands, this should be changed once all commands will facilitate the CoCo
        // infrastructure.
        getParameters().setUseCinderCommandCallback(!DisksFilter.filterCinderDisks(getImagesToPreview()).isEmpty() ||
                !DisksFilter.filterManagedBlockStorageDisks(getImagesToPreview()).isEmpty());
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
                snapshotDao.get(getVmId(), SnapshotType.PREVIEW, SnapshotStatus.LOCKED);
        snapshotDao.remove(previouslyActiveSnapshot.getId());
        snapshotDao.remove(snapshotDao.getId(getVmId(), SnapshotType.ACTIVE));

        getSnapshotsManager().addActiveSnapshot(previouslyActiveSnapshot.getId(),
                getVm(),
                SnapshotStatus.OK,
                previouslyActiveSnapshot.getMemoryDiskId(),
                previouslyActiveSnapshot.getMetadataDiskId(),
                getCompensationContext());

        super.endWithFailure();
    }

    @Override
    protected void endSuccessfully() {
        endActionOnDisks();
        vmStaticDao.incrementDbGeneration(getVm().getId());

        boolean succeeded = false;
        if (getVm() != null) {
            vmHandler.unlockVm(getVm(), getCompensationContext());
            try {
                restoreVmConfigFromSnapshot();
                // disks and configuration is restored, let's set CCV if the snapshot originates in older Cluster version
                if (!updateClusterCompatibilityVersionToOldCluster(false)) {
                    log.warn("Failed to set the Cluster Compatibility Version to the cluster version the snapshot originates from.");
                }
                succeeded = true;
            } catch (EngineException ex) {
                getReturnValue().setEndActionTryAgain(false);
                log.error("Unable to restore VM configuration from snapshot: {}, undoing preview.",
                        ExceptionUtils.getRootCauseMessage(ex));
                commandCoordinatorUtil.executeAsyncCommand(ActionType.RestoreAllSnapshots,
                        new RestoreAllSnapshotsParameters(getVm().getId(), SnapshotActionEnum.UNDO),
                        CommandContext.createContext(getParameters().getSessionId()));
            }
        } else {
            setCommandShouldBeLogged(false);
            log.warn("VM is null, not performing endAction");
            succeeded = true;
        }

        setSucceeded(succeeded);
    }

    private boolean canRestoreVmConfigFromSnapshot() {
        return getSnapshotsManager().canRestoreVmConfigurationFromSnapshot(getVm(),
                getDstSnapshot(),
                new VmInterfaceManager(getMacPool()));
    }

    private void restoreVmConfigFromSnapshot() {
        snapshotDao.updateStatus(getParameters().getDstSnapshotId(), SnapshotStatus.IN_PREVIEW);
        snapshotDao.updateStatus(snapshotDao.getId(getVm().getId(),
                SnapshotType.PREVIEW,
                SnapshotStatus.LOCKED),
                SnapshotStatus.OK);

        Guid activeSnapshotLeaseDomainId = null;
        if (isLeaseDomainIdUpdateNeeded()) {
            activeSnapshotLeaseDomainId = getVm().getStaticData().getLeaseStorageDomainId();
        }

        getSnapshotsManager().attemptToRestoreVmConfigurationFromSnapshot(getVm(),
                getDstSnapshot(),
                snapshotDao.getId(getVm().getId(), SnapshotType.ACTIVE),
                getFilteredImagesToPreview(),
                getCompensationContext(),
                getCurrentUser(),
                new VmInterfaceManager(getMacPool()),
                isRestoreMemory());

        // custom preview - without leases
        if (!getParameters().isRestoreLease()) {
            vmStaticDao.updateVmLeaseStorageDomainId(getVm().getId(), null);
            return;
        }

        Guid dstLeaseStorageDomainId = OvfUtils.fetchLeaseDomainId(getDstSnapshot().getVmConfiguration());
        if (isLeaseDomainIdUpdateNeeded()) {
            // previewed snapshot and active have leases on different domains, use the active snapshot lease
            vmStaticDao.updateVmLeaseStorageDomainId(getVm().getId(), activeSnapshotLeaseDomainId);
        } else if (getParameters().getDstLeaseDomainId() != null &&
                !getParameters().getDstLeaseDomainId().equals(dstLeaseStorageDomainId)) {
            // custom preview - use the other snapshot lease.
            // if the given destination lease domain ID is not equals to the previewed snapshot lease domain ID
            // it means that the preview uses other lease domain ID that should set instead of the previewed snapshot
            // lease domain ID
            vmStaticDao.updateVmLeaseStorageDomainId(getVm().getId(), getParameters().getDstLeaseDomainId());
        }
    }

    @Override
    protected void executeVmCommand() {
        final boolean restoreMemory = isRestoreMemory();

        final Guid newActiveSnapshotId = Guid.newGuid();
        final Snapshot snapshotToBePreviewed = getDstSnapshot();

        final Snapshot previousActiveSnapshot = snapshotDao.get(getVmId(), SnapshotType.ACTIVE);
        final Guid previousActiveSnapshotId = previousActiveSnapshot.getId();

        final List<DiskImage> images = getImagesToPreview();

        // Images list without those that are excluded from preview
        final List<DiskImage> filteredImages = (List<DiskImage>) CollectionUtils.subtract(
                images, getImagesExcludedFromPreview(images, previousActiveSnapshotId, newActiveSnapshotId));

        if (log.isInfoEnabled()) {
            log.info("Previewing snapshot {} with the disks:\n{}", getSnapshotName(),
                    filteredImages.stream()
                            .map(disk -> String.format("%s (%s) to imageId %s",
                                    disk.getName(), disk.getId().toString(), disk.getImageId().toString()))
                                    .collect(Collectors.joining("\n")));
        }

        final List<CinderDisk> cinderDisks = new ArrayList<>();
        TransactionSupport.executeInNewTransaction(() -> {
            getCompensationContext().snapshotEntity(previousActiveSnapshot);
            snapshotDao.remove(previousActiveSnapshotId);
            getSnapshotsManager().addSnapshot(previousActiveSnapshotId,
                    "Active VM before the preview",
                    SnapshotStatus.LOCKED,
                    SnapshotType.PREVIEW,
                    getVm(),
                    true,
                    null,
                    previousActiveSnapshot.getMemoryDiskId(),
                    previousActiveSnapshot.getMetadataDiskId(),
                    null,
                    null,
                    null,
                    getCompensationContext());
            getSnapshotsManager().addActiveSnapshot(newActiveSnapshotId,
                    getVm(),
                    SnapshotStatus.OK,
                    restoreMemory ? snapshotToBePreviewed.getMemoryDiskId() : null,
                    restoreMemory ? snapshotToBePreviewed.getMetadataDiskId() : null,
                    snapshotToBePreviewed.getCreationDate(),
                    images,
                    getCompensationContext());

            // if there are no images there's no reason to save the compensation data to DB as the update is
            // being executed in the same transaction so we can restore the vm config and end the command.
            if (!filteredImages.isEmpty()) {
                getCompensationContext().stateChanged();
            } else {
                vmStaticDao.incrementDbGeneration(getVm().getId());
                restoreVmConfigFromSnapshot();
            }
            return null;
        });

        initializeSnapshotsLeasesParams();
        final List<ManagedBlockStorageDisk> managedBlockStorageDisks = new ArrayList<>();

        if (!filteredImages.isEmpty()) {
            vmHandler.lockVm(getVm().getDynamicData(), getCompensationContext());
            freeLock();
            TransactionSupport.executeInNewTransaction(new TransactionMethod<Void>() {
                @Override
                public Void runInTransaction() {
                    for (DiskImage image : filteredImages) {
                        if (image.getDiskStorageType() == DiskStorageType.MANAGED_BLOCK_STORAGE) {
                            managedBlockStorageDisks.add((ManagedBlockStorageDisk) image);
                            continue;
                        }
                        if (image.getDiskStorageType() == DiskStorageType.CINDER) {
                            cinderDisks.add((CinderDisk)image);
                            continue;
                        }
                        ActionReturnValue actionReturnValue =
                                runInternalActionWithTasksContext(ActionType.TryBackToSnapshot,
                                        buildTryBackToSnapshotParameters(newActiveSnapshotId, image));

                        if (actionReturnValue.getSucceeded()) {
                            getTaskIdList().addAll(actionReturnValue.getInternalVdsmTaskIdList());
                        } else if (actionReturnValue.getFault() != null) {
                            // if we have a fault, forward it to the user
                            throw new EngineException(actionReturnValue.getFault().getError(),
                                    actionReturnValue.getFault().getMessage());
                        } else {
                            log.error("Cannot create snapshot");
                            throw new EngineException(EngineError.IRS_IMAGE_STATUS_ILLEGAL);
                        }
                    }

                    if (getParameters().getLeaseAction() == LeaseAction.CREATE_NEW_LEASE) {
                        if (!addVmLease(getParameters().getDstLeaseDomainId(), getVm().getId(), false)) {
                            log.error("Failed to create lease for VM '{}' on storage domain '{}'",
                                    getVm().getName(),
                                    getParameters().getDstLeaseDomainId());
                            throw new EngineException(EngineError.FailedToCreateLease);
                        }
                    }

                    if (!cinderDisks.isEmpty() &&
                            !tryBackAllCinderDisks(cinderDisks, newActiveSnapshotId)) {
                        throw new EngineException(EngineError.CINDER_ERROR, "Failed to preview a snapshot!");
                    }

                    managedBlockStorageDisks.forEach(disk -> tryBackToManagedBlockSnapshot(disk, newActiveSnapshotId));

                    return null;
                }

                private ImagesContainterParametersBase buildTryBackToSnapshotParameters(
                        final Guid newActiveSnapshotId, DiskImage image) {
                    ImagesContainterParametersBase params = new ImagesContainterParametersBase(image.getImageId());
                    params.setParentCommand(ActionType.TryBackToAllSnapshotsOfVm);
                    params.setVmSnapshotId(newActiveSnapshotId);
                    params.setEntityInfo(getParameters().getEntityInfo());
                    params.setParentParameters(getParameters());
                    params.setQuotaId(image.getQuotaId());
                    return params;
                }
            });
        } else {
            // if there are no disks to restore, no compensation context is saved and the VM Configuration
            // (including clusterCompatibilityVersionOrigin) is already restored at this point. Otherwise,
            // if disks are being restored, the VM Configuration is restored later in endSuccessfully()
            updateClusterCompatibilityVersionToOldCluster(true);
        }

        setSucceeded(true);
    }

    private void tryBackToManagedBlockSnapshot(DiskImage image, Guid newActiveSnapshotId) {
        CreateManagedBlockStorageDiskSnapshotParameters params =
                new CreateManagedBlockStorageDiskSnapshotParameters();
        params.setVolumeId(image.getId());
        params.setImageId(image.getImageId());
        params.setVmSnapshotId(newActiveSnapshotId);
        params.setStorageDomainId(image.getStorageIds().get(0));
        params.setParentCommand(getActionType());
        params.setParentParameters(getParameters());
        runInternalAction(ActionType.TryBackToManagedBlockSnapshot, params);
    }

    private void initializeSnapshotsLeasesParams() {
        if (getParameters().isRestoreLease()) {
            Guid activeSnapshotLeaseDomainId = getVm().getStaticData().getLeaseStorageDomainId();
            Guid selectedLeaseDomainId = getParameters().getDstLeaseDomainId();
            LeaseAction leaseAction = null;

            if (selectedLeaseDomainId == null && getDstSnapshot().getVmConfiguration() != null) {
                // save the destination lease domain id and the required lease action
                // in order to use again in endSuccessfully
                Guid dstLeaseDomainId = OvfUtils.fetchLeaseDomainId(getDstSnapshot().getVmConfiguration());
                if (dstLeaseDomainId != null && activeSnapshotLeaseDomainId != null && !dstLeaseDomainId.equals(
                        activeSnapshotLeaseDomainId)) {
                    // in regular preview, in case both snapshots have leases on different domains,
                    // use the active snapshot lease
                    getParameters().setDstLeaseDomainId(activeSnapshotLeaseDomainId);
                    leaseAction = LeaseAction.UPDATE_LEASE_INFO_AND_LEASE_DOMAIN_ID;
                } else {
                    getParameters().setDstLeaseDomainId(dstLeaseDomainId);
                }
            }
            if (leaseAction == null) {
                leaseAction = determineLeaseAction(activeSnapshotLeaseDomainId, getParameters().getDstLeaseDomainId());
            }
            getParameters().setLeaseAction(leaseAction);
        }
    }

    private boolean isLeaseDomainIdUpdateNeeded() {
        return getParameters().getLeaseAction() == LeaseAction.UPDATE_LEASE_INFO_AND_LEASE_DOMAIN_ID;
    }

    private LeaseAction determineLeaseAction(Guid srcLeaseDomainId, Guid dstLeaseDomainId) {
        // if the current VM snapshot has a lease
        if (srcLeaseDomainId != null && getVm().getLeaseInfo() != null) {
            // if both snapshots have leases - use the active snapshot lease
            if (dstLeaseDomainId != null) {
                return srcLeaseDomainId.equals(dstLeaseDomainId) ?
                        LeaseAction.UPDATE_LEASE_INFO :
                        LeaseAction.CREATE_NEW_LEASE;
            }
        }
        return dstLeaseDomainId != null ? LeaseAction.CREATE_NEW_LEASE : LeaseAction.DO_NOTHING;
    }

    private boolean isRestoreMemory() {
        return getParameters().isRestoreMemory() &&
                FeatureSupported.isMemorySnapshotSupportedByArchitecture(
                        getVm().getClusterArch(), getVm().getCompatibilityVersion());
    }

    private boolean updateClusterCompatibilityVersionToOldCluster(boolean disableLock) {
        Version oldClusterVersion = getVm().getClusterCompatibilityVersionOrigin();
        if (isRestoreMemory() && getVm().getCustomCompatibilityVersion() == null &&
                oldClusterVersion.less(getVm().getClusterCompatibilityVersion())) {
            // the snapshot was taken before cluster version change, call the UpdateVmCommand

            // vm_static of the getVm() is just updated by the previewed OVF config, so reload before UpdateVmCommand
            VmStatic vmFromDb = vmStaticDao.get(getVmId());
            return updateVm(vmFromDb, oldClusterVersion, disableLock);
        }

        return true;
    }

    private boolean updateVm(VmStatic vm, Version oldClusterVersion, boolean disableLock) {
        VmManagementParametersBase updateParams = new VmManagementParametersBase(vm);
        updateParams.setClusterLevelChangeFromVersion(oldClusterVersion);

        CommandContext context;
        if (disableLock) {
            updateParams.setLockProperties(LockProperties.create(LockProperties.Scope.None));
            context = cloneContextAndDetachFromParent();
        } else {
            // Wait for VM lock
            EngineLock updateVmLock = createUpdateVmLock();
            lockManager.acquireLockWait(updateVmLock); // will be released by UpdateVmCommand
            context = ExecutionHandler.createInternalJobContext(updateVmLock);
        }

        ActionReturnValue result = runInternalAction(
                ActionType.UpdateVm,
                updateParams,
                context);

        if (!result.getSucceeded()) {
            getReturnValue().setFault(result.getFault());
            return false;
        }

        return true;
    }

    private EngineLock createUpdateVmLock() {
        return new EngineLock(
                UpdateVmCommand.getExclusiveLocksForUpdateVm(getVm()),
                UpdateVmCommand.getSharedLocksForUpdateVm(getVm()));
    }
    protected boolean tryBackAllCinderDisks( List<CinderDisk> cinderDisks, Guid newSnapshotId) {
        for (CinderDisk disk : cinderDisks) {
            ImagesContainterParametersBase params = buildCinderChildCommandParameters(disk, newSnapshotId);
            ActionReturnValue actionReturnValue = runInternalAction(
                    ActionType.TryBackToCinderSnapshot,
                    params,
                    cloneContextAndDetachFromParent());
            if (!actionReturnValue.getSucceeded()) {
                log.error("Error cloning Cinder disk for preview. '{}': {}", disk.getDiskAlias());
                getReturnValue().setFault(actionReturnValue.getFault());
                return false;
            }
        }
        return true;
    }

    private CreateCinderSnapshotParameters buildCinderChildCommandParameters(DiskImage cinderDisk, Guid newSnapshotId) {
        CreateCinderSnapshotParameters createParams = new CreateCinderSnapshotParameters(cinderDisk.getImageId());
        createParams.setContainerId(cinderDisk.getId());
        createParams.setStorageDomainId(cinderDisk.getStorageIds().get(0));
        createParams.setDestinationImageId(cinderDisk.getImageId());
        createParams.setVmSnapshotId(newSnapshotId);
        createParams.setParentCommand(getActionType());
        createParams.setParentParameters(getParameters());
        return createParams;
    }

    private List<DiskImage> getImagesToPreview() {
        if (imagesToPreview == null) {
            imagesToPreview = getParameters().getDisks() != null ? getParameters().getDisks() :
                    diskImageDao.getAllSnapshotsForVmSnapshot(getDstSnapshot().getId());

            // Filter out shareable/nonsnapable disks
            List<CinderDisk> CinderImagesToPreview = DisksFilter.filterCinderDisks(imagesToPreview);
            List<ManagedBlockStorageDisk> managedBlockStorageDisks = DisksFilter.filterManagedBlockStorageDisks(imagesToPreview);
            imagesToPreview = DisksFilter.filterImageDisks(imagesToPreview, ONLY_NOT_SHAREABLE, ONLY_SNAPABLE);
            imagesToPreview.addAll(CinderImagesToPreview);
            imagesToPreview.addAll(managedBlockStorageDisks);
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
                imageDao.updateImageVmSnapshotId(image.getImageId(), newActiveSnapshotId);
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
    protected boolean validate() {
        if (Guid.Empty.equals(getParameters().getDstSnapshotId())) {
            return failValidation(EngineMessage.ACTION_TYPE_FAILED_CORRUPTED_VM_SNAPSHOT_ID);
        }
        // verify that the destination lease domain ID belongs to one of the VM's snapshots
        if (getParameters().getDstLeaseDomainId() != null) {
            if (!validate(snapshotsValidator.isLeaseDomainIdBelongsToSnapshot(getVmId(),
                    getParameters().getDstLeaseDomainId())) &&!getParameters().getDstLeaseDomainId()
                    .equals(getVm().getLeaseStorageDomainId())) {
                return failValidation(EngineMessage.ACTION_TYPE_FAILED_LEASE_DOMAIN_ID_IS_NOT_VALID);
            }
        }

        VmValidator vmValidator = new VmValidator(getVm());
        if (!validate(vmValidator.isVmExists())
                || !validate(vmValidator.vmDown())
                || !validate(snapshotsValidator.snapshotExists(getVmId(), getParameters().getDstSnapshotId()))
                || !validate(snapshotsValidator.vmNotDuringSnapshot(getVmId()))
                || !validate(snapshotsValidator.vmNotInPreview(getVmId()))
                || !validate(snapshotsValidator.snapshotVmConfigurationBroken(getDstSnapshot(), getVmName()))) {
            return false;
        }

        updateVmDisksFromDb();
        List<DiskImage> diskImages =
                DisksFilter.filterImageDisks(getVm().getDiskMap().values(), ONLY_NOT_SHAREABLE,
                        ONLY_SNAPABLE, ONLY_ACTIVE);
        diskImages.addAll(DisksFilter.filterCinderDisks(getVm().getDiskMap().values(), ONLY_PLUGGED));
        if (!diskImages.isEmpty()) {
            if (!validate(new StoragePoolValidator(getStoragePool()).existsAndUp())) {
                return false;
            }

            DiskImagesValidator diskImagesValidator = new DiskImagesValidator(diskImages);
            if (!validate(diskImagesValidator.diskImagesNotIllegal()) ||
                    !validate(diskImagesValidator.diskImagesNotLocked())) {
                return false;
            }

            List<DiskImage> images = getImagesToPreview();
            DiskImagesValidator diskImagesToPreviewValidator = new DiskImagesValidator(images);
            if (!validate(diskImagesToPreviewValidator.noDuplicatedIds()) ||
                    !validate(diskImagesToPreviewValidator.diskImagesSnapshotsAttachedToVm(getVmId())) ||
                    !validate(diskImagesToPreviewValidator.diskImagesNotIllegal()) ||
                    !validate(diskImagesToPreviewValidator.diskImagesNotLocked())) {
                return false;
            }

            Set<Guid> storageIds = ImagesHandler.getAllStorageIdsForImageIds(diskImages);
            // verify lease storage domain status
            if (getParameters().getDstLeaseDomainId() != null) {
                storageIds.add(getParameters().getDstLeaseDomainId());
            } else if (getDstSnapshot().getVmConfiguration() != null && getParameters().isRestoreLease()) {
                Guid leaseDomainId = OvfUtils.fetchLeaseDomainId(getDstSnapshot().getVmConfiguration());
                if (leaseDomainId != null) {
                    storageIds.add(leaseDomainId);
                }
            }

            MultipleStorageDomainsValidator storageValidator =
                    new MultipleStorageDomainsValidator(getVm().getStoragePoolId(), storageIds);
            if (!validate(storageValidator.allDomainsExistAndActive())
                    || !validate(storageValidator.allDomainsWithinThresholds())
                    || !validateCinder()
                    || !validate(storageValidator.isSupportedByManagedBlockStorageDomains(getActionType()))) {
                return false;
            }

            if (!validateDamagedSnapshotsForVmImages(getVmId())) {
                return false;
            }
        }

        DiskSnapshotsValidator diskSnapshotsValidator = new DiskSnapshotsValidator(getParameters().getDisks());
        if (!validate(diskSnapshotsValidator.canDiskSnapshotsBePreviewed(getParameters().getDstSnapshotId()))) {
            return false;
        }

        if (isRestoreMemory() && !validateMemoryTakenInSupportedVersion()) {
            return false;
        }

        if(!canRestoreVmConfigFromSnapshot()) {
            return failValidation(EngineMessage.MAC_POOL_NOT_ENOUGH_MAC_ADDRESSES);
        }
        return true;
    }

    private boolean validateDamagedSnapshotsForVmImages(Guid vmId) {
        List<Guid> vmImagesWithDamagedSnapshot = diskDao.getImagesWithDamagedSnapshotForVm(vmId);
        if (!vmImagesWithDamagedSnapshot.isEmpty()) {
            addValidationMessageVariable("diskIds", StringUtils.join(vmImagesWithDamagedSnapshot, "\n"));
            return failValidation(EngineMessage.ACTION_TYPE_FAILED_DAMAGED_IMAGE_SNAPSHOT);
        }
        return true;
    }

    private boolean validateMemoryTakenInSupportedVersion() {
        VM vmFromSnapshot = null;
        try {
            vmFromSnapshot = ovfHelper.readVmFromOvf(getDstSnapshot().getVmConfiguration()).getVm();
        } catch (OvfReaderException e) {
            // should never happen since the OVF was created by us
            log.error("Failed to parse a given ovf configuration: {}", e.getMessage());
            return false;
        }
        Version originalClusterVersion = vmFromSnapshot.getClusterCompatibilityVersionOrigin();
        if (Version.getLowest().greater(originalClusterVersion)) {
            return failValidation(EngineMessage.ACTION_TYPE_FAILED_MEMORY_TOO_OLD,
                    String.format("$Cv %s", originalClusterVersion != null ? originalClusterVersion : "N/A"));
        }
        return true;
    }

    private boolean validateCinder() {
        List<CinderDisk> cinderDisks = DisksFilter.filterCinderDisks(diskDao.getAllForVm(getVmId()));
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
            cachedSnapshot = snapshotDao.get(getParameters().getDstSnapshotId());
        }
        return cachedSnapshot;
    }

    @Override
    protected void setActionMessageParameters() {
        addValidationMessage(EngineMessage.VAR__ACTION__PREVIEW);
        addValidationMessage(EngineMessage.VAR__TYPE__SNAPSHOT);
    }

    protected void updateVmDisksFromDb() {
        vmHandler.updateDisksFromDb(getVm());
    }

    @Override
    protected ActionType getChildActionType() {
        return ActionType.TryBackToSnapshot;
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

    @Override
    public CommandCallback getCallback() {
        return getParameters().isUseCinderCommandCallback() ? callbackProvider.get() : null;
    }

    private List<DiskImage> getFilteredImagesToPreview() {
        List<DiskImage> images = getImagesToPreview();
        List<DiskImage> imagesToExclude = diskImageDao.getAttachedDiskSnapshotsToVm(getVmId(), Boolean.TRUE);
        Set<Guid> excludeIds = imagesToExclude.stream().map(DiskImage::getId).collect(Collectors.toSet());

        return images.stream()
                .filter(image -> !excludeIds.contains(image.getId()))
                .collect(Collectors.toList());
    }
}
