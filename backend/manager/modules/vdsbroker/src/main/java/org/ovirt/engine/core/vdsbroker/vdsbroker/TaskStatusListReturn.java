package org.ovirt.engine.core.vdsbroker.vdsbroker;

import java.util.Map;

import org.ovirt.engine.core.utils.ObjectDescriptor;
import org.ovirt.engine.core.vdsbroker.irsbroker.StatusReturn;

public final class TaskStatusListReturn extends StatusReturn {
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
        ObjectDescriptor.toStringBuilder(taskStatusList, builder);
        return builder.toString();
    }

    @SuppressWarnings("unchecked")
    public TaskStatusListReturn(Map<String, Object> innerMap) {
        super(innerMap);
        taskStatusList = (Map<String, Object>) innerMap.get(ALL_TASKS_STATUS);
    }

}
