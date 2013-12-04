package org.ovirt.engine.core.vdsbroker.vdsbroker;

import java.util.Map;

import org.ovirt.engine.core.vdsbroker.irsbroker.StatusReturnForXmlRpc;
import org.ovirt.engine.core.vdsbroker.xmlrpc.XmlRpcObjectDescriptor;

public final class TaskStatusListReturnForXmlRpc extends StatusReturnForXmlRpc {
    private static final String ALL_TASKS_STATUS = "allTasksStatus";
    // We are ignoring missing fields after the status, because on failure it is
    // not sent.
    public Map<String, Object> taskStatusList;

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("\n");
        builder.append(super.toString());
        builder.append("\n");
        XmlRpcObjectDescriptor.toStringBuilder(taskStatusList, builder);
        return builder.toString();
    }

    @SuppressWarnings("unchecked")
    public TaskStatusListReturnForXmlRpc(Map<String, Object> innerMap) {
        super(innerMap);
        taskStatusList = (Map<String, Object>) innerMap.get(ALL_TASKS_STATUS);
    }

}
