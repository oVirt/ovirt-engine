package org.ovirt.engine.core.bll;

import static org.ovirt.engine.core.bll.storage.disk.image.DisksFilter.ONLY_ACTIVE;
import static org.ovirt.engine.core.bll.storage.disk.image.DisksFilter.ONLY_PLUGGED;
import static org.ovirt.engine.core.bll.storage.disk.image.DisksFilter.ONLY_SNAPABLE;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import org.apache.commons.lang.StringUtils;
import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.bll.context.CompensationContext;
import org.ovirt.engine.core.bll.storage.disk.image.DisksFilter;
import org.ovirt.engine.core.bll.storage.disk.image.ImagesHandler;
import org.ovirt.engine.core.common.VdcObjectType;
import org.ovirt.engine.core.common.action.ActionType;
import org.ovirt.engine.core.common.action.AddVmTemplateFromSnapshotParameters;
import org.ovirt.engine.core.common.action.CreateAllTemplateDisksFromSnapshotParameters;
import org.ovirt.engine.core.common.action.CreateAllTemplateDisksParameters;
import org.ovirt.engine.core.common.action.LockProperties;
import org.ovirt.engine.core.common.action.LockProperties.Scope;
import org.ovirt.engine.core.common.businessentities.Snapshot;
import org.ovirt.engine.core.common.businessentities.Snapshot.SnapshotStatus;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VMStatus;
import org.ovirt.engine.core.common.businessentities.VmDynamic;
import org.ovirt.engine.core.common.businessentities.storage.DiskImage;
import org.ovirt.engine.core.common.businessentities.storage.ImageStatus;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.common.locks.LockingGroup;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.common.queries.QueryReturnValue;
import org.ovirt.engine.core.common.queries.QueryType;
import org.ovirt.engine.core.common.utils.Pair;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.DiskImageDao;
import org.ovirt.engine.core.dao.SnapshotDao;
import org.ovirt.engine.core.utils.transaction.TransactionSupport;

/**
 * This class adds a template VM from a snapshot (disks are cloned).
 */
@DisableInPrepareMode
@NonTransactiveCommandAttribute(forceCompensation = true)
public class AddVmTemplateFromSnapshotCommand<T extends AddVmTemplateFromSnapshotParameters> extends AddVmTemplateCommand<T> {

    @Inject
    private DiskImageDao diskImageDao;

    @Inject
    private SnapshotDao snapshotDao;

    private Snapshot cachedSnapshot;

    public AddVmTemplateFromSnapshotCommand(Guid commandId) {
        super(commandId);
    }

    public AddVmTemplateFromSnapshotCommand(T parameters, CommandContext commandContext) {
        super(parameters, commandContext);
    }

    @Override
    protected void init() {
        if (getParameters().getVm() == null) {
            return;
        }
        setClusterId(getParameters().getVm().getClusterId());
        if (getCluster() == null) {
            return;
        }
        setStoragePoolId(getCluster().getStoragePoolId());
        VM vm = getVmFromConfiguration();
        if (vm == null) {
            return;
        }
        vm.setStaticData(getParameters().getMasterVm());
        setVm(vm);
        setSnapshotName(getSnapshot() != null ? getSnapshot().getDescription() : null);
        super.init();
    }

    @Override
    protected boolean validate() {
        if (!validate(snapshotsValidator.snapshotExists(getSnapshot()))
                || !validate(snapshotsValidator.vmNotDuringSnapshot(getSnapshot().getVmId()))
                || !validate(snapshotsValidator.snapshotVmConfigurationBroken(getSnapshot(), getVmName()))) {
            return false;
        }

        if (getVm() == null) {
            return failValidation(EngineMessage.ACTION_TYPE_FAILED_VM_SNAPSHOT_HAS_NO_CONFIGURATION,
                    String.format("$VmName %1$s", getVmName()),
                    String.format("$SnapshotName %1$s", getSnapshotName()));
        }

        if (getCluster() == null) {
            return failValidation(EngineMessage.VMT_CLUSTER_IS_NOT_VALID);
        }

        return super.validate();
    }

    @Override
    protected boolean isVmStatusValid(VMStatus status) {
        if (getSnapshot().getType() == Snapshot.SnapshotType.ACTIVE) {
            return status == VMStatus.Down;
        }
        return true;
    }

    @Override
    protected List<DiskImage> getVmDisksFromDB() {
        List<DiskImage> disksFromDb =
                DisksFilter.filterImageDisks(getVm().getDiskMap().values(), ONLY_SNAPABLE, ONLY_ACTIVE);
        disksFromDb.addAll(DisksFilter.filterCinderDisks(getVm().getDiskMap().values(), ONLY_PLUGGED));
        return disksFromDb;
    }

    @Override
    protected CreateAllTemplateDisksParameters buildCreateAllTemplateDisksParameters() {
        CreateAllTemplateDisksFromSnapshotParameters parameters =
                new CreateAllTemplateDisksFromSnapshotParameters(getVm() != null ? getVmId() : Guid.Empty);
        parameters.setSnapshotId(getParameters().getSourceSnapshotId());
        fillCreateAllTemplateDisksParameters(parameters);
        return parameters;
    }

    @Override
    protected Map<Guid, Guid> addAllTemplateDisks() {
        processIllegalDisks();
        Map<Guid, Guid> srcDeviceIdToTargetDeviceIdMapping = super.addAllTemplateDisks();
        if (!srcDeviceIdToTargetDeviceIdMapping.isEmpty()) {
            lockSnapshot();
        }
        return srcDeviceIdToTargetDeviceIdMapping;
    }

    private void processIllegalDisks() {
        for (DiskImage diskImage : images) {
            if (diskImage.getImageStatus() == ImageStatus.ILLEGAL) {
                DiskImage snapshotImageInDb =
                        diskImageDao.getSnapshotById(diskImage.getImageId());
                if (snapshotImageInDb == null) {
                    // If the snapshot diskImage is null, it means the disk was probably
                    // erased after the snapshot was created.
                    // Create a disk to reflect the fact the disk existed during snapshot
                    saveIllegalDisk(diskImage);
                }
            }
        }
    }

    private void saveIllegalDisk(final DiskImage diskImage) {
        TransactionSupport.executeInNewTransaction(() -> {
            // Allocating new IDs for image and disk as it's possible
            // that more than one clone will be made from this source
            // So this is required to avoid PK violation at DB.
            diskImage.setImageId(Guid.newGuid());
            diskImage.setId(Guid.newGuid());
            diskImage.setParentId(Guid.Empty);
            diskImage.setImageTemplateId(Guid.Empty);

            ImagesHandler.setDiskAlias(diskImage, getVm());
            imagesHandler.addDiskImage(diskImage, getVmId());
            return null;
        });
    }

    @Override
    protected ActionType getAddAllTemplateDisksActionType() {
        return ActionType.CreateAllTemplateDisksFromSnapshot;
    }

    /**
     * Assumption - a snapshot can be locked only if in status OK, so if validate passed
     * this is the status of the snapshot. In addition the newly added VM is in down status
     */
    protected void lockSnapshot() {
        TransactionSupport.executeInNewTransaction(() -> {
            getCompensationContext().snapshotEntityStatus(getSnapshot());
            snapshotDao.updateStatus(getParameters().getSourceSnapshotId(), SnapshotStatus.LOCKED);
            getCompensationContext().stateChanged();
            return null;
        });
    }

    protected void unlockSnapshot() {
        // Assumption - this is last DB change of command, no need for compensation here
        snapshotDao.updateStatus(getParameters().getSourceSnapshotId(), SnapshotStatus.OK);
    }

    private Snapshot getSnapshot() {
        if (cachedSnapshot == null) {
            cachedSnapshot = snapshotDao.get(getParameters().getSourceSnapshotId());
        }
        return cachedSnapshot;
    }

    protected VM getVmFromConfiguration() {
        QueryReturnValue queryReturnValue = runInternalQuery(
                QueryType.GetVmConfigurationBySnapshot,
                new IdQueryParameters(getParameters().getSourceSnapshotId()));
        return queryReturnValue.getSucceeded() ? queryReturnValue.getReturnValue() : null;
    }

    @Override
    protected LockProperties applyLockProperties(LockProperties lockProperties) {
        return lockProperties.withScope(Scope.Command);
    }

    @Override
    protected Map<String, Pair<String, String>> getSharedLocks() {
        Map<String, Pair<String, String>> locks = new HashMap<>();
        Map<String, Pair<String, String>> parentLocks = super.getSharedLocks();
        if (parentLocks != null) {
            locks.putAll(parentLocks);
        }
        if (getVm() != null) {
            locks.put(getVmId().toString(),
                    LockMessagesMatchUtil.makeLockingPair(LockingGroup.VM, EngineMessage.ACTION_TYPE_FAILED_OBJECT_LOCKED));
        }
        return locks;
    }

    @Override
    protected void endSuccessfully() {
        super.endSuccessfully();
        unlockSnapshot();
    }

    @Override
    protected void endWithFailure() {
        super.endWithFailure();
        unlockSnapshot();
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
    protected void lockOps(VmDynamic vmDynamic, CompensationContext context) {
        // do nothing
    }

    @Override
    protected void unLockVm(VM vm) {
        // do nothing
    }

    @Override
    protected void endUnlockOps() {
        vmTemplateHandler.unlockVmTemplate(getVmTemplateId());
    }

}
