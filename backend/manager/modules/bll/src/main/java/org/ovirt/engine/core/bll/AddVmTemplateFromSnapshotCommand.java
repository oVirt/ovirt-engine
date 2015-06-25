package org.ovirt.engine.core.bll;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.bll.snapshots.SnapshotsValidator;
import org.ovirt.engine.core.common.VdcObjectType;
import org.ovirt.engine.core.common.action.AddVmTemplateFromSnapshotParameters;
import org.ovirt.engine.core.common.action.LockProperties;
import org.ovirt.engine.core.common.action.LockProperties.Scope;
import org.ovirt.engine.core.common.businessentities.Snapshot;
import org.ovirt.engine.core.common.businessentities.Snapshot.SnapshotStatus;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.storage.DiskImage;
import org.ovirt.engine.core.common.businessentities.storage.ImageStatus;
import org.ovirt.engine.core.common.errors.VdcBllMessages;
import org.ovirt.engine.core.common.locks.LockingGroup;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.common.queries.VdcQueryReturnValue;
import org.ovirt.engine.core.common.queries.VdcQueryType;
import org.ovirt.engine.core.common.utils.Pair;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.DiskImageDAO;
import org.ovirt.engine.core.utils.transaction.TransactionMethod;
import org.ovirt.engine.core.utils.transaction.TransactionSupport;

/**
 * This class adds a template VM from a snapshot (disks are cloned).
 */
@DisableInPrepareMode
@NonTransactiveCommandAttribute(forceCompensation = true)
public class AddVmTemplateFromSnapshotCommand<T extends AddVmTemplateFromSnapshotParameters> extends AddVmTemplateCommand<T> {

    private Snapshot cachedSnapshot;
    private List<DiskImage> cachedDisksFromDb;

    public AddVmTemplateFromSnapshotCommand(Guid commandId) {
        super(commandId);
    }

    public AddVmTemplateFromSnapshotCommand(T parameters) {
        this(parameters, null);
    }

    public AddVmTemplateFromSnapshotCommand(T parameters, CommandContext commandContext) {
        super(parameters, commandContext);
    }

    @Override
    protected void init() {
        if (getParameters().getVm() == null) {
            return;
        }
        setVdsGroupId(getParameters().getVm().getVdsGroupId());
        if (getVdsGroup() == null) {
            return;
        }
        setStoragePoolId(getVdsGroup().getStoragePoolId());
        VM vm = getVmFromConfiguration();
        if (vm == null) {
            return;
        }
        vm.setVdsGroupId(getVdsGroupId());
        vm.setStoragePoolId(getStoragePoolId());
        setVm(vm);
        getParameters().setVm(vm);
        setSnapshotName(getSnapshot() != null ? getSnapshot().getDescription() : null);
        super.init();
    }

    @Override
    protected boolean canDoAction() {
        SnapshotsValidator snapshotsValidator = createSnapshotsValidator();
        if (!validate(snapshotsValidator.snapshotExists(getSnapshot()))
                || !validate(snapshotsValidator.vmNotDuringSnapshot(getSnapshot().getVmId()))) {
            return false;
        }

        if (getVm() == null) {
            return failCanDoAction(VdcBllMessages.ACTION_TYPE_FAILED_VM_SNAPSHOT_HAS_NO_CONFIGURATION,
                    String.format("$VmName %1$s", getVmName()),
                    String.format("$SnapshotName %1$s", getSnapshotName()));
        }

        if (getVdsGroup() == null) {
            return failCanDoAction(VdcBllMessages.VMT_CLUSTER_IS_NOT_VALID);
        }

        return super.canDoAction();
    }

    protected SnapshotsValidator createSnapshotsValidator() {
        return new SnapshotsValidator();
    }

    @Override
    protected List<DiskImage> getVmDisksFromDB() {
        if (cachedDisksFromDb == null) {
            cachedDisksFromDb =
                    ImagesHandler.filterImageDisks(getVm().getDiskMap().values(),
                            false,
                            true,
                            true);
        }
        return cachedDisksFromDb;
    }

    @Override
    protected void addVmTemplateImages(Map<Guid, Guid> srcDeviceIdToTargetDeviceIdMapping) {
        List<DiskImage> diskImages = getVmDisksFromDB();
        if (diskImages.isEmpty()) {
            return;
        }

        for (DiskImage diskImage : diskImages) {
            if (diskImage.getImageStatus() == ImageStatus.ILLEGAL) {
                DiskImage snapshotImageInDb =
                        getDiskImageDao().getSnapshotById(diskImage.getImageId());
                if (snapshotImageInDb == null) {
                    // If the snapshot diskImage is null, it means the disk was probably
                    // erased after the snapshot was created.
                    // Create a disk to reflect the fact the disk existed during snapshot
                    saveIllegalDisk(diskImage);
                }
            } else {
                addVmTemplateImage(srcDeviceIdToTargetDeviceIdMapping, diskImage);
            }
        }
        if (!getReturnValue().getVdsmTaskIdList().isEmpty()) {
            lockSnapshot();
        }
    }

    private void saveIllegalDisk(final DiskImage diskImage) {
        TransactionSupport.executeInNewTransaction(new TransactionMethod<Void>() {
            @Override
            public Void runInTransaction() {
                // Allocating new IDs for image and disk as it's possible
                // that more than one clone will be made from this source
                // So this is required to avoid PK violation at DB.
                diskImage.setImageId(Guid.newGuid());
                diskImage.setId(Guid.newGuid());
                diskImage.setParentId(Guid.Empty);
                diskImage.setImageTemplateId(Guid.Empty);

                ImagesHandler.setDiskAlias(diskImage, getVm());
                ImagesHandler.addDiskImage(diskImage, getVmId());
                return null;
            }
        });
    }

    protected void lockSnapshot() {
        TransactionSupport.executeInNewTransaction(new TransactionMethod<Void>() {

            @Override
            public Void runInTransaction() {
                // Assumption - a snapshot can be locked only if in status OK, so if canDoAction passed
                // this is the status of the snapshot. In addition the newly added VM is in down status
                getCompensationContext().snapshotEntityStatus(getSnapshot());
                getSnapshotDAO().updateStatus(getParameters().getSourceSnapshotId(), SnapshotStatus.LOCKED);
                getCompensationContext().stateChanged();
                return null;
            }
        });
    }

    protected void unlockSnapshot() {
        // Assumption - this is last DB change of command, no need for compensation here
        getSnapshotDAO().updateStatus(getParameters().getSourceSnapshotId(), SnapshotStatus.OK);
    }

    private Snapshot getSnapshot() {
        if (cachedSnapshot == null) {
            cachedSnapshot = getSnapshotDAO().get(getParameters().getSourceSnapshotId());
        }
        return cachedSnapshot;
    }

    protected DiskImageDAO getDiskImageDao() {
        return getDbFacade().getDiskImageDao();
    }

    protected VM getVmFromConfiguration() {
        VdcQueryReturnValue queryReturnValue = runInternalQuery(
                VdcQueryType.GetVmConfigurationBySnapshot,
                new IdQueryParameters(getParameters().getSourceSnapshotId()));
        return queryReturnValue.getSucceeded() ? queryReturnValue.<VM>getReturnValue() : null;
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
                    LockMessagesMatchUtil.makeLockingPair(LockingGroup.VM, VdcBllMessages.ACTION_TYPE_FAILED_OBJECT_LOCKED));
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
}
