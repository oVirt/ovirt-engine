package org.ovirt.engine.core.bll;

import java.util.Collections;
import java.util.List;

import javax.inject.Inject;

import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.bll.utils.PermissionSubject;
import org.ovirt.engine.core.common.action.ActionType;
import org.ovirt.engine.core.common.action.RemoveDiskParameters;
import org.ovirt.engine.core.common.action.RemoveMemoryVolumesParameters;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.SnapshotDao;

/**
 * Command for removing the given memory volumes.
 * Note that no tasks are created, so we don't monitor whether
 * the operation succeed or not as we can't do much when if fails.
 */
@NonTransactiveCommandAttribute
@InternalCommandAttribute
public class RemoveMemoryVolumesCommand<T extends RemoveMemoryVolumesParameters> extends CommandBase<T> {

    @Inject
    private SnapshotDao snapshotDao;

    public RemoveMemoryVolumesCommand(T parameters, CommandContext cmdContext) {
        super(parameters, cmdContext);
    }

    protected RemoveMemoryVolumesCommand(Guid commandId) {
        super(commandId);
    }

    @Override
    protected void executeCommand() {
        if (getParameters().getSnapshot().containsMemory() && isMemoryRemovable()) {

            // As part of the RemoveDisk command, removing the memory_dump volume removes
            // the memory_metadata volume as well.
            RemoveDiskParameters removeMemoryDumpDiskParameters = new RemoveDiskParameters(getParameters().getSnapshot().getMemoryDiskId());
            removeMemoryDumpDiskParameters.setShouldBeLogged(false);
            runInternalAction(ActionType.RemoveDisk, removeMemoryDumpDiskParameters);
        }
        setSucceeded(true);
    }

    private boolean isMemoryRemovable() {
        return snapshotDao.getNumOfSnapshotsByDisks(getParameters().getSnapshot()) == 1
                || getParameters().isForceRemove();
    }

    @Override
    public List<PermissionSubject> getPermissionCheckSubjects() {
        return Collections.emptyList();
    }
}
