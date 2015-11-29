package org.ovirt.engine.core.bll.storage.disk.cinder;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import org.ovirt.engine.core.bll.InternalCommandAttribute;
import org.ovirt.engine.core.bll.LockMessagesMatchUtil;
import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.bll.storage.disk.image.ImagesHandler;
import org.ovirt.engine.core.bll.storage.disk.image.RemoveImageCommand;
import org.ovirt.engine.core.bll.tasks.CommandCoordinatorUtil;
import org.ovirt.engine.core.bll.tasks.interfaces.CommandCallback;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.VdcObjectType;
import org.ovirt.engine.core.common.action.RemoveCinderDiskParameters;
import org.ovirt.engine.core.common.action.RemoveCinderDiskVolumeParameters;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.action.VdcReturnValueBase;
import org.ovirt.engine.core.common.businessentities.Snapshot;
import org.ovirt.engine.core.common.businessentities.SubjectEntity;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VmDeviceId;
import org.ovirt.engine.core.common.businessentities.storage.CinderDisk;
import org.ovirt.engine.core.common.businessentities.storage.DiskImage;
import org.ovirt.engine.core.common.businessentities.storage.Image;
import org.ovirt.engine.core.common.businessentities.storage.ImageStatus;
import org.ovirt.engine.core.common.errors.EngineFault;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.common.locks.LockingGroup;
import org.ovirt.engine.core.common.utils.Pair;
import org.ovirt.engine.core.compat.CommandStatus;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.TransactionScopeOption;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.AuditLogDirector;
import org.ovirt.engine.core.utils.lock.EngineLock;
import org.ovirt.engine.core.utils.transaction.TransactionMethod;
import org.ovirt.engine.core.utils.transaction.TransactionSupport;

@InternalCommandAttribute
public class RemoveCinderDiskCommand<T extends RemoveCinderDiskParameters> extends RemoveImageCommand<T> {

    private CinderDisk cinderDisk;

    public RemoveCinderDiskCommand(T parameters) {
        super(parameters, null);
    }

    public RemoveCinderDiskCommand(T parameters, CommandContext commandContext) {
        super(parameters, commandContext);
    }

    public void executeCommand() {
        CinderDisk disk = getDisk();
        lockDisk();
        VM vm = getVmForNonShareableDiskImage(disk);

        // if the disk is not part of a vm (floating), there are no snapshots to update
        // so no lock is required.
        if (vm != null) {
            getParameters().setVmId(vm.getId());
            lockVmSnapshotsWithWait(vm);
        }
        getParameters().setRemovedVolume(getDisk());
        getParameters().setStorageDomainId(getDisk().getStorageIds().get(0));
        if (getDisk().getImageStatus() == ImageStatus.ILLEGAL) {
            handleRemoveCinderVolumesForIllegal();
            setCommandStatus(CommandStatus.SUCCEEDED);
            setSucceeded(true);
            return;
        }
        initCinderDiskVolumesParametersList(disk);
        if (!removeCinderVolume(0)) {
            getImageDao().updateStatusOfImagesByImageGroupId(getDisk().getId(), ImageStatus.ILLEGAL);
            setSucceeded(false);
            return;
        }
        persistCommand(getParameters().getParentCommand(), true);
        getReturnValue().setActionReturnValue(disk.getId());
        setSucceeded(true);
    }

    private void handleRemoveCinderVolumesForIllegal() {
        final List<DiskImage> diskSnapshots =
                DbFacade.getInstance().getDiskImageDao().getAllSnapshotsForImageGroup(cinderDisk.getId());
        ImagesHandler.sortImageList(diskSnapshots);
        TransactionSupport.executeInScope(TransactionScopeOption.Required,
                new TransactionMethod<Object>() {
                    @Override
                    public Object runInTransaction() {
                        int indCinderVolumeToDelete = diskSnapshots.size() - 1;
                        while (indCinderVolumeToDelete >= 0) {
                            CinderDisk cinderVolume = (CinderDisk) diskSnapshots.get(indCinderVolumeToDelete);
                            Snapshot updated = getSnapshotWithoutCinderVolume(cinderVolume);
                            removeDiskFromDb(cinderVolume, updated);
                            indCinderVolumeToDelete--;
                        }
                        return null;
                    }
                });

    }

    /**
     * Return the future command to be executed from the childCommandsParameters, we pass the storage's disk as the
     * subject entity only in the first call, since all the other volumes should be on the same Storage Domain.
     *
     * @param removedChildCommandParametersIndex
     *            - The index to fetch the child command parameters.
     * @return - The future command at the ChildCommandsParameters[removedChildCommandParametersIndex].
     */
    private Future<VdcReturnValueBase> getFutureRemoveCinderDiskVolume(int removedChildCommandParametersIndex) {
        return CommandCoordinatorUtil.executeAsyncCommand(VdcActionType.RemoveCinderDiskVolume,
                getParameters().getChildCommandsParameters().get(removedChildCommandParametersIndex),
                cloneContextAndDetachFromParent(),
                new SubjectEntity(VdcObjectType.Storage, getDisk().getStorageIds().get(0)));
    }

    protected boolean removeCinderVolume(int removedVolumeIndex) {
        RemoveCinderDiskVolumeParameters param = getParameters().getChildCommandsParameters().get(removedVolumeIndex);
        try {
            VdcReturnValueBase vdcReturnValueBase = getFutureRemoveCinderDiskVolume(removedVolumeIndex).get();
            if (vdcReturnValueBase == null || !vdcReturnValueBase.getSucceeded()) {
                handleExecutionFailure(param.getRemovedVolume(), vdcReturnValueBase);
                return false;
            }
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    private void initCinderDiskVolumesParametersList(CinderDisk cinderDisk) {
        // Get all the Cinder disk volumes ordered from the parent to the leaf.
        List<DiskImage> diskSnapshots =
                DbFacade.getInstance().getDiskImageDao().getAllSnapshotsForImageGroup(cinderDisk.getId());
        ImagesHandler.sortImageList(diskSnapshots);

        // Based on the ordered diskSnapshots list, initialize the childCommandsParameters list with parameters class
        // ordered from the last Cinder leaf volume up to the last volume to delete.
        int indVolumeToRemove = diskSnapshots.size() - 1;
        CinderDisk cinderDiskVolume = (CinderDisk) diskSnapshots.get(indVolumeToRemove);
        getParameters().getChildCommandsParameters().add(createChildParams(cinderDiskVolume));
        while (hasAnyMoreVolumesToDelete(cinderDiskVolume)) {
            indVolumeToRemove--;
            cinderDiskVolume = (CinderDisk) diskSnapshots.get(indVolumeToRemove);
            getParameters().getChildCommandsParameters().add(createChildParams(cinderDiskVolume));
        }
    }

    private RemoveCinderDiskVolumeParameters createChildParams(CinderDisk cinderDiskVolume) {
        RemoveCinderDiskVolumeParameters childParam = new RemoveCinderDiskVolumeParameters(cinderDiskVolume);
        childParam.setParentCommand(getActionType());
        childParam.setParentParameters(getParameters());
        return childParam;
    }

    private void removeDiskFromDb(final CinderDisk cinderVolume, Snapshot updated) {
            if (cinderVolume.getActive()) {
                // Get the base volume and set it as active, so the disk will not disappear from the disks view.
                Image baseVol = getImageDao().get(cinderVolume.getId());
                baseVol.setActive(true);
                getImageDao().update(baseVol);
            }
            getImageStorageDomainMapDao().remove(cinderVolume.getImageId());
            getImageDao().remove(cinderVolume.getImageId());
            getDiskImageDynamicDao().remove(cinderVolume.getImageId());
            if (updated != null) {
                getSnapshotDao().update(updated);
            }
    }

    protected void removeDiskFromDbCallBack(final CinderDisk cinderVolume) {
        final Snapshot updated = getSnapshotWithoutCinderVolume(cinderVolume);
        TransactionSupport.executeInScope(TransactionScopeOption.Required,
                new TransactionMethod<Object>() {
                    @Override
                    public Object runInTransaction() {
                        removeDiskFromDb(cinderVolume, updated);
                        return null;
                    }
                });
    }

    private Snapshot getSnapshotWithoutCinderVolume(CinderDisk lastCinderVolume) {
        Guid vmSnapshotId = lastCinderVolume.getVmSnapshotId();
        Snapshot updated = null;
        if (vmSnapshotId != null && !Guid.Empty.equals(vmSnapshotId)) {
            Snapshot snapshot = getSnapshotDao().get(vmSnapshotId);
            if (snapshot != null) {
                updated = ImagesHandler.prepareSnapshotConfigWithoutImageSingleImage(snapshot,
                        lastCinderVolume.getImageId());
            }
        }
        return updated;
    }

    @Override
    protected void endSuccessfully() {
        freeVmSnapshotsWithWait();
        getDbFacade().getVmDeviceDao().remove(new VmDeviceId(getParameters().getRemovedVolume().getId(), null));
        getBaseDiskDao().remove(getParameters().getRemovedVolume().getId());
        if (getParameters().getShouldBeLogged()) {
            new AuditLogDirector().log(this, AuditLogType.USER_FINISHED_REMOVE_DISK);
        }
        setSucceeded(true);
    }

    @Override
    protected void endWithFailure() {
        freeVmSnapshotsWithWait();
        int removedVolumeIndex = getParameters().getRemovedVolumeIndex();
        final CinderDisk cinderVolume =
                getParameters().getChildCommandsParameters().get(removedVolumeIndex).getRemovedVolume();
        getImageDao().updateStatusOfImagesByImageGroupId(getDisk().getId(), ImageStatus.ILLEGAL);
        addCustomValue("imageId", cinderVolume.getImageId().toString());
        new AuditLogDirector().log(this, AuditLogType.USER_FINISHED_FAILED_REMOVE_CINDER_DISK);
        setSucceeded(true);
    }

    protected void handleExecutionFailure(CinderDisk disk, VdcReturnValueBase vdcReturnValueBase) {
        log.error("Failed to remove cider volume id '{}' for disk id '{}'.", disk.getImageId(), disk.getId());
        EngineFault fault = vdcReturnValueBase == null ? new EngineFault() : vdcReturnValueBase.getFault();
        getReturnValue().setFault(fault);
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

    /**
     * The indication for Cinder disk to have no more volumes to delete is when the image id is equal to the disk id.
     *
     * @param leafCinderVolume
     *            - Cinder volume id to check if it has any more volumes to delete.
     * @return - True if the leafCinderVolume does no have other volumes to delete, false otherwise.
     */
    protected boolean hasAnyMoreVolumesToDelete(CinderDisk leafCinderVolume) {
        return !leafCinderVolume.getImageId().equals(leafCinderVolume.getId());
    }

    private void lockDisk() {
        ImagesHandler.updateAllDiskImageSnapshotsStatusWithCompensation(getDisk().getId(),
                ImageStatus.LOCKED,
                ImageStatus.ILLEGAL,
                getCompensationContext());
    }

    protected CinderDisk getDisk() {
        if (cinderDisk == null) {
            cinderDisk = (CinderDisk) getDiskDao().get(getParameters().getDiskId());
        }
        return cinderDisk;
    }

    private void freeVmSnapshotsWithWait() {
        if (getParameters().getVmId() != null) {
            EngineLock snapshotsEngineLock = new EngineLock();
            Map<String, Pair<String, String>> snapshotsExlusiveLockMap =
                    Collections.singletonMap(getParameters().getVmId().toString(),
                            LockMessagesMatchUtil.makeLockingPair(LockingGroup.VM_SNAPSHOTS,
                                    EngineMessage.ACTION_TYPE_FAILED_OBJECT_LOCKED));
            snapshotsEngineLock.setExclusiveLocks(snapshotsExlusiveLockMap);
            getLockManager().releaseLock(snapshotsEngineLock);
        }
    }

    @Override
    public CommandCallback getCallback() {
        return new RemoveCinderDiskCommandCallback();
    }

}
