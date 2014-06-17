package org.ovirt.engine.core.bll;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.lang.StringUtils;
import org.ovirt.engine.core.bll.quota.QuotaConsumptionParameter;
import org.ovirt.engine.core.bll.quota.QuotaManager;
import org.ovirt.engine.core.bll.quota.QuotaStorageDependent;
import org.ovirt.engine.core.bll.snapshots.SnapshotsValidator;
import org.ovirt.engine.core.bll.storage.StoragePoolValidator;
import org.ovirt.engine.core.bll.tasks.TaskManagerUtil;
import org.ovirt.engine.core.bll.tasks.interfaces.CommandCallBack;
import org.ovirt.engine.core.bll.validator.DiskImagesValidator;
import org.ovirt.engine.core.bll.validator.StorageDomainValidator;
import org.ovirt.engine.core.bll.validator.VmValidator;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.FeatureSupported;
import org.ovirt.engine.core.common.VdcObjectType;
import org.ovirt.engine.core.common.action.ImagesContainterParametersBase;
import org.ovirt.engine.core.common.action.RemoveSnapshotParameters;
import org.ovirt.engine.core.common.action.RemoveSnapshotSingleDiskParameters;
import org.ovirt.engine.core.common.action.VdcActionParametersBase;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.action.VdcReturnValueBase;
import org.ovirt.engine.core.common.asynctasks.EntityInfo;
import org.ovirt.engine.core.common.businessentities.DiskImage;
import org.ovirt.engine.core.common.businessentities.Snapshot;
import org.ovirt.engine.core.common.businessentities.Snapshot.SnapshotStatus;
import org.ovirt.engine.core.common.businessentities.StorageDomain;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.errors.VdcBLLException;
import org.ovirt.engine.core.common.errors.VdcBllErrors;
import org.ovirt.engine.core.common.errors.VdcBllMessages;
import org.ovirt.engine.core.common.locks.LockingGroup;
import org.ovirt.engine.core.common.utils.Pair;
import org.ovirt.engine.core.compat.CommandStatus;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.AuditLogDirector;
import org.ovirt.engine.core.dao.SnapshotDao;
import org.ovirt.engine.core.utils.collections.MultiValueMapUtils;
import org.ovirt.engine.core.utils.transaction.TransactionMethod;
import org.ovirt.engine.core.utils.transaction.TransactionSupport;

/**
 * Merges snapshots either live or non-live based on VM status
 */
@DisableInPrepareMode
@LockIdNameAttribute
public class RemoveSnapshotCommand<T extends RemoveSnapshotParameters> extends VmCommand<T>
        implements QuotaStorageDependent {
    private List<DiskImage> _sourceImages = null;

    public RemoveSnapshotCommand(T parameters) {
        super(parameters);
    }

    private void initializeObjectState() {
        if (StringUtils.isEmpty(getSnapshotName())) {
            Snapshot snapshot = getSnapshotDao().get(getParameters().getSnapshotId());
            if (snapshot != null) {
                setSnapshotName(snapshot.getDescription());
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
     * Note that the first time this method is run it issues DAO call.
     */
    protected List<DiskImage> getSourceImages() {
        if (_sourceImages == null) {
            _sourceImages = getDiskImageDao().getAllSnapshotsForVmSnapshot(getParameters().getSnapshotId());
        }
        return _sourceImages;
    }

    @Override
    protected void executeCommand() {
        if (FeatureSupported.liveMerge(getVm().getVdsGroupCompatibilityVersion())) {
            if (!getVm().isQualifiedForSnapshotMerge()) {
                log.error("Cannot remove VM snapshot. Vm is not Down, Up or Paused");
                throw new VdcBLLException(VdcBllErrors.VM_NOT_QUALIFIED_FOR_SNAPSHOT_MERGE);
            }
        } else if (!getVm().isDown()) {
            log.error("Cannot remove VM snapshot. Vm is not Down and cluster version does not support Live Merge");
            throw new VdcBLLException(VdcBllErrors.IRS_IMAGE_STATUS_ILLEGAL);
        }

        final Snapshot snapshot = getSnapshotDao().get(getParameters().getSnapshotId());

        boolean snapshotHasImages = hasImages();
        boolean removeSnapshotMemory = isMemoryVolumeRemoveable(snapshot.getMemoryVolume());

        // If the VM hasn't got any images and memory - simply remove the snapshot.
        // No need for locking, VDSM tasks, and all that jazz.
        if (!snapshotHasImages && !removeSnapshotMemory) {
            getSnapshotDao().remove(getParameters().getSnapshotId());
            setSucceeded(true);
            return;
        }

        lockSnapshot(snapshot);
        freeLock();
        getParameters().setEntityInfo(new EntityInfo(VdcObjectType.VM, getVmId()));

        if (snapshotHasImages) {
            removeImages();

            if (getSnapshotActionType() == VdcActionType.RemoveSnapshotSingleDiskLive) {
                // Enable callbacks in order to monitor for new-style child completion
                setCommandStatus(CommandStatus.ACTIVE_ASYNC);
                persistCommand(getParameters().getParentCommand(), true);
            }
        }

        if (removeSnapshotMemory) {
            removeMemory(snapshot);
        }

        setSucceeded(true);
    }

    /**
     * There is a one to many relation between memory volumes and snapshots, so memory
     * volumes should be removed only if the only snapshot that points to them is removed
     */
    protected boolean isMemoryVolumeRemoveable(String memoryVolume) {
        return !memoryVolume.isEmpty() &&
                getDbFacade().getSnapshotDao().getNumOfSnapshotsByMemory(memoryVolume) == 1;
    }

    private void removeMemory(final Snapshot snapshot) {
        boolean success = removeMemoryVolumes(snapshot.getMemoryVolume(), getActionType(), false);
        if (!success) {
            log.errorFormat("Cannot remove memory volumes for snapshot {0}", snapshot.getId());
        }
    }

    private void removeImages() {
        for (final DiskImage source : getSourceImages()) {

            // The following line is ok because we have tested in the candoaction that the vm
            // is not a template and the vm is not in preview mode and that
            // this is not the active snapshot.
            DiskImage dest = getDiskImageDao().getAllSnapshotsForParent(source.getImageId()).get(0);

            if (getSnapshotActionType() == VdcActionType.RemoveSnapshotSingleDisk) {
                VdcReturnValueBase vdcReturnValue = runInternalActionWithTasksContext(
                        getSnapshotActionType(),
                        buildRemoveSnapshotSingleDiskParameters(source, dest));
                if (vdcReturnValue != null && vdcReturnValue.getInternalVdsmTaskIdList() != null) {
                    getReturnValue().getVdsmTaskIdList().addAll(vdcReturnValue.getInternalVdsmTaskIdList());
                }
            } else {
                TaskManagerUtil.executeAsyncCommand(
                        getSnapshotActionType(),
                        buildRemoveSnapshotSingleDiskParameters(source, dest));
            }

            List<Guid> quotasToRemoveFromCache = new ArrayList<Guid>();
            quotasToRemoveFromCache.add(source.getQuotaId());
            quotasToRemoveFromCache.add(dest.getQuotaId());
            QuotaManager.getInstance().removeQuotaFromCache(getStoragePoolId(), quotasToRemoveFromCache);
        }
    }

    private void lockSnapshot(final Snapshot snapshot) {
        TransactionSupport.executeInNewTransaction(new TransactionMethod<Void>() {

            @Override
            public Void runInTransaction() {
                getCompensationContext().snapshotEntityStatus(snapshot);
                getSnapshotDao().updateStatus(
                        getParameters().getSnapshotId(), SnapshotStatus.LOCKED);
                getCompensationContext().stateChanged();
                return null;
            }
        });
    }

    private RemoveSnapshotSingleDiskParameters buildRemoveSnapshotSingleDiskParameters(final DiskImage source,
            DiskImage dest) {
        RemoveSnapshotSingleDiskParameters parameters =
                new RemoveSnapshotSingleDiskParameters(source.getImageId(), getVmId());
        parameters.setDestinationImageId(dest.getImageId());
        parameters.setEntityInfo(getParameters().getEntityInfo());
        parameters.setParentParameters(getParameters());
        parameters.setParentCommand(getActionType());
        parameters.setCommandType(getSnapshotActionType());
        parameters.setVdsId(getVm().getRunOnVds());
        return parameters;
    }

    @Override
    protected void endVmCommand() {
        initializeObjectState();
        if (getParameters().getTaskGroupSuccess()) {
            getSnapshotDao().remove(getParameters().getSnapshotId());
        } else {
            List<String> failedToRemoveDisks = new ArrayList<>();
            Snapshot snapshot = getSnapshotDao().get(getParameters().getSnapshotId());

            for (VdcActionParametersBase parameters : getParameters().getImagesParameters()) {
                ImagesContainterParametersBase imagesParams = (parameters instanceof ImagesContainterParametersBase ?
                        (ImagesContainterParametersBase) parameters : null);

                if (imagesParams == null) {
                    // Shouldn't happen as for now ImagesParameters list contains only
                    // instances of ImagesContainterParametersBase objects.
                    continue;
                }

                if (imagesParams.getTaskGroupSuccess()) {
                    snapshot = ImagesHandler.prepareSnapshotConfigWithoutImageSingleImage(
                            snapshot, imagesParams.getImageId());
                } else {
                    log.errorFormat("Could not delete image {0} from snapshot {1}",
                            imagesParams.getImageId(), getParameters().getSnapshotId());

                    DiskImage diskImage = getDiskImageDao().getSnapshotById(imagesParams.getImageId());
                    failedToRemoveDisks.add(diskImage.getDiskAlias());
                }
            }

            // Remove memory volume and update the dao.
            // Note: on failure, we can treat memory volume deletion as deleting an image
            // and remove it from the snapshot entity (rollback isn't applicable).
            snapshot.setMemoryVolume("");
            getSnapshotDao().update(snapshot);

            if (!failedToRemoveDisks.isEmpty()) {
                addCustomValue("DiskAliases", StringUtils.join(failedToRemoveDisks, ", "));
                AuditLogDirector.log(this, AuditLogType.USER_REMOVE_SNAPSHOT_FINISHED_FAILURE_PARTIAL_SNAPSHOT);
            }

            getSnapshotDao().updateStatus(getParameters().getSnapshotId(), SnapshotStatus.OK);
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
    protected boolean canDoAction() {
        initializeObjectState();

        if (getVm() == null) {
            return failCanDoAction(VdcBllMessages.ACTION_TYPE_FAILED_VM_NOT_FOUND);
        }

        if (!canRunActionOnNonManagedVm()) {
            return false;
        }

        VmValidator vmValidator = createVmValidator(getVm());
        if (!validate(new StoragePoolValidator(getStoragePool()).isUp()) ||
                !validateVmNotDuringSnapshot() ||
                !validateVmNotInPreview() ||
                !validateSnapshotExists() ||
                !(FeatureSupported.liveMerge(getVm().getVdsGroupCompatibilityVersion())
                        ? validate(vmValidator.vmQualifiedForSnapshotMerge())
                        : validate(vmValidator.vmDown())) ||
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
                return failCanDoAction(VdcBllMessages.ACTION_TYPE_FAILED_CANNOT_REMOVE_IMAGE_TEMPLATE);
            }

            // check that we are not deleting the vm working snapshot
            if (!validateImageNotActive()) {
                return failCanDoAction(VdcBllMessages.ACTION_TYPE_FAILED_CANNOT_REMOVE_ACTIVE_IMAGE);
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
     * Each domain is validated for status and for enough free space to perform removeSnapshot. <BR/>
     * The remove snapshot logic in VDSM includes creating a new temporary volume which might be as large as the disk's
     * actual size. <BR/>
     * Hence, as part of the validation, we sum up all the disks virtual sizes, for each storage domain.
     *
     * @return True if there is enough space in all relevant storage domains. False otherwise.
     */
    protected boolean validateStorageDomains() {
        for (final Entry<Guid, List<DiskImage>> storageToDiskEntry : getStorageToDiskMap().entrySet()) {
            Guid storageDomainId = storageToDiskEntry.getKey();
            StorageDomain storageDomain =
                    getStorageDomainDAO().getForStoragePool(storageDomainId, getStoragePoolId());
            StorageDomainValidator validator = new StorageDomainValidator(storageDomain);

            if (!validate(validator.isDomainExistAndActive())) {
                return false;
            }

            List<DiskImage> diskImages = storageToDiskEntry.getValue();
            long sizeRequested = 0L;
            for (DiskImage diskImage : diskImages) {
                sizeRequested += diskImage.getActualSize();
            }

            if (!validate(validator.isDomainHasSpaceForRequest(sizeRequested, false))) {
                return false;
            }
        }
        return true;
    }

    private Map<Guid, List<DiskImage>> getStorageToDiskMap() {
        Map<Guid, List<DiskImage>> storageToDisksMap = new HashMap<Guid, List<DiskImage>>();
        for (DiskImage disk : getSourceImages()) {
            MultiValueMapUtils.addToMap(disk.getStorageIds().get(0), disk, storageToDisksMap);
        }
        return storageToDisksMap;
    }

    @Override
    protected void setActionMessageParameters() {
        addCanDoActionMessage(VdcBllMessages.VAR__TYPE__SNAPSHOT);
        addCanDoActionMessage(VdcBllMessages.VAR__ACTION__REMOVE);
    }

    protected boolean validateVmNotDuringSnapshot() {
        return validate(createSnapshotValidator().vmNotDuringSnapshot(getVmId()));
    }

    protected boolean validateVmNotInPreview() {
        return validate(createSnapshotValidator().vmNotInPreview(getVmId()));
    }

    protected boolean validateSnapshotExists() {
        return validate(createSnapshotValidator().snapshotExists(getVmId(), getParameters().getSnapshotId()));
    }

    protected boolean validateImages() {
        List<DiskImage> imagesToValidate =
                ImagesHandler.filterImageDisks(getDiskDao().getAllForVm(getVmId()), true, false, true);
        DiskImagesValidator diskImagesValidator = new DiskImagesValidator(imagesToValidate);

        return validate(diskImagesValidator.diskImagesNotLocked()) &&
                validate(diskImagesValidator.diskImagesNotIllegal());
    }

    protected boolean validateImageNotInTemplate() {
        return getVmTemplateDAO().get(getRepresentativeSourceImageId()) == null;
    }

    protected boolean validateImageNotActive() {
        return getDiskImageDao().get(getRepresentativeSourceImageId()) == null;
    }

    private boolean hasImages() {
        return !getSourceImages().isEmpty();
    }

    private Guid getRepresentativeSourceImageId() {
        return getSourceImages().get(0).getImageId();
    }

    protected SnapshotsValidator createSnapshotValidator() {
        return new SnapshotsValidator();
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

    private VdcActionType getSnapshotActionType() {
        return getVm().isDown() ? VdcActionType.RemoveSnapshotSingleDisk : VdcActionType.RemoveSnapshotSingleDiskLive;
    }

    @Override
    protected Map<String, Pair<String, String>> getExclusiveLocks() {
        return Collections.singletonMap(getVmId().toString(),
                LockMessagesMatchUtil.makeLockingPair(LockingGroup.VM, VdcBllMessages.ACTION_TYPE_FAILED_OBJECT_LOCKED));
    }

    protected SnapshotDao getSnapshotDao() {
        return getDbFacade().getSnapshotDao();
    }

    @Override
    public List<QuotaConsumptionParameter> getQuotaStorageConsumptionParameters() {
        //return empty list - the command only release quota so it could never fail the quota check
        return new ArrayList<QuotaConsumptionParameter>();
    }

    @Override
    public CommandCallBack getCallBack() {
        return new RemoveSnapshotCommandCallback();
    }
}
