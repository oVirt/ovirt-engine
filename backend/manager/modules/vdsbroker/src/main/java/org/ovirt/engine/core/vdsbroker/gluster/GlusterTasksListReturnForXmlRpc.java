package org.ovirt.engine.core.vdsbroker.gluster;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.ovirt.engine.core.common.asynctasks.gluster.GlusterAsyncTask;
import org.ovirt.engine.core.common.asynctasks.gluster.GlusterAsyncTaskStatus;
import org.ovirt.engine.core.common.asynctasks.gluster.GlusterTaskParameters;
import org.ovirt.engine.core.common.asynctasks.gluster.GlusterTaskType;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.vdsbroker.irsbroker.StatusReturnForXmlRpc;

public class GlusterTasksListReturnForXmlRpc extends StatusReturnForXmlRpc {

    private static final String TASKS_LIST = "tasks";
    private static final String STATUS = "status";
    private static final String TASK_TYPE = "type";
    private static final String DATA = "data";
    private static final String VOLUME_NAME = "volume";
    private static final String FILES_MOVED = "filesMoved";
    private static final String TOTAL_SIZE_MOVED = "totalSizeMoved";
    private static final String FILES_SCANNED = "filesScanned";
    private static final String FILES_FAILED = "filesFailed";

    private List<GlusterAsyncTask> tasks = new ArrayList<GlusterAsyncTask>();

    @SuppressWarnings("unchecked")
    public GlusterTasksListReturnForXmlRpc(Map<String, Object> innerMap) {
        super(innerMap);

        if (mStatus.mCode != 0) {
            return;
        }

        Map<String, Object> tasksMap = (Map<String, Object>) innerMap.get(TASKS_LIST);
        if (tasksMap != null) {
            for (Entry<String, Object> entry: tasksMap.entrySet()) {
                tasks.add(getTask(entry.getKey(),(Map<String, Object>)entry.getValue()));
            }
        }

    }

    @SuppressWarnings("unchecked")
    private GlusterAsyncTask getTask(String taskId, Map<String, Object> map) {
        GlusterAsyncTask task = new GlusterAsyncTask();
        task.setTaskId(Guid.createGuidFromString(taskId));
        task.setStatus(GlusterAsyncTaskStatus.from((String)map.get(STATUS)).getJobExecutionStatus());
        task.setType(GlusterTaskType.valueOf((String)map.get(TASK_TYPE)));
        task.setMessage(getMessage((Map<String, Object>)map.get(DATA)));
        task.setTaskParameters(new GlusterTaskParameters());
        task.getTaskParameters().setVolumeName((String)map.get(VOLUME_NAME));
        return task;
    }

    private String getMessage(Map<String, Object> map) {
        return String.format("Files [scanned: %1$s, moved: %2$s, failed: %3$s], Total size moved: %4$s",
                            map.get(FILES_SCANNED),
                            map.get(FILES_MOVED),
                            map.get(FILES_FAILED),
                            map.get(TOTAL_SIZE_MOVED));

    }

    public List<GlusterAsyncTask> getGlusterTasks() {
        return tasks;
    }

}
