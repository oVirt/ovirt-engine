package org.ovirt.engine.core.vdsbroker.gluster;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.ovirt.engine.core.common.asynctasks.gluster.GlusterAsyncTask;
import org.ovirt.engine.core.common.asynctasks.gluster.GlusterAsyncTaskStatus;
import org.ovirt.engine.core.common.asynctasks.gluster.GlusterTaskParameters;
import org.ovirt.engine.core.common.asynctasks.gluster.GlusterTaskType;
import org.ovirt.engine.core.common.utils.Pair;
import org.ovirt.engine.core.common.utils.SizeConverter;
import org.ovirt.engine.core.common.utils.SizeConverter.SizeUnit;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.vdsbroker.irsbroker.StatusReturn;

public class GlusterTasksListReturn extends StatusReturn {

    private static final String TASKS_LIST = "tasks";
    private static final String STATUS = "status";
    private static final String TASK_TYPE = "type";
    private static final String DATA = "data";
    private static final String VOLUME_NAME = "volume";
    private static final String BRICK_NAMES = "bricks";
    private static final String FILES_MOVED = "filesMoved";
    private static final String TOTAL_SIZE_MOVED = "totalSizeMoved";
    private static final String FILES_SCANNED = "filesScanned";
    private static final String FILES_FAILED = "filesFailed";

    private List<GlusterAsyncTask> tasks = new ArrayList<>();

    @SuppressWarnings("unchecked")
    public GlusterTasksListReturn(Map<String, Object> innerMap) {
        super(innerMap);

        if (getStatus().code != 0) {
            return;
        }

        Map<String, Object> tasksMap = (Map<String, Object>) innerMap.get(TASKS_LIST);
        if (tasksMap != null) {
            for (Entry<String, Object> entry: tasksMap.entrySet()) {
                GlusterAsyncTask task = getTask(entry.getKey(), (Map<String, Object>)entry.getValue());
                if(GlusterTaskType.UNKNOWN != task.getType()){
                    tasks.add(task);
                }
            }
        }

    }

    @SuppressWarnings("unchecked")
    private GlusterAsyncTask getTask(String taskId, Map<String, Object> map) {
        GlusterAsyncTask task = new GlusterAsyncTask();
        task.setTaskId(Guid.createGuidFromString(taskId));
        task.setStatus(GlusterAsyncTaskStatus.from((String)map.get(STATUS)).getJobExecutionStatus());
        task.setType(GlusterTaskType.fromValue((String)map.get(TASK_TYPE)));
        if(GlusterTaskType.UNKNOWN != task.getType()){
            task.setMessage(getMessage((Map<String, Object>)map.get(DATA)));
            task.setTaskParameters(new GlusterTaskParameters());
            task.getTaskParameters().setVolumeName((String)map.get(VOLUME_NAME));
            task.getTaskParameters().setBricks(getBrickNames(map.get(BRICK_NAMES)));
        }
        return task;
    }

    private String getMessage(Map<String, Object> map) {
        return String.format("Files [scanned: %1$s, moved: %2$s, failed: %3$s], Total size moved: %4$s",
                            map.get(FILES_SCANNED),
                            map.get(FILES_MOVED),
                            map.get(FILES_FAILED),
                            formatTotalSizeMoved(Long.parseLong(map.get(TOTAL_SIZE_MOVED).toString())));

    }

    private String formatTotalSizeMoved(long size) {
        NumberFormat formatSize = NumberFormat.getInstance();
        formatSize.setMaximumFractionDigits(2);
        formatSize.setMinimumFractionDigits(2);
        Pair<SizeConverter.SizeUnit, Double> sizeMoved= SizeConverter.autoConvert(size, SizeUnit.BYTES);
        return formatSize.format(sizeMoved.getSecond().doubleValue()).toString().concat(" ").concat(sizeMoved.getFirst().toString());
    }

    private String[] getBrickNames(Object bricksObj){
        if (bricksObj == null || !(bricksObj instanceof Object[])) {
            return null;
        }
        Object[] brickObjectArray = (Object[])bricksObj;
        String[] brickNames = new String[brickObjectArray.length];
        for (int i=0; i< brickNames.length; i++){
            brickNames[i] = brickObjectArray[i].toString();
        }
        return brickNames;
    }

    public List<GlusterAsyncTask> getGlusterTasks() {
        return tasks;
    }

}
