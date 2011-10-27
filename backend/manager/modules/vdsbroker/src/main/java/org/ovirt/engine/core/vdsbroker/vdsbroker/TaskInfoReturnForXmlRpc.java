package org.ovirt.engine.core.vdsbroker.vdsbroker;

import java.util.Map;

import org.ovirt.engine.core.vdsbroker.irsbroker.*;
import org.ovirt.engine.core.vdsbroker.xmlrpc.XmlRpcObjectDescriptor;

public final class TaskInfoReturnForXmlRpc extends StatusReturnForXmlRpc {
    private static final String TASK_INFO = "TaskInfo";
    // We are ignoring missing fields after the status, because on failure it is
    // not sent.
    // [XmlRpcMissingMapping(MappingAction.Ignore), XmlRpcMember("TaskInfo")]
    public Map<String, String> TaskInfo;

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("\n");
        builder.append(super.toString());
        builder.append("\n");
        XmlRpcObjectDescriptor.ToStringBuilder(TaskInfo, builder);
        return builder.toString();
    }

    @SuppressWarnings("unchecked")
    public TaskInfoReturnForXmlRpc(Map<String, Object> innerMap) {
        super(innerMap);
        Object temp = (Object) innerMap.get(TASK_INFO);
        if (temp != null) {
            TaskInfo = (Map<String, String>) temp;
        }
    }

}
