package org.ovirt.engine.core.bll.storage;

import org.ovirt.engine.core.bll.Backend;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractSPMAsyncTaskHandler<C extends TaskHandlerCommand<?>> implements SPMAsyncTaskHandler {

    protected Logger log = LoggerFactory.getLogger(AbstractSPMAsyncTaskHandler.class);

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
            Guid taskId = getEnclosingCommand().persistAsyncTaskPlaceHolder();

            addTask(taskId, runVdsCommand(getVDSCommandType(), getVDSParameters()), false);
        }
        ExecutionHandler.setAsyncJob(getEnclosingCommand().getExecutionContext(), true);
        getReturnValue().setSucceeded(true);
    }

    protected VDSReturnValue runVdsCommand(VDSCommandType commandType, VDSParametersBase params) {
        return Backend.getInstance().getResourceManager()
                .runVdsCommand(commandType, params);
    }

    @Override
    public void compensate() {
        revertTask();
        VDSCommandType revertCommandType = getRevertVDSCommandType();
        if (revertCommandType != null) {
            addTask(Guid.Empty,
                    runVdsCommand(getRevertVDSCommandType(), getRevertVDSParameters()), true);
        }
    }

    private void addTask(Guid taskId, VDSReturnValue vdsReturnValue, boolean isRevertedTask) {
        AsyncTaskCreationInfo taskCreationInfo = vdsReturnValue.getCreationInfo();
        getReturnValue().getInternalVdsmTaskIdList().add(cmd.createTask(
                taskId,
                taskCreationInfo,
                cmd.getActionType(),
                getTaskObjectType(),
                getTaskObjects())
                );
        Guid vdsmTaskId = taskCreationInfo.getVdsmTaskId();
        getReturnValue().getVdsmTaskIdList().add(vdsmTaskId);
        if (isRevertedTask) {
            log.info("Reverting task '{}' with ID '{}' on DataCenter '{}'.",
                    taskCreationInfo.getTaskType().name(),
                    vdsmTaskId,
                    taskCreationInfo.getStoragePoolID());
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
