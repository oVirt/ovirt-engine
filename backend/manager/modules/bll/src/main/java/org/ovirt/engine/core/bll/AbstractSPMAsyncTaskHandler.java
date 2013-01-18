package org.ovirt.engine.core.bll;

import org.ovirt.engine.core.bll.job.ExecutionHandler;
import org.ovirt.engine.core.bll.tasks.SPMAsyncTaskHandler;
import org.ovirt.engine.core.bll.tasks.TaskHandlerCommand;
import org.ovirt.engine.core.common.VdcObjectType;
import org.ovirt.engine.core.common.action.VdcReturnValueBase;
import org.ovirt.engine.core.common.asynctasks.AsyncTaskCreationInfo;
import org.ovirt.engine.core.common.vdscommands.VDSCommandType;
import org.ovirt.engine.core.common.vdscommands.VDSParametersBase;
import org.ovirt.engine.core.common.vdscommands.VDSReturnValue;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.utils.log.Log;
import org.ovirt.engine.core.utils.log.LogFactory;

public abstract class AbstractSPMAsyncTaskHandler<C extends TaskHandlerCommand<?>> implements SPMAsyncTaskHandler {

    protected Log log = LogFactory.getLog(getClass());

    private final C cmd;

    public AbstractSPMAsyncTaskHandler(C cmd) {
        this.cmd = cmd;
    }

    public C getEnclosingCommand() {
        return cmd;
    }

    public VdcReturnValueBase getReturnValue() {
        return cmd.getReturnValue();
    }

    @Override
    public void execute() {
        if (getEnclosingCommand().getParameters().getTaskGroupSuccess()) {
            getReturnValue().setSucceeded(false);
            beforeTask();
            addTask(Backend.getInstance().getResourceManager()
                    .RunVdsCommand(getVDSCommandType(), getVDSParameters()), false);
        }
        ExecutionHandler.setAsyncJob(getEnclosingCommand().getExecutionContext(), true);
        getReturnValue().setSucceeded(true);
    }

    @Override
    public void compensate() {
        revertTask();
        VDSCommandType revertCommandType = getRevertVDSCommandType();
        if (revertCommandType != null) {
            addTask(Backend.getInstance().getResourceManager()
                    .RunVdsCommand(getRevertVDSCommandType(), getRevertVDSParameters()), true);
        }
    }

    private void addTask(VDSReturnValue vdsReturnValue, boolean isRevertedTask) {
        AsyncTaskCreationInfo taskCreationInfo = vdsReturnValue.getCreationInfo();
        getReturnValue().getInternalTaskIdList().add(cmd.createTask(
                taskCreationInfo,
                cmd.getActionType(),
                getTaskObjectType(),
                getTaskObjects())
                );
        Guid taskId = taskCreationInfo.getTaskID();
        getReturnValue().getTaskIdList().add(taskId);
        if (isRevertedTask) {
            log.infoFormat("Reverting task {0} with ID {1} on DataCenter {2}.", taskCreationInfo.getTaskType().name(), taskId, taskCreationInfo.getStoragePoolID());
        }
    }

    @Override
    public void endSuccessfully() {
        getReturnValue().setSucceeded(true);
    }

    @Override
    public void endWithFailure() {
        getEnclosingCommand().getParameters().setTaskGroupSuccess(false);
        getReturnValue().setSucceeded(true);
    }

    protected abstract void beforeTask();
    protected abstract VDSCommandType getVDSCommandType();
    protected abstract VDSParametersBase getVDSParameters();

    protected abstract void revertTask();
    protected abstract VDSCommandType getRevertVDSCommandType();
    protected abstract VDSParametersBase getRevertVDSParameters();

    protected abstract VdcObjectType getTaskObjectType();
    protected abstract Guid[] getTaskObjects();
}
