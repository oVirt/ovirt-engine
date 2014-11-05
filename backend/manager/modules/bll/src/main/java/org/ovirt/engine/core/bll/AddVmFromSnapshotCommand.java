package org.ovirt.engine.core.bll;

import org.apache.commons.lang.StringUtils;
import org.ovirt.engine.core.bll.snapshots.SnapshotsValidator;
import org.ovirt.engine.core.bll.utils.PermissionSubject;
import org.ovirt.engine.core.bll.validator.VmValidator;
import org.ovirt.engine.core.common.VdcObjectType;
import org.ovirt.engine.core.common.action.AddVmFromSnapshotParameters;
import org.ovirt.engine.core.common.action.LockProperties;
import org.ovirt.engine.core.common.action.LockProperties.Scope;
import org.ovirt.engine.core.common.businessentities.DiskImage;
import org.ovirt.engine.core.common.businessentities.Snapshot;
import org.ovirt.engine.core.common.businessentities.Snapshot.SnapshotStatus;
import org.ovirt.engine.core.common.businessentities.StorageType;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VMStatus;
import org.ovirt.engine.core.common.businessentities.VmStatic;
import org.ovirt.engine.core.common.businessentities.VolumeFormat;
import org.ovirt.engine.core.common.businessentities.VolumeType;
import org.ovirt.engine.core.common.errors.VdcBllMessages;
import org.ovirt.engine.core.common.locks.LockingGroup;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.common.queries.VdcQueryReturnValue;
import org.ovirt.engine.core.common.queries.VdcQueryType;
import org.ovirt.engine.core.common.utils.Pair;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.SnapshotDao;
import org.ovirt.engine.core.utils.transaction.TransactionMethod;
import org.ovirt.engine.core.utils.transaction.TransactionSupport;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

    public AddVmFromSnapshotCommand(T params) {
        super(params);
        sourceSnapshotId = params.getSourceSnapshotId();
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
            storageDomainId = (!images.isEmpty()) ? images.get(0).getStorageIds().get(0) : Guid.Empty;
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
            adjustDisksImageConfiguration(diskImagesFromConfiguration);
        }
        return diskImagesFromConfiguration;
    }

    private void adjustDisksImageConfiguration(Collection<DiskImage> diskImages) {
        for (DiskImage diskImage : diskImages) {
            // Adjust disk image configuration if needed.
            if ( diskImage.getVolumeType().equals(VolumeType.Sparse) && diskImage.getVolumeFormat().equals(VolumeFormat.RAW) &&
                    getDestintationDomainTypeFromDisk(diskImage).isBlockDomain()) {
                diskImage.setvolumeFormat(VolumeFormat.COW);
            }
        }
    }

    private StorageType getDestintationDomainTypeFromDisk(DiskImage diskImage) {
        return destStorages.get(diskInfoDestinationMap.get(diskImage.getId()).getStorageIds().get(0)).getStorageStaticData().getStorageType();
    }

    @Override
    protected void logErrorOneOrMoreActiveDomainsAreMissing() {
        log.errorFormat("Can not found any default active domain for one of the disks of snapshot with id : {0}",
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
    protected boolean canDoAction() {
        SnapshotsValidator snapshotsValidator = createSnapshotsValidator();

        // If snapshot does not exist, there is not point in checking any of the VM related checks
        if (!validate(snapshotsValidator.snapshotExists(getSnapshot()))
                || !validate(snapshotsValidator.vmNotDuringSnapshot(getSnapshot().getVmId()))) {
            return false;
        }

        vmFromConfiguration = getVmFromConfiguration();
        if (vmFromConfiguration == null) {
            addCanDoActionMessage(VdcBllMessages.ACTION_TYPE_FAILED_VM_SNAPSHOT_HAS_NO_CONFIGURATION);
            addCanDoActionMessageVariable("VmName", getVmName());
            addCanDoActionMessageVariable("SnapshotName", getSnapshotName());

            return false;
        }

        VmValidator vmValidator = createVmValidator(vmFromConfiguration);
        if (Boolean.FALSE.equals(getParameters().isVirtioScsiEnabled()) &&
                !validate(vmValidator.canDisableVirtioScsi(getAdjustedDiskImagesFromConfiguration()))) {
            return false;
        }

        if (!super.canDoAction()) {
            return false;
        }

        return true;
    }

    protected SnapshotsValidator createSnapshotsValidator() {
        return new SnapshotsValidator();
    }

    protected VM getVmFromConfiguration() {
        VM result = null;
        VdcQueryReturnValue queryReturnValue =
                runInternalQuery(VdcQueryType.GetVmConfigurationBySnapshot,
                        new IdQueryParameters(snapshot.getId()));
        if (queryReturnValue.getSucceeded()) {
            result = queryReturnValue.getReturnValue();
        }
        return result;
    }

    protected SnapshotDao getSnapshotDao() {
        return getDbFacade().getSnapshotDao();
    }

    @Override
    protected void lockEntities() {
        TransactionSupport.executeInNewTransaction(new TransactionMethod<Void>() {

            @Override
            public Void runInTransaction() {
                // Assumption - a snapshot can be locked only if in status OK, so if canDoAction passed
                // this is the status of the snapshot. In addition the newly added VM is in down status
                getCompensationContext().snapshotEntityStatus(getSnapshot());
                getSnapshotDao().updateStatus(sourceSnapshotId, SnapshotStatus.LOCKED);
                lockVmWithCompensationIfNeeded();
                getCompensationContext().stateChanged();
                return null;
            }
        });
        freeLock();
    }

    protected VM getSourceVmFromDb() {
        if (sourceVmFromDb == null) {
            sourceVmFromDb = getVmDAO().get(getVmIdFromSnapshot());
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
                LockMessagesMatchUtil.makeLockingPair(LockingGroup.VM, VdcBllMessages.ACTION_TYPE_FAILED_OBJECT_LOCKED));
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

    protected void updateOriginalTemplate(VmStatic vmStatic) {
        // do not update it - it is already correctly configured from the snapshot
    }

    public VmValidator createVmValidator(VM vm) {
        return new VmValidator(vm);
    }
}
