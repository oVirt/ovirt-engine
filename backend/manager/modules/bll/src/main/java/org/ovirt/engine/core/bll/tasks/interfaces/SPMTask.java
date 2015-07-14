package org.ovirt.engine.core.bll.tasks.interfaces;

import java.util.Map;

import org.ovirt.engine.core.bll.tasks.AsyncTaskState;
import org.ovirt.engine.core.common.VdcObjectType;
import org.ovirt.engine.core.common.asynctasks.AsyncTaskParameters;
import org.ovirt.engine.core.common.businessentities.AsyncTaskStatus;
import org.ovirt.engine.core.compat.Guid;

public interface SPMTask {
    void concreteStartPollingTask();

    void startPollingTask();

    /**
     * For each task set its updated status retrieved from VDSM.
     *
     * @param returnTaskStatus
     *            - Task status returned from VDSM.
     */
    void updateTask(AsyncTaskStatus returnTaskStatus);

    void clearAsyncTask();

    void clearAsyncTask(boolean forceDelete);

    Object getCommandId();

    long getLastAccessToStatusSinceEnd();

    AsyncTaskStatus getLastTaskStatus();

    void setLastTaskStatus(AsyncTaskStatus taskStatus);

    AsyncTaskParameters getParameters();

    boolean getShouldPoll();

    AsyncTaskState getState();

    Guid getStoragePoolID();

    Guid getVdsmTaskId();

    /**
     * Update task last access date ,only for not active task.
     */
    void setLastStatusAccessTime();

    void setParameters(AsyncTaskParameters value);

    void setState(AsyncTaskState value);

    void stopTask();

    void stopTask(boolean forceFinish);

    void setPartiallyCompletedCommandTask(boolean val);

    boolean isPartiallyCompletedCommandTask();

    Map<Guid, VdcObjectType> getEntitiesMap();

    void setEntitiesMap(Map<Guid, VdcObjectType> entitiesMap);

    void setZombieTask(boolean val);

    boolean isZombieTask();
}
