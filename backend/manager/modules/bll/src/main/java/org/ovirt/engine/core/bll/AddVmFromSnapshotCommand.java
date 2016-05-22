package org.ovirt.engine.core.bll;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.bll.snapshots.SnapshotsValidator;
import org.ovirt.engine.core.bll.storage.disk.image.ImagesHandler;
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
import org.ovirt.engine.core.common.businessentities.VmStatic;
import org.ovirt.engine.core.common.businessentities.storage.DiskImage;
import org.ovirt.engine.core.common.businessentities.storage.StorageType;
import org.ovirt.engine.core.common.businessentities.storage.VolumeFormat;
import org.ovirt.engine.core.common.businessentities.storage.VolumeType;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.common.locks.LockingGroup;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.common.queries.VdcQueryReturnValue;
import org.ovirt.engine.core.common.queries.VdcQueryType;
import org.ovirt.engine.core.common.utils.Pair;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.utils.transaction.TransactionSupport;

/**
 * This class adds a cloned VM from a snapshot (Deep disk copy).
 */
@DisableInPrepareMode
@NonTransactiveCommandAttribute(forceCompensation = true)
public class AddVmFromSnapshotCommand<T extends AddVmFromSnapshotParameters> extends AddVmAndCloneImageCommand<T> {

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
        VM vm = getVmDao().get(getVmIdFromSnapshot());
        VmHandler.updateDisksFromDb(vm);
        boolean isCinderDisksExist = !ImagesHandler.filterDisksBasedOnCinder(vm.getDiskList()).isEmpty();
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
            List<DiskImage> images = getDiskImageDao().getAllSnapshotsForVmSnapshot(sourceSnapshotId);
            storageDomainId = !images.isEmpty() ? images.get(0).getStorageIds().get(0) : Guid.Empty;
        }
        return storageDomainId;
    }

    @Override
    protected Guid getStoragePoolIdFromSourceImageContainer() {
        return sourceVmFromDb.getStoragePoolId();
    }

    protected Guid getVmIdFromSnapshot() {
        return (getSnapshot() != null) ? getSnapshot().getVmId() : Guid.Empty;
    }

    @Override
    protected Collection<DiskImage> getAdjustedDiskImagesFromConfiguration() {
        if (diskImagesFromConfiguration == null) {
            diskImagesFromConfiguration =
                    ImagesHandler.filterImageDisks(vmFromConfiguration.getDiskMap().values(),
                            false,
                            true,
                            true);
            diskImagesFromConfiguration.addAll(ImagesHandler.filterDisksBasedOnCinder(vmFromConfiguration.getDiskMap().values(), true));
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
            snapshot = getSnapshotDao().get(sourceSnapshotId);
            if (snapshot != null) {
                setSnapshotName(snapshot.getDescription());
            }
        }
        return snapshot;
    }

    @Override
    protected boolean validate() {
        SnapshotsValidator snapshotsValidator = createSnapshotsValidator();

        // If snapshot does not exist, there is not point in checking any of the VM related checks
        if (!validate(snapshotsValidator.snapshotExists(getSnapshot()))
                || !validate(snapshotsValidator.vmNotDuringSnapshot(getSnapshot().getVmId()))) {
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
                !validate(vmValidator.canDisableVirtioScsi(getAdjustedDiskImagesFromConfiguration()))) {
            return false;
        } else {
            return true;
        }
    }

    protected SnapshotsValidator createSnapshotsValidator() {
        return new SnapshotsValidator();
    }

    @Override
    protected VM getVmFromConfiguration() {
        if (vmFromConfiguration == null) {
            VdcQueryReturnValue queryReturnValue =
                    runInternalQuery(VdcQueryType.GetVmConfigurationBySnapshot,
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
            getSnapshotDao().updateStatus(sourceSnapshotId, SnapshotStatus.LOCKED);
            lockVmWithCompensationIfNeeded();
            getCompensationContext().stateChanged();
            return null;
        });
        freeLock();
    }

    @Override
    protected VM getSourceVmFromDb() {
        if (sourceVmFromDb == null) {
            sourceVmFromDb = getVmDao().get(getVmIdFromSnapshot());
        }
        return sourceVmFromDb;
    }

    @Override
    protected void unlockEntities() {
        // Assumption - this is last DB change of command, no need for compensation here
        getSnapshotDao().updateStatus(sourceSnapshotId, SnapshotStatus.OK);
        getVmDynamicDao().updateStatus(getVmId(), VMStatus.Down);
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
        return getParameters().isUseCinderCommandCallback() ? new ConcurrentChildCommandsExecutionCallback() : null;
    }
}
