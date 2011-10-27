package org.ovirt.engine.core.vdsbroker.vdsbroker;

import java.util.Map;

public final class TaskStatusForXmlRpc extends StatusForXmlRpc {
    private static final String TASK_STATE = "taskState";
    private static final String TASK_RESULT = "taskResult";

    // [XmlRpcMember("taskState")]
    public String mTaskState;
    // [XmlRpcMember("taskResult")]
    public String mTaskResult;

    public TaskStatusForXmlRpc(Map<String, Object> innerMap) {
        super(innerMap);
        mTaskState = (String) innerMap.get(TASK_STATE);
        mTaskResult = (String) innerMap.get(TASK_RESULT);
    }

    public TaskStatusForXmlRpc() {

    }

}
