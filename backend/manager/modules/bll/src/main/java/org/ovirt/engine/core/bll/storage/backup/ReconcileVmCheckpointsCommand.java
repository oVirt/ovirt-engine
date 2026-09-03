package org.ovirt.engine.core.bll.storage.backup;

import static org.ovirt.engine.core.bll.storage.disk.image.DisksFilter.ONLY_ACTIVE;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import javax.inject.Inject;

import org.ovirt.engine.core.bll.InternalCommandAttribute;
import org.ovirt.engine.core.bll.NonTransactiveCommandAttribute;
import org.ovirt.engine.core.bll.VmCommand;
import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.bll.storage.disk.image.DisksFilter;
import org.ovirt.engine.core.bll.storage.disk.image.VolumeBitmapsHelper;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.action.VmOperationParameterBase;
import org.ovirt.engine.core.common.businessentities.VmCheckpoint;
import org.ovirt.engine.core.common.businessentities.storage.DiskImage;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.AuditLogDirector;
import org.ovirt.engine.core.dao.DiskDao;
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
 */
@InternalCommandAttribute
@NonTransactiveCommandAttribute
public class ReconcileVmCheckpointsCommand<T extends VmOperationParameterBase> extends VmCommand<T> {

    @Inject
    private VmCheckpointDao vmCheckpointDao;
    @Inject
    private DiskDao diskDao;
    @Inject
    private VolumeBitmapsHelper volumeBitmapsHelper;
    @Inject
    private AuditLogDirector auditLogDirector;

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
        Set<Guid> removedCheckpoints = new HashSet<>();
        Set<String> removedBitmaps = new HashSet<>();
        List<DiskImage> activeDisks = DisksFilter.filterImageDisks(
                diskDao.getAllForVm(getVmId()), ONLY_ACTIVE);
        for (DiskImage diskImage : activeDisks) {
            Set<Guid> removed = volumeBitmapsHelper.reconcileDisk(
                    getStoragePoolId(),
                    diskImage,
                    checkpoints,
                    (volume, bitmapName) -> {
                        if (removeDiskBitmap(volume, bitmapName)) {
                            removedBitmaps.add(bitmapName);
                        }
                    });
            if (removed == null) {
                // The bitmaps of this disk could not be queried - skip the whole VM
                log.warn("Could not query the bitmaps of VM '{}' disk '{}', skipping the checkpoints "
                        + "reconciliation", getVmId(), diskImage.getId());
                setSucceeded(true);
                return;
            }
            removedCheckpoints.addAll(removed);
        }

        if (!removedCheckpoints.isEmpty()) {
            TransactionSupport.executeInNewTransaction(() -> {
                removedCheckpoints.forEach(vmCheckpointDao::remove);
                return null;
            });
            log.warn("Reconciled the checkpoints of VM '{}' after an unclean shutdown: "
                    + "removed bitmap(s) '{}', removed checkpoint(s) '{}'",
                    getVmId(),
                    String.join(", ", removedBitmaps),
                    removedCheckpoints);
            addCustomValue("checkpointId", removedCheckpoints.stream()
                    .map(Guid::toString)
                    .collect(Collectors.joining(", ")));
            addCustomValue("bitmaps", String.join(", ", removedBitmaps));
            addCustomValue("VmName", getVmName());
            auditLogDirector.log(this, AuditLogType.VM_CHECKPOINT_BITMAPS_REMOVED);
        }
        setSucceeded(true);
    }

    @Override
    protected void setActionMessageParameters() {
        addValidationMessage(EngineMessage.VAR__ACTION__UPDATE);
        addValidationMessage(EngineMessage.VAR__TYPE__VM);
    }

    @Override
    public AuditLogType getAuditLogTypeValue() {
        addCustomValue("VmName", getVmName());
        return AuditLogType.VM_CHECKPOINT_BITMAPS_REMOVED;
    }
}
