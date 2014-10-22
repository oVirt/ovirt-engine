package org.ovirt.engine.core.vdsbroker.vdsbroker;

import java.util.HashMap;
import java.util.Map;

import org.ovirt.engine.core.common.businessentities.AsyncTaskResultEnum;
import org.ovirt.engine.core.common.businessentities.AsyncTaskStatus;
import org.ovirt.engine.core.common.businessentities.AsyncTaskStatusEnum;
import org.ovirt.engine.core.common.vdscommands.VdsIdVDSCommandParametersBase;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.utils.log.Logged;
import org.ovirt.engine.core.utils.log.Logged.LogLevel;

@SuppressWarnings("unchecked")
@Logged(executionLevel = LogLevel.DEBUG)
public class HSMGetAllTasksStatusesVDSCommand<P extends VdsIdVDSCommandParametersBase> extends VdsBrokerCommand<P> {
    private TaskStatusListReturnForXmlRpc _result;

    public HSMGetAllTasksStatusesVDSCommand(P parameters) {
        super(parameters);
    }

    @Override
    protected void executeVdsBrokerCommand() {
        _result = getBroker().getAllTasksStatuses();
        proceedProxyReturnValue();
        setReturnValue(parseTaskStatusList(_result.taskStatusList));
    }

    protected AsyncTaskStatus parseTaskStatus(TaskStatusForXmlRpc taskStatus) {
        AsyncTaskStatus task = new AsyncTaskStatus();
        task.setStatus((taskStatus != null && taskStatus.mTaskState != null) ? (AsyncTaskStatusEnum
                .valueOf(taskStatus.mTaskState)) : AsyncTaskStatusEnum.unknown);

        if (task.getStatus() == AsyncTaskStatusEnum.finished) {
            updateReturnStatus(taskStatus);

            try {
                proceedProxyReturnValue();
            }

            catch (RuntimeException ex) {
                task.setException(ex);
            }

            task.setResult(taskStatus != null ? (AsyncTaskResultEnum.valueOf(taskStatus.mTaskResult))
                    : AsyncTaskResultEnum.unknown);

            // Normally, when the result is not 'success', there is an
            // exception.
            // Just in case, we check the result here and if there is no
            // exception,
            // we throw a special one here:
            if (task.getResult() != AsyncTaskResultEnum.success && task.getException() == null) {
                task.setException(new VDSTaskResultNotSuccessException(String.format(
                        "TaskState contained successful return code, but a non-success result ('%1$s').",
                        taskStatus.mTaskResult)));
            }
        }
        return task;
    }

    protected HashMap<Guid, AsyncTaskStatus> parseTaskStatusList(Map<String, ?> taskStatusList) {
        HashMap<Guid, AsyncTaskStatus> result = new HashMap<Guid, AsyncTaskStatus>(
                taskStatusList.size());
        for (Map.Entry<String, ?> entry : taskStatusList.entrySet()) {
            try {
                Guid taskGuid = new Guid(entry.getKey().toString());
                Map<String, Object> xrsTaskStatusAsMAp = (Map<String, Object>) entry.getValue();
                if (xrsTaskStatusAsMAp == null) {
                    log.error("Task ID {} has no task data", entry.getKey());
                } else {
                    Map<String, Object> xrsTaskStatus = xrsTaskStatusAsMAp;
                    TaskStatusForXmlRpc tempVar = new TaskStatusForXmlRpc();
                    tempVar.mCode = Integer.parseInt(xrsTaskStatus.get("code").toString());
                    tempVar.mMessage = xrsTaskStatus.get("message").toString();
                    tempVar.mTaskResult = xrsTaskStatus.get("taskResult").toString();
                    tempVar.mTaskState = xrsTaskStatus.get("taskState").toString();
                    TaskStatusForXmlRpc taskStatus = tempVar;
                    AsyncTaskStatus task = parseTaskStatus(taskStatus);
                    result.put(taskGuid, task);
                }
            } catch (RuntimeException exp) {
                log.error(
                        "HSMGetAllTasksStatusesVDSCommand: Ignoring error while parsing task status from list. key: '{}', value: '{}': {}",
                        entry.getKey(),
                        entry.getValue(),
                        exp.getMessage());
                log.debug("Exception", exp);
            }
        }
        return result;
    }

    @Override
    protected StatusForXmlRpc getReturnStatus() {
        return _result.mStatus;
    }

    // overrides the value of the status that is being checked in the
    // proceedProxyReturnValue method.
    // Used when multiple calls to proceedProxyReturnValue are needed within
    // the same VDSCommand on different status values, for example, a regular
    // verb
    // execution status and an asynchronous task status.
    protected void updateReturnStatus(StatusForXmlRpc newReturnStatus) {
        _result.mStatus = newReturnStatus;
    }

    @Override
    protected Object getReturnValueFromBroker() {
        return _result;
    }
}
