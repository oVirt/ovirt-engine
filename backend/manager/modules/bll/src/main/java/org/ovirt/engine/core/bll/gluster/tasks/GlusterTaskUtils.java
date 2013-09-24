package org.ovirt.engine.core.bll.gluster.tasks;

import org.ovirt.engine.core.common.asynctasks.gluster.GlusterTaskType;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterTaskSupport;
import org.ovirt.engine.core.common.job.JobExecutionStatus;

public class GlusterTaskUtils {

    public static boolean isTaskOfType(GlusterTaskSupport supportObj, GlusterTaskType type) {
        if (supportObj.getAsyncTask() != null && supportObj.getAsyncTask().getType() == type) {
            return true;
        }

        return false;
    }

    public static boolean isTaskStatus(GlusterTaskSupport supportObj, JobExecutionStatus status) {
        if (supportObj.getAsyncTask() != null && supportObj.getAsyncTask().getStatus() == status) {
            return true;
        }

        return false;
    }
}
