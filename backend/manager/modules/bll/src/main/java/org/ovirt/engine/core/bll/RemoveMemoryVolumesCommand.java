package org.ovirt.engine.core.bll;

import org.ovirt.engine.core.bll.context.CommandContext;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.commons.lang.NotImplementedException;
import org.ovirt.engine.core.bll.memory.MemoryImageRemoverOnDataDomain;
import org.ovirt.engine.core.bll.tasks.TaskHandlerCommand;
import org.ovirt.engine.core.bll.utils.PermissionSubject;
import org.ovirt.engine.core.common.VdcObjectType;
import org.ovirt.engine.core.common.action.RemoveMemoryVolumesParameters;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.asynctasks.AsyncTaskCreationInfo;
import org.ovirt.engine.core.common.asynctasks.AsyncTaskType;
import org.ovirt.engine.core.compat.Guid;

/**
 * Command for removing the given memory volumes.
 * Note that no tasks are created, so we don't monitor whether
 * the operation succeed or not as we can't do much when if fails.
 */
@NonTransactiveCommandAttribute
@InternalCommandAttribute
public class RemoveMemoryVolumesCommand<T extends RemoveMemoryVolumesParameters> extends CommandBase<T> implements TaskHandlerCommand<T> {

    public RemoveMemoryVolumesCommand(T parameters) {
        super(parameters);
    }

    public RemoveMemoryVolumesCommand(T parameters, CommandContext cmdContext) {
        super(parameters, cmdContext);
    }

    protected RemoveMemoryVolumesCommand(Guid commandId) {
        super(commandId);
    }

    @Override
    protected void executeCommand() {
        MemoryImageRemoverOnDataDomain memoryImageRemover =
                new MemoryImageRemoverOnDataDomain(
                        getParameters().getVmId(),
                        this);

        setSucceeded(memoryImageRemover.remove(
                Collections.singleton(getParameters().getMemoryVolumes())));
    }

    @Override
    protected AsyncTaskType getTaskType() {
        return AsyncTaskType.deleteImage;
    }

    //////////////////////////////////
    /// TaskHandler implementation ///
    //////////////////////////////////

    @Override
    public VdcActionType getActionType() {
        return getParameters().getParentCommand() == VdcActionType.Unknown ?
                super.getActionType() : getParameters().getParentCommand();
    }

    @Override
    public Guid createTask(Guid taskId, AsyncTaskCreationInfo asyncTaskCreationInfo, VdcActionType parentCommand) {
        return super.createTask(taskId, asyncTaskCreationInfo, parentCommand);
    }

    @Override
    public Guid createTask(Guid taskId,
            AsyncTaskCreationInfo asyncTaskCreationInfo,
            VdcActionType parentCommand,
            VdcObjectType vdcObjectType,
            Guid... entityIds) {
        return super.createTask(taskId, asyncTaskCreationInfo, parentCommand, vdcObjectType, entityIds);
    }

    @Override
    public ArrayList<Guid> getTaskIdList() {
        return super.getTaskIdList();
    }

    @Override
    public void taskEndSuccessfully() {
        // Not implemented
    }

    @Override
    public void preventRollback() {
        throw new NotImplementedException();
    }

    @Override
    public Guid persistAsyncTaskPlaceHolder() {
        return super.persistAsyncTaskPlaceHolder(getActionType());
    }

    @Override
    public Guid persistAsyncTaskPlaceHolder(String taskKey) {
        return super.persistAsyncTaskPlaceHolder(getActionType(), taskKey);
    }

    @Override
    public List<PermissionSubject> getPermissionCheckSubjects() {
        return Collections.emptyList();
    }
}
