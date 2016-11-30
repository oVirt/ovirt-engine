package org.ovirt.engine.core.vdsbroker.vdsbroker;

import java.util.Map;

import org.ovirt.engine.core.vdsbroker.irsbroker.StatusReturn;

public final class TaskStatusReturn extends StatusReturn {
    private static final String TASK_STATUS = "taskStatus";
    // We are ignoring missing fields after the status, because on failure it is
    // not sent.
    // [MissingMapping(MappingAction.Ignore), Member("taskStatus")]
    public TaskStatus taskStatus;

    @SuppressWarnings("unchecked")
    public TaskStatusReturn(Map<String, Object> innerMap) {
        super(innerMap);
        Object temp = innerMap.get(TASK_STATUS);
        if (temp != null) {
            taskStatus = new TaskStatus((Map<String, Object>) temp);
        }
    }

}
