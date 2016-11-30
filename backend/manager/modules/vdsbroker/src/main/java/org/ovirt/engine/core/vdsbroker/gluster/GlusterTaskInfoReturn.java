package org.ovirt.engine.core.vdsbroker.gluster;

import java.util.Map;

import org.ovirt.engine.core.common.asynctasks.gluster.GlusterAsyncTask;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.vdsbroker.irsbroker.StatusReturn;

public class GlusterTaskInfoReturn extends  StatusReturn {
    private static final String TASK_ID = "taskId";
    private final GlusterAsyncTask glusterTask = new GlusterAsyncTask();

    public GlusterTaskInfoReturn(Map<String, Object> innerMap) {
        super(innerMap);
        if (innerMap.containsKey(TASK_ID)) {
            glusterTask.setTaskId(Guid.createGuidFromString((String)innerMap.get(TASK_ID)));
        }
    }

    public GlusterAsyncTask getGlusterTask() {
        return glusterTask;
    }

}
