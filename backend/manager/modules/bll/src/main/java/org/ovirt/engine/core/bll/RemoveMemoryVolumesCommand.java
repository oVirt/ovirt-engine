package org.ovirt.engine.core.bll;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.ovirt.engine.core.bll.memory.HibernationVolumesRemover;
import org.ovirt.engine.core.bll.tasks.TaskHandlerCommand;
import org.ovirt.engine.core.bll.utils.PermissionSubject;
import org.ovirt.engine.core.common.VdcObjectType;
import org.ovirt.engine.core.common.action.RemoveMemoryVolumesParameters;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.asynctasks.AsyncTaskCreationInfo;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.NotImplementedException;

/**
 * Command for removing the given memory volumes.
 * Note that no tasks are created, so we don't monitor whether
 * the operation succeed or not as we can't do much when if fails.
 */
@NonTransactiveCommandAttribute
@InternalCommandAttribute
public class RemoveMemoryVolumesCommand<T extends RemoveMemoryVolumesParameters> extends CommandBase<T> implements TaskHandlerCommand<T> {
    /** fictive list of task IDs, used when we don't want to add tasks */
    private static final ArrayList<Guid> dummyTaskIdList = new ArrayList<>();

    public RemoveMemoryVolumesCommand(T parameters) {
        super(parameters);
    }

    protected RemoveMemoryVolumesCommand(Guid commandId) {
        super(commandId);
    }

    @Override
    protected void executeCommand() {
        HibernationVolumesRemover hibernationVolumesRemover =
                new HibernationVolumesRemover(
                        getParameters().getMemoryVolumes(),
                        getParameters().getVmId(),
                        this);

        setSucceeded(hibernationVolumesRemover.remove());
    }

    //////////////////////////////////
    /// TaskHandler implementation ///
    //////////////////////////////////

    @Override
    public VdcActionType getActionType() {
        return super.getActionType();
    }

    /**
     * Not adding tasks
     */
    @Override
    public Guid createTask(Guid taskId, AsyncTaskCreationInfo asyncTaskCreationInfo, VdcActionType parentCommand) {
        return Guid.Empty;
    }

    /**
     * Not adding tasks
     */
    @Override
    public Guid createTask(Guid taskId,
            AsyncTaskCreationInfo asyncTaskCreationInfo,
            VdcActionType parentCommand,
            VdcObjectType vdcObjectType,
            Guid... entityIds) {
        return Guid.Empty;
    }

    /**
     * Not adding task IDs
     */
    @Override
    public ArrayList<Guid> getTaskIdList() {
        return dummyTaskIdList;
    }

    @Override
    public void preventRollback() {
        throw new NotImplementedException();
    }

    /**
     * Not adding place holders
     */
    @Override
    public Guid persistAsyncTaskPlaceHolder() {
        return Guid.Empty;
    }

    /**
     * Not adding place holders
     */
    @Override
    public Guid persistAsyncTaskPlaceHolder(String taskKey) {
        return Guid.Empty;
    }

    @Override
    public List<PermissionSubject> getPermissionCheckSubjects() {
        return Collections.emptyList();
    }
}
