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
    private TaskStatusListReturn result;

    public HSMGetAllTasksStatusesVDSCommand(P parameters) {
        super(parameters);
    }

    @Override
    protected void executeVdsBrokerCommand() {
        result = getBroker().getAllTasksStatuses();
        proceedProxyReturnValue();
        setReturnValue(parseTaskStatusList(result.taskStatusList));
    }

    protected AsyncTaskStatus parseTaskStatus(TaskStatus taskStatus) {
        AsyncTaskStatus task = new AsyncTaskStatus();
        task.setStatus((taskStatus != null && taskStatus.taskState != null) ? AsyncTaskStatusEnum
                .valueOf(taskStatus.taskState) : AsyncTaskStatusEnum.unknown);

        if (task.getStatus() == AsyncTaskStatusEnum.finished) {
            updateReturnStatus(taskStatus);

            try {
                proceedProxyReturnValue();
            } catch (RuntimeException ex) {
                task.setException(ex);
            }

            task.setResult(taskStatus != null ? AsyncTaskResultEnum.valueOf(taskStatus.taskResult)
                    : AsyncTaskResultEnum.unknown);

            // Normally, when the result is not 'success', there is an
            // exception.
            // Just in case, we check the result here and if there is no
            // exception,
            // we throw a special one here:
            if (task.getResult() != AsyncTaskResultEnum.success && task.getException() == null) {
                task.setException(new VDSTaskResultNotSuccessException(String.format(
                        "TaskState contained successful return code, but a non-success result ('%1$s').",
                        taskStatus.taskResult)));
            }
        }
        return task;
    }

    protected Map<Guid, AsyncTaskStatus> parseTaskStatusList(Map<String, ?> taskStatusList) {
        Map<Guid, AsyncTaskStatus> result = new HashMap<>(taskStatusList.size());
        for (Map.Entry<String, ?> entry : taskStatusList.entrySet()) {
            try {
                Guid taskGuid = new Guid(entry.getKey().toString());
                Map<String, Object> xrsTaskStatusAsMAp = (Map<String, Object>) entry.getValue();
                if (xrsTaskStatusAsMAp == null) {
                    log.error("Task ID {} has no task data", entry.getKey());
                } else {
                    Map<String, Object> xrsTaskStatus = xrsTaskStatusAsMAp;
                    TaskStatus tempVar = new TaskStatus();
                    tempVar.code = Integer.parseInt(xrsTaskStatus.get("code").toString());
                    tempVar.message = xrsTaskStatus.get("message").toString();
                    tempVar.taskResult = xrsTaskStatus.get("taskResult").toString();
                    tempVar.taskState = xrsTaskStatus.get("taskState").toString();
                    TaskStatus taskStatus = tempVar;
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
    protected Status getReturnStatus() {
        return result.getStatus();
    }

    // overrides the value of the status that is being checked in the
    // proceedProxyReturnValue method.
    // Used when multiple calls to proceedProxyReturnValue are needed within
    // the same VDSCommand on different status values, for example, a regular
    // verb
    // execution status and an asynchronous task status.
    protected void updateReturnStatus(Status newReturnStatus) {
        result.setStatus(newReturnStatus);
    }

    @Override
    protected Object getReturnValueFromBroker() {
        return result;
    }
}
