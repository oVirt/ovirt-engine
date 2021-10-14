package org.ovirt.engine.core.bll.storage.backup;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.ovirt.engine.core.bll.InternalCommandAttribute;
import org.ovirt.engine.core.bll.LockMessagesMatchUtil;
import org.ovirt.engine.core.bll.NonTransactiveCommandAttribute;
import org.ovirt.engine.core.bll.VmCommand;
import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.common.action.ActionType;
import org.ovirt.engine.core.common.action.LockProperties;
import org.ovirt.engine.core.common.action.RemoveDiskParameters;
import org.ovirt.engine.core.common.action.VmBackupParameters;
import org.ovirt.engine.core.common.businessentities.storage.DiskImage;
import org.ovirt.engine.core.common.businessentities.storage.ImageStatus;
import org.ovirt.engine.core.common.errors.EngineException;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.common.locks.LockingGroup;
import org.ovirt.engine.core.common.utils.Pair;
import org.ovirt.engine.core.compat.Guid;

@InternalCommandAttribute
@NonTransactiveCommandAttribute
public class RemoveScratchDisksCommand<T extends VmBackupParameters> extends VmCommand<T> {

    public RemoveScratchDisksCommand(T parameters, CommandContext cmdContext) {
        super(parameters, cmdContext);
    }

    public RemoveScratchDisksCommand(Guid commandId) {
        super(commandId);
    }

    @Override
    public void init() {
        setVmId(getParameters().getVmBackup().getVmId());
        setVdsId(getParameters().getVmBackup().getHostId());
    }

    @Override
    protected void executeCommand() {
        List<DiskImage> scratchDisks = getParameters().getScratchDisksMap()
                .values()
                .stream()
                .map(Pair::getFirst)
                .collect(Collectors.toList());

        // Scratch disks remain locked throughout the backup.
        // In order to remove the scratch disks they should be unlocked.
        // Collect the created scratch disks IDs.
        Set<Guid> scratchDisksIds = scratchDisks
                .stream()
                .map(DiskImage::getId)
                .collect(Collectors.toCollection(HashSet::new));
        imagesHandler.updateAllDiskImagesSnapshotsStatusInTransactionWithCompensation(
                scratchDisksIds,
                ImageStatus.OK,
                ImageStatus.ILLEGAL,
                getCompensationContext());

        // Best effort to teardown and remove the scratch disks.
        scratchDisks.forEach(this::teardownScratchDisk);
        scratchDisks.forEach(this::removeScratchDisk);

        persistCommandIfNeeded();
        setSucceeded(true);
    }

    private void teardownScratchDisk(DiskImage diskImage) {
        log.info("Teardown '{}/{}' on the VM host '{}'", diskImage.getId(), diskImage.getImageId(), getVdsId());
        try {
            imagesHandler.teardownImage(getStoragePoolId(),
                    diskImage.getStorageIds().get(0),
                    diskImage.getId(),
                    diskImage.getImageId(),
                    getVdsId());
        } catch (EngineException e) {
            log.error("Failed to Teardown image '{}/{}' on the VM host '{}'",
                    diskImage.getId(),
                    diskImage.getImageId(),
                    getVdsId());
        }
    }

    private void removeScratchDisk(DiskImage diskImage) {
        log.info("Remove scratch disk '{}'", diskImage.getDiskAlias());
        runInternalAction(ActionType.RemoveDisk,
                new RemoveDiskParameters(diskImage.getId()),
                getContext().clone().withoutLock());
    }

    @Override
    protected void setActionMessageParameters() {
        addValidationMessage(EngineMessage.VAR__ACTION__SCRATCH_DISK);
        addValidationMessage(EngineMessage.VAR__ACTION__BACKUP);
        addValidationMessage(EngineMessage.VAR__TYPE__VM);
    }

    @Override
    protected void endVmCommand() {
        endActionOnDisks();
        setSucceeded(true);
    }

    @Override
    protected LockProperties applyLockProperties(LockProperties lockProperties) {
        return lockProperties.withScope(LockProperties.Scope.Command);
    }

    @Override
    protected Map<String, Pair<String, String>> getSharedLocks() {
        return Collections.singletonMap(getParameters().getVmBackup().getVmId().toString(),
                LockMessagesMatchUtil.makeLockingPair(LockingGroup.VM, EngineMessage.ACTION_TYPE_FAILED_VM_IS_LOCKED));
    }
}
