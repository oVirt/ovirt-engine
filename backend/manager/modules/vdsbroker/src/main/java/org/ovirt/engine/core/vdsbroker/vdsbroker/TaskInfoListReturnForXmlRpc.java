package org.ovirt.engine.core.vdsbroker.vdsbroker;

import java.util.Map;

import org.ovirt.engine.core.vdsbroker.irsbroker.StatusReturnForXmlRpc;
import org.ovirt.engine.core.vdsbroker.xmlrpc.XmlRpcObjectDescriptor;

public final class TaskInfoListReturnForXmlRpc extends StatusReturnForXmlRpc {
    private static final String ALL_TASKS_INFO = "allTasksInfo";
    // We are ignoring missing fields after the status, because on failure it is
    // not sent.
    // [XmlRpcMissingMapping(MappingAction.Ignore),
    // XmlRpcMember("allTasksInfo")]
    public Map<String, Map<String, String>> taskInfoList;

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("\n");
        builder.append(super.toString());
        builder.append("\n");
        XmlRpcObjectDescriptor.toStringBuilder(taskInfoList, builder);
        return builder.toString();
    }

    @SuppressWarnings("unchecked")
    public TaskInfoListReturnForXmlRpc(Map<String, Object> innerMap) {
        super(innerMap);
        Object temp = innerMap.get(ALL_TASKS_INFO);
        if (temp != null) {
            taskInfoList = (Map<String, Map<String, String>>) temp;
        }
    }

}
