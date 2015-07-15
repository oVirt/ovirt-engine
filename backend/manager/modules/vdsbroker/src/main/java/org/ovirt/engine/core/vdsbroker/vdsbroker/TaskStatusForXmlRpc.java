package org.ovirt.engine.core.vdsbroker.vdsbroker;

import java.util.Map;

public final class TaskStatusForXmlRpc extends StatusForXmlRpc {
    private static final String TASK_STATE = "taskState";
    private static final String TASK_RESULT = "taskResult";

    // [XmlRpcMember("taskState")]
    public String taskState;
    // [XmlRpcMember("taskResult")]
    public String taskResult;

    public TaskStatusForXmlRpc(Map<String, Object> innerMap) {
        super(innerMap);
        taskState = (String) innerMap.get(TASK_STATE);
        taskResult = (String) innerMap.get(TASK_RESULT);
    }

    public TaskStatusForXmlRpc() {

    }

}
