package org.ovirt.engine.core.bll.storage.disk.cinder;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.enterprise.inject.Instance;
import javax.enterprise.inject.Typed;
import javax.inject.Inject;

import org.ovirt.engine.core.bll.InternalCommandAttribute;
import org.ovirt.engine.core.bll.LockMessagesMatchUtil;
import org.ovirt.engine.core.bll.SerialChildCommandsExecutionCallback;
import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.bll.storage.disk.image.ImagesHandler;
import org.ovirt.engine.core.bll.tasks.interfaces.CommandCallback;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.VdcObjectType;
import org.ovirt.engine.core.common.action.RemoveCinderDiskParameters;
import org.ovirt.engine.core.common.businessentities.Snapshot;
import org.ovirt.engine.core.common.businessentities.SubjectEntity;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VmDeviceId;
import org.ovirt.engine.core.common.businessentities.storage.CinderDisk;
import org.ovirt.engine.core.common.businessentities.storage.DiskImage;
import org.ovirt.engine.core.common.businessentities.storage.ImageStatus;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.common.locks.LockingGroup;
import org.ovirt.engine.core.common.utils.Pair;
import org.ovirt.engine.core.compat.CommandStatus;
import org.ovirt.engine.core.compat.TransactionScopeOption;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.AuditLogDirector;
import org.ovirt.engine.core.dao.BaseDiskDao;
import org.ovirt.engine.core.dao.DiskDao;
import org.ovirt.engine.core.dao.DiskImageDao;
import org.ovirt.engine.core.dao.ImageDao;
import org.ovirt.engine.core.dao.VmDeviceDao;
import org.ovirt.engine.core.utils.lock.EngineLock;
import org.ovirt.engine.core.utils.transaction.TransactionSupport;

@InternalCommandAttribute
public class RemoveCinderDiskCommand<T extends RemoveCinderDiskParameters> extends RemoveCinderVolumeParentCommand<T> {

    @Inject
    private AuditLogDirector auditLogDirector;

    @Inject
    private ImageDao imageDao;
    @Inject
    private VmDeviceDao vmDeviceDao;
    @Inject
    private BaseDiskDao baseDiskDao;
    @Inject
    private DiskDao diskDao;

    private CinderDisk cinderDisk;
    @Inject
    private DiskImageDao diskImageDao;
    @Inject
    @Typed(SerialChildCommandsExecutionCallback.class)
    private Instance<SerialChildCommandsExecutionCallback> callbackProvider;

    public RemoveCinderDiskCommand(T parameters, CommandContext commandContext) {
        super(parameters, commandContext);
    }

    @Override
    public void executeCommand() {
        CinderDisk disk = getDisk();
        lockDisk();
        VM vm = getVmForNonShareableDiskImage(disk);

        // if the disk is not part of a vm (floating), there are no snapshots to update
        // so no lock is required.
        if (getParameters().isLockVM() && vm != null) {
            getParameters().setVmId(vm.getId());
            lockVmSnapshotsWithWait(vm);
        }
        getParameters().setRemovedVolume(getDisk());
        getParameters().setStorageDomainId(getDisk().getStorageIds().get(0));
        getParameters().setUpdateSnapshot(true);
        if (getDisk().getImageStatus() == ImageStatus.ILLEGAL) {
            handleRemoveCinderVolumesForIllegal();
            setCommandStatus(CommandStatus.SUCCEEDED);
            setSucceeded(true);
            return;
        }
        // Get the first volume to delete from.
        CinderDisk parentVolume = (CinderDisk) diskImageDao.getSnapshotById(disk.getId());
        initCinderDiskVolumesParametersList(parentVolume);
        if (!removeCinderVolume(0)) {
            imageDao.updateStatusOfImagesByImageGroupId(getDisk().getId(), ImageStatus.ILLEGAL);
            setSucceeded(false);
            return;
        }
        persistCommand(getParameters().getParentCommand(), true);
        getReturnValue().setActionReturnValue(disk.getId());
        setSucceeded(true);
    }

    private void handleRemoveCinderVolumesForIllegal() {
        final List<DiskImage> diskSnapshots = diskImageDao.getAllSnapshotsForImageGroup(cinderDisk.getId());
        ImagesHandler.sortImageList(diskSnapshots);
        TransactionSupport.executeInScope(TransactionScopeOption.Required,
                () -> {
                    int indCinderVolumeToDelete = diskSnapshots.size() - 1;
                    while (indCinderVolumeToDelete >= 0) {
                        CinderDisk cinderVolume = (CinderDisk) diskSnapshots.get(indCinderVolumeToDelete);
                        Snapshot updated = getSnapshotWithoutCinderVolume(cinderVolume);
                        removeDiskFromDb(cinderVolume, updated);
                        indCinderVolumeToDelete--;
                    }
                    return null;
                });

    }

    @Override
    protected void endSuccessfully() {
        freeVmSnapshotsWithWait();
        vmDeviceDao.remove(new VmDeviceId(getParameters().getRemovedVolume().getId(), null));
        baseDiskDao.remove(getParameters().getRemovedVolume().getId());
        if (getParameters().getShouldBeLogged()) {
            auditLogDirector.log(this, AuditLogType.USER_FINISHED_REMOVE_DISK);
        }
        setSucceeded(true);
    }

    @Override
    protected void endWithFailure() {
        freeVmSnapshotsWithWait();
        int removedVolumeIndex = getParameters().getRemovedVolumeIndex();
        final CinderDisk cinderVolume =
                getParameters().getChildCommandsParameters().get(removedVolumeIndex).getRemovedVolume();
        imageDao.updateStatusOfImagesByImageGroupId(getDisk().getId(), ImageStatus.ILLEGAL);
        addCustomValue("imageId", cinderVolume.getImageId().toString());
        auditLogDirector.log(this, AuditLogType.USER_FINISHED_FAILED_REMOVE_CINDER_DISK);
        setSucceeded(true);
    }

    @Override
    public Map<String, String> getJobMessageProperties() {
        if (jobProperties == null) {
            jobProperties = super.getJobMessageProperties();
            jobProperties.put("diskalias", getDiskAlias());
        }
        return jobProperties;
    }

    public String getDiskAlias() {
        return getParameters().getRemovedVolume().getDiskAlias();
    }

    private void lockDisk() {
        imagesHandler.updateAllDiskImageSnapshotsStatusWithCompensation(getDisk().getId(),
                ImageStatus.LOCKED,
                ImageStatus.ILLEGAL,
                getCompensationContext());
    }

    protected CinderDisk getDisk() {
        if (cinderDisk == null) {
            cinderDisk = (CinderDisk) diskDao.get(getParameters().getDiskId());
        }
        return cinderDisk;
    }

    private void freeVmSnapshotsWithWait() {
        if (getParameters().getVmId() != null) {
            EngineLock snapshotsEngineLock = new EngineLock();
            Map<String, Pair<String, String>> snapshotsExclusiveLockMap =
                    Collections.singletonMap(getParameters().getVmId().toString(),
                            LockMessagesMatchUtil.makeLockingPair(LockingGroup.VM_SNAPSHOTS,
                                    EngineMessage.ACTION_TYPE_FAILED_OBJECT_LOCKED));
            snapshotsEngineLock.setExclusiveLocks(snapshotsExclusiveLockMap);
            lockManager.releaseLock(snapshotsEngineLock);
        }
    }

    @Override
    public CommandCallback getCallback() {
        return callbackProvider.get();
    }

    @Override
    protected Collection<SubjectEntity> getSubjectEntities() {
        return Collections.singleton(new SubjectEntity(VdcObjectType.Storage, getDisk().getStorageIds().get(0)));
    }
}
