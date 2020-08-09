package org.ovirt.engine.core.bll.storage.disk.managedblock;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import javax.inject.Inject;

import org.ovirt.engine.core.bll.CommandBase;
import org.ovirt.engine.core.bll.InternalCommandAttribute;
import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.bll.storage.disk.image.DisksFilter;
import org.ovirt.engine.core.bll.tasks.CommandCoordinatorUtil;
import org.ovirt.engine.core.bll.utils.PermissionSubject;
import org.ovirt.engine.core.common.action.ActionParametersBase.EndProcedure;
import org.ovirt.engine.core.common.action.ActionReturnValue;
import org.ovirt.engine.core.common.action.ActionType;
import org.ovirt.engine.core.common.action.RemoveAllManagedBlockStorageDisksParameters;
import org.ovirt.engine.core.common.action.RemoveDiskParameters;
import org.ovirt.engine.core.common.businessentities.storage.ManagedBlockStorageDisk;
import org.ovirt.engine.core.common.utils.Pair;
import org.ovirt.engine.core.dao.DiskDao;

@InternalCommandAttribute
public class RemoveAllManagedBlockStorageDisksCommand<T extends RemoveAllManagedBlockStorageDisksParameters> extends CommandBase<T> {
    @Inject
    private DiskDao diskDao;
    @Inject
    private CommandCoordinatorUtil commandCoordinatorUtil;

    public RemoveAllManagedBlockStorageDisksCommand(T parameters, CommandContext commandContext) {
        super(parameters, commandContext);
    }

    @Override
    public boolean validate() {
        return true;
    }

    @Override
    protected void executeCommand() {
        Collection<ManagedBlockStorageDisk> failedRemoving = new LinkedList<>();
        for (final ManagedBlockStorageDisk managedBlockDisk : getManagedBlockDisksToBeRemoved()) {
            if (Boolean.TRUE.equals(managedBlockDisk.getActive())) {
                ActionReturnValue actionReturnValuernValue = removeManagedBlockDisk(managedBlockDisk);
                if (actionReturnValuernValue == null || !actionReturnValuernValue.getSucceeded()) {
                    failedRemoving.add(managedBlockDisk);
                    logRemoveManagedBlockDiskError(managedBlockDisk, actionReturnValuernValue);
                }
            }
        }
        setActionReturnValue(failedRemoving);
        persistCommand(getParameters().getParentCommand(), false);
        setSucceeded(true);
    }

    private void logRemoveManagedBlockDiskError(ManagedBlockStorageDisk managedBlockDisk, ActionReturnValue actionReturnValue) {
        log.error("Can't remove managed block disk id '{}' for VM id '{}' from domain id '{}' due to: {}.",
                managedBlockDisk.getImageId(),
                getParameters().getVmId(),
                managedBlockDisk.getStorageIds().get(0),
                actionReturnValue != null ? actionReturnValue.getFault().getMessage() : "");
    }

    private ActionReturnValue removeManagedBlockDisk(ManagedBlockStorageDisk managedBlockDisk) {
        Future<ActionReturnValue> future = commandCoordinatorUtil.executeAsyncCommand(
                ActionType.RemoveManagedBlockStorageDisk,
                buildChildCommandParameters(managedBlockDisk),
                cloneContextAndDetachFromParent());
        try {
            return future.get();
        } catch (InterruptedException | ExecutionException e) {
            log.error("Error removing Cinder disk", e);
        }
        return null;
    }

    private RemoveDiskParameters buildChildCommandParameters(ManagedBlockStorageDisk managedBlockDisk) {
        RemoveDiskParameters removeDiskParams = new RemoveDiskParameters(managedBlockDisk.getId());
        removeDiskParams.setStorageDomainId(managedBlockDisk.getStorageIds().get(0));
        removeDiskParams.setParentCommand(getActionType());
        removeDiskParams.setParentParameters(getParameters());
        removeDiskParams.setShouldBeLogged(false);
        removeDiskParams.setEndProcedure(EndProcedure.COMMAND_MANAGED);
        return removeDiskParams;
    }

    private List<ManagedBlockStorageDisk> getManagedBlockDisksToBeRemoved() {
        List<ManagedBlockStorageDisk> imageDisks = getParameters().getManagedBlockDisks();
        final List<ManagedBlockStorageDisk> managedBlockDisks = new ArrayList<>();
        if (imageDisks == null) {
            managedBlockDisks.addAll(DisksFilter.filterManagedBlockStorageDisks(diskDao.getAllForVm(getVmId())));
        } else {
            imageDisks.forEach(diskImage -> managedBlockDisks.add((ManagedBlockStorageDisk) diskImage));
        }
        return managedBlockDisks;
    }

    @Override
    protected void endWithFailure() {
        setSucceeded(true);
    }

    @Override
    protected void endSuccessfully() {
        setSucceeded(true);
    }

    @Override
    protected Map<String, Pair<String, String>> getExclusiveLocks() {
        return Collections.emptyMap();
    }

    @Override
    protected Map<String, Pair<String, String>> getSharedLocks() {
        return Collections.emptyMap();
    }

    @Override public List<PermissionSubject> getPermissionCheckSubjects() {
        return null;
    }
}
