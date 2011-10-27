package org.ovirt.engine.core.vdsbroker.vdsbroker;

import java.util.Map;

import org.ovirt.engine.core.compat.*;
import org.ovirt.engine.core.common.vdscommands.*;
import org.ovirt.engine.core.common.asynctasks.*;

public class HSMGetAllTasksInfoVDSCommand<P extends VdsIdVDSCommandParametersBase> extends VdsBrokerCommand<P> {
    private TaskInfoListReturnForXmlRpc _result;
    private static final String VERB_KEY = "verb";

    public HSMGetAllTasksInfoVDSCommand(P parameters) {
        super(parameters);
    }

    @Override
    protected void ExecuteVdsBrokerCommand() {
        _result = getBroker().getAllTasksInfo();
        ProceedProxyReturnValue();
        setReturnValue(ParseTaskInfoList(_result.TaskInfoList));
    }

    protected java.util.ArrayList<AsyncTaskCreationInfo> ParseTaskInfoList(Map<String, Map<String, String>> taskInfoList) {
        try {
            java.util.ArrayList<AsyncTaskCreationInfo> result = new java.util.ArrayList<AsyncTaskCreationInfo>(
                    taskInfoList.size());
            for (java.util.Map.Entry<String, java.util.Map<String, String>> entry : taskInfoList.entrySet()) {
                Guid taskID = new Guid(entry.getKey());
                Map<String, String> taskInfo = entry.getValue();
                AsyncTaskCreationInfo task = ParseTaskInfo(taskInfo, taskID);
                if (task != null) {
                    result.add(task);
                }
            }
            return result;
        } catch (RuntimeException exp) {
            log.errorFormat("Could not parse task info list: '{0}'", taskInfoList.toString());
            throw exp;
        }
    }

    protected AsyncTaskCreationInfo ParseTaskInfo(Map<String, String> taskInfo, Guid taskID) {
        try {
            // LINQ 29456
            // DictionaryEntry deTaskType =
            // taskInfo.Cast<DictionaryEntry>().FirstOrDefault(a =>
            // a.Key.toString() == VERB_KEY);
            String deTaskType = taskInfo.get(VERB_KEY);
            // LINQ 29456
            AsyncTaskType taskType;
            try {
                taskType = AsyncTaskType.valueOf(deTaskType);
            } catch (Exception e) {
                taskType = AsyncTaskType.unknown;
                log.warn("The task type in the vdsm response is " + deTaskType
                        + " and does not appear in the AsyncTaskType enum");
            }

            AsyncTaskCreationInfo tempVar = new AsyncTaskCreationInfo();
            tempVar.setTaskID(taskID);
            tempVar.setTaskType(taskType);
            AsyncTaskCreationInfo task = tempVar;

            return task;
        }

        catch (RuntimeException e) {
            log.error(String.format(
                    "Could not parse single task info: '%1$s'  (possibly specific task should not be monitored).",
                    taskInfo), e);
            return null;
        }
    }

    @Override
    protected StatusForXmlRpc getReturnStatus() {
        return _result.mStatus;
    }

    @Override
    protected Object getReturnValueFromBroker() {
        return _result;
    }

    private static LogCompat log = LogFactoryCompat.getLog(HSMGetAllTasksInfoVDSCommand.class);
}
