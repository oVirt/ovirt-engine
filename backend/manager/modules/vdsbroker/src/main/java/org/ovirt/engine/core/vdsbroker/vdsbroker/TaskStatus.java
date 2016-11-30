package org.ovirt.engine.core.vdsbroker.vdsbroker;

import java.util.Map;

public final class TaskStatus extends Status {
    private static final String TASK_STATE = "taskState";
    private static final String TASK_RESULT = "taskResult";

    // [Member("taskState")]
    public String taskState;
    // [Member("taskResult")]
    public String taskResult;

    public TaskStatus(Map<String, Object> innerMap) {
        super(innerMap);
        taskState = (String) innerMap.get(TASK_STATE);
        taskResult = (String) innerMap.get(TASK_RESULT);
    }

    public TaskStatus() {

    }

}
