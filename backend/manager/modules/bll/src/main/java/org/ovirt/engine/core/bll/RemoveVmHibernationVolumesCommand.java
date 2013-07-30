package org.ovirt.engine.core.bll;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.ovirt.engine.core.bll.memory.HibernationVolumesRemover;
import org.ovirt.engine.core.bll.tasks.TaskHandlerCommand;
import org.ovirt.engine.core.bll.utils.PermissionSubject;
import org.ovirt.engine.core.common.RemoveVmHibernationVolumesParameters;
import org.ovirt.engine.core.common.VdcObjectType;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.asynctasks.AsyncTaskCreationInfo;
import org.ovirt.engine.core.common.asynctasks.AsyncTaskType;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.NotImplementedException;

@NonTransactiveCommandAttribute
@InternalCommandAttribute
public class RemoveVmHibernationVolumesCommand<T extends RemoveVmHibernationVolumesParameters> extends CommandBase<T> implements TaskHandlerCommand<T> {

    public static final String DELETE_PRIMARY_IMAGE_TASK_KEY = "DELETE_PRIMARY_IMAGE_TASK_KEY";
    public static final String DELETE_SECONDARY_IMAGES_TASK_KEY = "DELETE_SECONDARY_IMAGES_TASK_KEY";

    public RemoveVmHibernationVolumesCommand(T parameters) {
        super(parameters);
        setVmId(parameters.getVmId());
    }

    protected RemoveVmHibernationVolumesCommand(Guid commandId) {
        super(commandId);
    }

    @Override
    protected void executeCommand() {
        HibernationVolumesRemover hibernationVolumesRemover =
                new HibernationVolumesRemover(
                        getVm().getHibernationVolHandle(),
                        getVm().getId(),
                        this);

        setSucceeded(hibernationVolumesRemover.remove());
    }

    @Override
    protected AsyncTaskType getTaskType() {
        return AsyncTaskType.deleteImage;
    }

    @Override
    public List<PermissionSubject> getPermissionCheckSubjects() {
        return Collections.emptyList();
    }

    /////////////////////////
    /// TaskHandlerCommand //
    ////////////////////////

    @Override
    public Guid persistAsyncTaskPlaceHolder() {
        return super.persistAsyncTaskPlaceHolder(getParameters().getParentCommand());
    }

    @Override
    public Guid persistAsyncTaskPlaceHolder(String taskKey) {
        return super.persistAsyncTaskPlaceHolder(getParameters().getParentCommand(), taskKey);
    }

    @Override
    public VdcActionType getActionType() {
        return super.getActionType();
    }

    @Override
    public Guid createTask(Guid taskId,
            AsyncTaskCreationInfo asyncTaskCreationInfo,
            VdcActionType parentCommand,
            VdcObjectType entityType,
            Guid... entityIds) {
        return super.createTaskInCurrentTransaction(taskId, asyncTaskCreationInfo, parentCommand, entityType, entityIds);
    }

    @Override
    public Guid createTask(Guid taskId,
            AsyncTaskCreationInfo asyncTaskCreationInfo,
            VdcActionType parentCommand) {
        return super.createTask(taskId, asyncTaskCreationInfo, parentCommand);
    }

    @Override
    public ArrayList<Guid> getTaskIdList() {
        return super.getTaskIdList();
    }

    @Override
    public void preventRollback() {
        throw new NotImplementedException();
    }
}
