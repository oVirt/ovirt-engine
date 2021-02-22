package org.ovirt.engine.core.bll.storage.backup;

import java.util.Collections;
import java.util.Map;

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
import org.ovirt.engine.core.bll.tasks.interfaces.CommandCallback;
import org.ovirt.engine.core.common.action.ActionParametersBase;
import org.ovirt.engine.core.common.action.ActionReturnValue;
import org.ovirt.engine.core.common.action.ActionType;
import org.ovirt.engine.core.common.action.AddDiskParameters;
import org.ovirt.engine.core.common.action.LockProperties;
import org.ovirt.engine.core.common.action.VmBackupParameters;
import org.ovirt.engine.core.common.businessentities.VmBackup;
import org.ovirt.engine.core.common.businessentities.storage.DiskImage;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.common.locks.LockingGroup;
import org.ovirt.engine.core.common.utils.Pair;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.DiskDao;

@InternalCommandAttribute
@NonTransactiveCommandAttribute
public class CreateScratchDisksCommand<T extends VmBackupParameters> extends VmCommand<T> implements SerialChildExecutingCommand {
    @Inject
    private DiskDao diskDao;
    @Inject
    @Typed(SerialChildCommandsExecutionCallback.class)
    private Instance<SerialChildCommandsExecutionCallback> callbackProvider;

    public CreateScratchDisksCommand(T parameters, CommandContext cmdContext) {
        super(parameters, cmdContext);
    }

    public CreateScratchDisksCommand(Guid commandId) {
        super(commandId);
    }

    @Override
    public void init() {
        setVmId(getParameters().getVmBackup().getVmId());
    }

    @Override
    protected void executeCommand() {
        // Set the scratch disks map to return even in case of a failure.
        setActionReturnValue(getParameters().getScratchDisksMap());

        for (DiskImage disk : getParameters().getVmBackup().getDisks()) {
            ActionReturnValue returnValue = addDisk(disk);
            if (!returnValue.getSucceeded() || returnValue.getActionReturnValue() == null) {
                log.error("Failed to create Scratch disk for disk ID '{}'", disk.getId());
                return;
            }

            Guid scratchDiskId = returnValue.getActionReturnValue();
            DiskImage scratchDiskImage = (DiskImage) diskDao.get(scratchDiskId);
            // Add the created scratch disk image to the disks map, the scratch disk
            // path will be updated after preparing the scratch disk.
            getParameters().getScratchDisksMap().put(disk.getId(), new Pair<>(scratchDiskImage, null));
        }

        persistCommandIfNeeded();
        setSucceeded(true);
    }

    private ActionReturnValue addDisk(DiskImage disk) {
        AddDiskParameters parameters = new AddDiskParameters(disk);
        parameters.setStorageDomainId(disk.getStorageIds().get(0));
        parameters.setParentCommand(getActionType());
        parameters.setParentParameters(getParameters());
        parameters.setEndProcedure(ActionParametersBase.EndProcedure.COMMAND_MANAGED);

        return runInternalAction(ActionType.CreateScratchDisk,
                parameters,
                getContext().clone().withoutLock());
    }

    @Override
    public boolean performNextOperation(int completedChildCount) {
        VmBackup vmBackup = getParameters().getVmBackup();
        if (vmBackup.getDisks().size() == completedChildCount) {
            log.info("All scratch disks created for VM '{}' backup '{}'", getVm().getName(), vmBackup.getId());
            return false;
        }
        persistCommandIfNeeded();
        return true;
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
    public CommandCallback getCallback() {
        return callbackProvider.get();
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
