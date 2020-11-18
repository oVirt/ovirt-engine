package org.ovirt.engine.core.bll;

import static org.ovirt.engine.core.bll.storage.disk.image.DisksFilter.ONLY_ACTIVE;
import static org.ovirt.engine.core.bll.storage.disk.image.DisksFilter.ONLY_PLUGGED;
import static org.ovirt.engine.core.bll.storage.disk.image.DisksFilter.ONLY_SNAPABLE;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.enterprise.inject.Instance;
import javax.enterprise.inject.Typed;
import javax.inject.Inject;

import org.apache.commons.lang.StringUtils;
import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.bll.storage.disk.image.DisksFilter;
import org.ovirt.engine.core.bll.tasks.interfaces.CommandCallback;
import org.ovirt.engine.core.bll.utils.PermissionSubject;
import org.ovirt.engine.core.bll.validator.VmValidator;
import org.ovirt.engine.core.common.VdcObjectType;
import org.ovirt.engine.core.common.action.AddVmFromSnapshotParameters;
import org.ovirt.engine.core.common.action.LockProperties;
import org.ovirt.engine.core.common.action.LockProperties.Scope;
import org.ovirt.engine.core.common.businessentities.Snapshot;
import org.ovirt.engine.core.common.businessentities.Snapshot.SnapshotStatus;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VMStatus;
import org.ovirt.engine.core.common.businessentities.VmInit;
import org.ovirt.engine.core.common.businessentities.VmStatic;
import org.ovirt.engine.core.common.businessentities.storage.DiskImage;
import org.ovirt.engine.core.common.businessentities.storage.StorageType;
import org.ovirt.engine.core.common.businessentities.storage.VolumeFormat;
import org.ovirt.engine.core.common.businessentities.storage.VolumeType;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.common.locks.LockingGroup;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.common.queries.QueryReturnValue;
import org.ovirt.engine.core.common.queries.QueryType;
import org.ovirt.engine.core.common.utils.Pair;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.DiskImageDao;
import org.ovirt.engine.core.dao.SnapshotDao;
import org.ovirt.engine.core.dao.VmDao;
import org.ovirt.engine.core.dao.VmDynamicDao;
import org.ovirt.engine.core.utils.transaction.TransactionSupport;

/**
 * This class adds a cloned VM from a snapshot (Deep disk copy).
 */
@DisableInPrepareMode
@NonTransactiveCommandAttribute(forceCompensation = true)
public class AddVmFromSnapshotCommand<T extends AddVmFromSnapshotParameters> extends AddVmAndCloneImageCommand<T> {

    @Inject
    private DiskImageDao diskImageDao;
    @Inject
    private VmDynamicDao vmDynamicDao;
    @Inject
    private SnapshotDao snapshotDao;
    @Inject
    private VmDao vmDao;
    @Inject
    @Typed(ConcurrentChildCommandsExecutionCallback.class)
    private Instance<ConcurrentChildCommandsExecutionCallback> callbackProvider;

    private Guid sourceSnapshotId;
    private Snapshot snapshot;
    private VM sourceVmFromDb;
    private VM vmFromConfiguration;
    private Collection<DiskImage> diskImagesFromConfiguration;
    private Guid storageDomainId;

    protected AddVmFromSnapshotCommand(Guid commandId) {
        super(commandId);
    }

    protected AddVmFromSnapshotCommand(T params, CommandContext commandContext) {
        super(params, commandContext);
        sourceSnapshotId = params.getSourceSnapshotId();
    }

    @Override
    protected void init() {
        super.init();
        VM vm = vmDao.get(getVmIdFromSnapshot());
        vmHandler.updateDisksFromDb(vm);
        boolean isCinderDisksExist = !DisksFilter.filterCinderDisks(vm.getDiskList()).isEmpty();
        getParameters().setUseCinderCommandCallback(isCinderDisksExist);
    }

    @Override
    protected LockProperties applyLockProperties(LockProperties lockProperties) {
        return lockProperties.withScope(Scope.Execution);
    }

    @Override
    public Guid getStoragePoolId() {
        return (getSourceVmFromDb() != null) ? getSourceVmFromDb().getStoragePoolId() : null;
    }

    @Override
    public Guid getStorageDomainId() {
        if (storageDomainId == null) {
            // This is needed for logging the command using CommandBase.logCommand
            List<DiskImage> images = diskImageDao.getAllSnapshotsForVmSnapshot(sourceSnapshotId);
            storageDomainId = !images.isEmpty() ? images.get(0).getStorageIds().get(0) : Guid.Empty;
        }
        return storageDomainId;
    }

    @Override
    protected VmInit loadOriginalVmInitWithRootPassword() {
        return vmFromConfiguration.getVmInit();
    }

    @Override
    protected Guid getStoragePoolIdFromSourceImageContainer() {
        return sourceVmFromDb.getStoragePoolId();
    }

    protected Guid getVmIdFromSnapshot() {
        return (getSnapshot() != null) ? getSnapshot().getVmId() : Guid.Empty;
    }

    @Override
    protected Collection<DiskImage> getSourceDisks() {
        if (diskImagesFromConfiguration == null) {
            diskImagesFromConfiguration =
                    DisksFilter.filterImageDisks(vmFromConfiguration.getDiskMap().values(),
                            ONLY_SNAPABLE, ONLY_ACTIVE);
            diskImagesFromConfiguration.addAll(
                    DisksFilter.filterCinderDisks(vmFromConfiguration.getDiskMap().values(), ONLY_PLUGGED));
            adjustDisksImageConfiguration(diskImagesFromConfiguration);
        }
        return diskImagesFromConfiguration;
    }

    private void adjustDisksImageConfiguration(Collection<DiskImage> diskImages) {
        for (DiskImage diskImage : diskImages) {
            // Adjust disk image configuration if needed.
            if ( diskImage.getVolumeType().equals(VolumeType.Sparse) && diskImage.getVolumeFormat().equals(VolumeFormat.RAW) &&
                    getDestintationDomainTypeFromDisk(diskImage).isBlockDomain()) {
                diskImage.setVolumeFormat(VolumeFormat.COW);
            }
        }
    }

    private StorageType getDestintationDomainTypeFromDisk(DiskImage diskImage) {
        return destStorages.get(diskInfoDestinationMap.get(diskImage.getId()).getStorageIds().get(0)).getStorageStaticData().getStorageType();
    }

    @Override
    protected void logErrorOneOrMoreActiveDomainsAreMissing() {
        log.error("Can not found any default active domain for one of the disks of snapshot with id '{}'",
                sourceSnapshotId);
    }

    protected Snapshot getSnapshot() {
        if (snapshot == null) {
            snapshot = snapshotDao.get(sourceSnapshotId);
            if (snapshot != null) {
                setSnapshotName(snapshot.getDescription());
            }
        }
        return snapshot;
    }

    @Override
    protected boolean validate() {
        // If snapshot does not exist, there is not point in checking any of the VM related checks
        if (!validate(snapshotsValidator.snapshotExists(getSnapshot()))
                || !validate(snapshotsValidator.vmNotDuringSnapshot(getSnapshot().getVmId()))
                || !validate(snapshotsValidator.snapshotVmConfigurationBroken(getSnapshot(), getVmName()))
                || !validate(snapshotsValidator.isRegularSnapshot(getSnapshot()))
                || !validate(snapshotsValidator.vmNotInPreview(getVmIdFromSnapshot()))) {
            return false;
        }

        vmFromConfiguration = getVmFromConfiguration();
        if (vmFromConfiguration == null) {
            addValidationMessage(EngineMessage.ACTION_TYPE_FAILED_VM_SNAPSHOT_HAS_NO_CONFIGURATION);
            addValidationMessageVariable("VmName", getVmName());
            addValidationMessageVariable("SnapshotName", getSnapshotName());

            return false;
        }

        if (!super.validate()) {
            return false;
        }

        if (!checkCanDisableVirtIoScsi()) {
            return false;
        }

        return true;
    }

    protected boolean checkCanDisableVirtIoScsi() {
        VmValidator vmValidator = createVmValidator(getVmFromConfiguration());
        if (Boolean.FALSE.equals(getParameters().isVirtioScsiEnabled()) &&
                !validate(vmValidator.canDisableVirtioScsi(getSourceDisks()))) {
            return false;
        } else {
            return true;
        }
    }

    @Override
    protected VM getVmFromConfiguration() {
        if (vmFromConfiguration == null) {
            QueryReturnValue queryReturnValue =
                    runInternalQuery(QueryType.GetVmConfigurationBySnapshot,
                            new IdQueryParameters(snapshot.getId()));
            if (queryReturnValue.getSucceeded()) {
                vmFromConfiguration = queryReturnValue.getReturnValue();
            }
        }
        return vmFromConfiguration;
    }

    @Override
    /**
     * Assumption - a snapshot can be locked only if in status OK, so if validate passed
     * this is the status of the snapshot. In addition the newly added VM is in down status
     */
    protected void lockEntities() {
        TransactionSupport.executeInNewTransaction(() -> {
            getCompensationContext().snapshotEntityStatus(getSnapshot());
            snapshotDao.updateStatus(sourceSnapshotId, SnapshotStatus.LOCKED);
            lockVmWithCompensationIfNeeded();
            getCompensationContext().stateChanged();
            return null;
        });
        freeLock();
    }

    @Override
    protected VM getSourceVmFromDb() {
        if (sourceVmFromDb == null) {
            sourceVmFromDb = vmDao.get(getVmIdFromSnapshot());
        }
        return sourceVmFromDb;
    }

    @Override
    protected void unlockEntities() {
        // Assumption - this is last DB change of command, no need for compensation here
        snapshotDao.updateStatus(sourceSnapshotId, SnapshotStatus.OK);
        vmDynamicDao.updateStatus(getVmId(), VMStatus.Down);
    }

    @Override
    public Map<String, String> getJobMessageProperties() {
        if (jobProperties == null) {
            jobProperties = super.getJobMessageProperties();
            jobProperties.put(VdcObjectType.Snapshot.name().toLowerCase(),
                    StringUtils.defaultString(getSnapshotName()));
        }
        return jobProperties;
    }

    @Override
    protected Map<String, Pair<String, String>> getExclusiveLocks() {
        Map<String, Pair<String, String>> thisLocks = Collections.singletonMap(getSourceVmFromDb().getId().toString(),
                LockMessagesMatchUtil.makeLockingPair(LockingGroup.VM, EngineMessage.ACTION_TYPE_FAILED_OBJECT_LOCKED));
        Map<String, Pair<String, String>> parentLocks = super.getExclusiveLocks();
        if (parentLocks == null) {
            return thisLocks;
        }

        Map<String, Pair<String, String>> union = new HashMap<>();
        union.putAll(parentLocks);
        union.putAll(thisLocks);

        return union;
    }

    @Override
    protected Guid getSourceVmId() {
        return getVmIdFromSnapshot();
    }

    @Override
    public List<PermissionSubject> getPermissionCheckSubjects() {
        List<PermissionSubject> permissionList = super.getPermissionCheckSubjects();

        permissionList.add(new PermissionSubject(getVmIdFromSnapshot(),
                VdcObjectType.VM,
                getActionType().getActionGroup()));

        return permissionList;
    }

    @Override
    protected void endSuccessfully() {
        super.endSuccessfully();
        unlockEntities();
    }

    @Override
    protected void endWithFailure() {
        super.endWithFailure();
        unlockEntities();
    }

    @Override
    protected void updateOriginalTemplate(VmStatic vmStatic) {
        // do not update it - it is already correctly configured from the snapshot
    }

    public VmValidator createVmValidator(VM vm) {
        return new VmValidator(vm);
    }

    @Override
    public CommandCallback getCallback() {
        return getParameters().isUseCinderCommandCallback() ? callbackProvider.get() : null;
    }
}
