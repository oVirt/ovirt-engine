package org.ovirt.engine.core.bll.storage.backup;

import static org.ovirt.engine.core.bll.storage.disk.image.DisksFilter.ONLY_ACTIVE;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.enterprise.inject.Instance;
import javax.enterprise.inject.Typed;
import javax.inject.Inject;

import org.ovirt.engine.core.bll.InternalCommandAttribute;
import org.ovirt.engine.core.bll.LockMessagesMatchUtil;
import org.ovirt.engine.core.bll.NonTransactiveCommandAttribute;
import org.ovirt.engine.core.bll.SerialChildCommandsExecutionCallback;
import org.ovirt.engine.core.bll.SerialChildExecutingCommand;
import org.ovirt.engine.core.bll.VmCommand;
import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.bll.storage.disk.image.DisksFilter;
import org.ovirt.engine.core.bll.storage.disk.image.VolumeBitmapsHelper;
import org.ovirt.engine.core.bll.tasks.interfaces.CommandCallback;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.action.LockProperties;
import org.ovirt.engine.core.common.action.ReconcileVmCheckpointsParameters;
import org.ovirt.engine.core.common.businessentities.VmCheckpoint;
import org.ovirt.engine.core.common.businessentities.storage.DiskImage;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.common.locks.LockingGroup;
import org.ovirt.engine.core.common.utils.Pair;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.AuditLogDirector;
import org.ovirt.engine.core.dao.DiskDao;
import org.ovirt.engine.core.dao.DiskImageDao;
import org.ovirt.engine.core.dao.VmCheckpointDao;
import org.ovirt.engine.core.utils.transaction.TransactionSupport;

/**
 * Reconciles the checkpoints of a down VM with the bitmaps on its disks - run when the VM went
 * down uncleanly (a host crash leaves the bitmaps of its VMs unflushed, and the checkpoint
 * metadata in libvirt dies with the domain).
 * <p>
 * For every disk of the VM the on-disk bitmaps are compared against the engine's checkpoints:
 * orphan and inconsistent bitmaps are removed, checkpoints that have no usable bitmap left are
 * removed from the database. A VM that boots afterwards has consistent disks, so the next
 * backup's redefine of the checkpoints succeeds instead of failing on a broken bitmap and taking
 * the whole chain down.
 * <p>
 * The bitmap removals run as RemoveVolumeBitmap children of this command, one checkpoint at a
 * time. The VM lock is held for the whole lifetime of the command - the asynchronous children
 * included - so a VM start (which takes the same key exclusively) cannot begin before the
 * repair has finished or failed. A failed reconcile is not fatal for the VM: the command ends,
 * the lock is released and the VM can be started; whatever is left broken is then reported by
 * the on-demand checks when the next backup or resize runs.
 */
@InternalCommandAttribute
@NonTransactiveCommandAttribute
public class ReconcileVmCheckpointsCommand<T extends ReconcileVmCheckpointsParameters> extends VmCommand<T>
        implements SerialChildExecutingCommand {

    @Inject
    private VmCheckpointDao vmCheckpointDao;
    @Inject
    private DiskDao diskDao;
    @Inject
    private DiskImageDao diskImageDao;
    @Inject
    private VolumeBitmapsHelper volumeBitmapsHelper;
    @Inject
    private AuditLogDirector auditLogDirector;
    @Inject
    @Typed(SerialChildCommandsExecutionCallback.class)
    private Instance<SerialChildCommandsExecutionCallback> callbackProvider;

    public ReconcileVmCheckpointsCommand(T parameters, CommandContext cmdContext) {
        super(parameters, cmdContext);
    }

    public ReconcileVmCheckpointsCommand(Guid commandId) {
        super(commandId);
    }

    @Override
    protected void executeVmCommand() {
        if (!getVm().isDown()) {
            log.info("VM '{}' is not down, skipping the checkpoints reconciliation", getVmId());
            setSucceeded(true);
            return;
        }

        Set<VmCheckpoint> checkpoints = new HashSet<>(vmCheckpointDao.getAllForVm(getVmId()));

        // A checkpoint's bitmap has to be removed from every volume of every disk of its chain,
        // a leftover on a consistent disk would be an orphan no later reconciliation would
        // clean up. Orphan bitmaps (no checkpoint owns them anymore) are collected during the
        // scan and removed per volume - an orphan in-use bitmap would otherwise block the
        // disk forever, with no checkpoint whose deletion would ever clean it.
        Set<String> orphanBitmaps = new HashSet<>();
        Set<Guid> brokenCheckpoints = new HashSet<>();
        List<DiskImage> activeDisks = DisksFilter.filterImageDisks(diskDao.getAllForVm(getVmId()), ONLY_ACTIVE);
        for (DiskImage diskImage : activeDisks) {
            Set<Guid> removed = volumeBitmapsHelper.reconcileDisk(
                    getStoragePoolId(),
                    diskImage,
                    checkpoints,
                    (volume, bitmapName) -> {
                        if (checkpoints.stream().noneMatch(cp -> cp.getId().toString().equals(bitmapName))) {
                            orphanBitmaps.add(bitmapName);
                        }
                    });
            if (removed == null) {
                log.warn("Could not query the bitmaps of VM '{}' disk '{}', skipping this disk",
                        getVmId(), diskImage.getId());
                continue;
            }
            brokenCheckpoints.addAll(removed);
        }

        if (brokenCheckpoints.isEmpty() && orphanBitmaps.isEmpty()) {
            log.info("The checkpoints of VM '{}' are consistent, nothing to reconcile", getVmId());
            setSucceeded(true);
            return;
        }

        getParameters().setReconcilingCheckpoints(brokenCheckpoints.stream()
                .map(this::findCheckpoint)
                .filter(Objects::nonNull)
                .collect(Collectors.toCollection(ArrayList::new)));
        getParameters().setOrphanBitmaps(new ArrayList<>(orphanBitmaps));
        persistCommandIfNeeded();
        setSucceeded(true);
    }

    @Override
    public boolean performNextOperation(int completedChildCount) {
        ReconcileVmCheckpointsParameters parameters = getParameters();
        if (parameters.getCompletedBitmapsCount() == parameters.getBitmapsCount()) {
            List<VmCheckpoint> removedCheckpoints = parameters.getReconcilingCheckpoints();
            log.info("All broken checkpoints of VM '{}' were reconciled", getVmName());
            TransactionSupport.executeInNewTransaction(() -> {
                removedCheckpoints.forEach(checkpoint -> vmCheckpointDao.remove(checkpoint.getId()));
                return null;
            });
            log.warn("Reconciled the checkpoints of VM '{}' after an unclean shutdown: "
                    + "removed checkpoint(s) '{}'",
                    getVmId(),
                    removedCheckpoints.stream().map(VmCheckpoint::getId).collect(Collectors.toList()));
            addCustomValue("checkpointId", removedCheckpoints.stream()
                    .map(cp -> cp.getId().toString())
                    .collect(Collectors.joining(", ")));
            addCustomValue("bitmaps", Stream.concat(
                    removedCheckpoints.stream().map(cp -> cp.getId().toString()),
                    parameters.getOrphanBitmaps().stream())
                    .collect(Collectors.joining(", ")));
            addCustomValue("VmName", getVmName());
            auditLogDirector.log(this, AuditLogType.VM_CHECKPOINT_BITMAPS_REMOVED);
            return false;
        }
        removeNextBitmap(parameters);
        parameters.advanceToNextBitmap();
        persistCommandIfNeeded();
        return true;
    }

    /**
     * Removes the next bitmap: a broken checkpoint's bitmap from every volume of every disk of
     * its chains, or an orphan from the volumes of all the VM's disks - it is not known which
     * disk an orphan belongs to, and a leftover on any volume would stay there forever.
     * The RemoveVolumeBitmap children run as vdsm storage jobs; the serial callback waits for
     * each before the next, so the command - and with it the VM lock - lives until the last
     * edit hits the disk.
     */
    private void removeNextBitmap(ReconcileVmCheckpointsParameters parameters) {
        int completed = parameters.getCompletedBitmapsCount();
        if (completed < parameters.getReconcilingCheckpoints().size()) {
            VmCheckpoint checkpoint = parameters.getReconcilingCheckpoints().get(completed);
            for (DiskImage diskImage : vmCheckpointDao.getDisksByCheckpointId(checkpoint.getId())) {
                removeBitmapFromDiskChain(diskImage, checkpoint.getId().toString());
            }
        } else {
            String orphan = parameters.getOrphanBitmaps().get(completed
                    - parameters.getReconcilingCheckpoints().size());
            for (DiskImage diskImage : DisksFilter.filterImageDisks(
                    diskDao.getAllForVm(getVmId()), ONLY_ACTIVE)) {
                removeBitmapFromDiskChain(diskImage, orphan);
            }
        }
    }

    private void removeBitmapFromDiskChain(DiskImage diskImage, String bitmapName) {
        for (DiskImage volume : diskImageDao.getAllSnapshotsForLeaf(diskImage.getImageId())) {
            if (!volume.isQcowFormat()) {
                continue;
            }
            removeDiskBitmap(volume, bitmapName);
        }
    }

    private VmCheckpoint findCheckpoint(Guid checkpointId) {
        return vmCheckpointDao.get(checkpointId);
    }

    @Override
    protected void setActionMessageParameters() {
        addValidationMessage(EngineMessage.VAR__ACTION__UPDATE);
        addValidationMessage(EngineMessage.VAR__TYPE__VM);
    }

    /**
     * The lock must span the whole lifetime of the command, the asynchronous bitmap removal
     * children included - a VM start must not slip in between the dispatch of a bitmap removal
     * and its completion on storage.
     */
    @Override
    protected LockProperties applyLockProperties(LockProperties lockProperties) {
        return lockProperties.withScope(LockProperties.Scope.Command);
    }

    @Override
    protected Map<String, Pair<String, String>> getSharedLocks() {
        return Collections.singletonMap(getParameters().getVmId().toString(),
                LockMessagesMatchUtil.makeLockingPair(LockingGroup.VM, EngineMessage.ACTION_TYPE_FAILED_VM_IS_LOCKED));
    }

    /**
     * The reconcile is a repair after a crash: when a single bitmap cannot be removed the
     * remaining checkpoints are still repaired, a half-repaired chain is better than a whole
     * chain of broken bitmaps.
     */
    @Override
    public boolean ignoreChildCommandFailure() {
        return true;
    }

    @Override
    public CommandCallback getCallback() {
        return callbackProvider.get();
    }

    @Override
    public AuditLogType getAuditLogTypeValue() {
        addCustomValue("VmName", getVmName());
        return AuditLogType.VM_CHECKPOINT_BITMAPS_REMOVED;
    }
}
