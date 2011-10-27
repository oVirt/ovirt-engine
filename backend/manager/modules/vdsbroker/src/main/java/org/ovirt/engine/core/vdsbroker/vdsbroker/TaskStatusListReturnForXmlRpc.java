package org.ovirt.engine.core.vdsbroker.vdsbroker;

import java.util.Map;

import org.ovirt.engine.core.vdsbroker.irsbroker.*;
import org.ovirt.engine.core.vdsbroker.xmlrpc.XmlRpcObjectDescriptor;
import org.ovirt.engine.core.vdsbroker.xmlrpc.XmlRpcStruct;

public final class TaskStatusListReturnForXmlRpc extends StatusReturnForXmlRpc {
    private static final String ALL_TASKS_STATUS = "allTasksStatus";
    // We are ignoring missing fields after the status, because on failure it is
    // not sent.
    // [XmlRpcMissingMapping(MappingAction.Ignore),
    // XmlRpcMember("allTasksStatus")]
    public XmlRpcStruct TaskStatusList;

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("\n");
        builder.append(super.toString());
        builder.append("\n");
        XmlRpcObjectDescriptor.ToStringBuilder(TaskStatusList, builder);
        return builder.toString();
    }

    public TaskStatusListReturnForXmlRpc(Map<String, Object> innerMap) {
        super(innerMap);
        Object temp = (Object) innerMap.get(ALL_TASKS_STATUS);
        if (temp != null) {
            TaskStatusList = new XmlRpcStruct((Map<String, Object>) temp);
        }
    }

}
