package org.ovirt.engine.core.common.asynctasks;

import java.io.Serializable;

import org.ovirt.engine.core.common.businessentities.AsyncTasks;
import org.ovirt.engine.core.compat.Guid;

public class AsyncTaskParameters implements Serializable {
    private AsyncTasks privateDbAsyncTask;

    public AsyncTasks getDbAsyncTask() {
        return privateDbAsyncTask;
    }

    protected void setDbAsyncTask(AsyncTasks value) {
        privateDbAsyncTask = value;
    }

    private AsyncTaskCreationInfo privateCreationInfo;

    protected AsyncTaskCreationInfo getCreationInfo() {
        return privateCreationInfo;
    }

    protected void setCreationInfo(AsyncTaskCreationInfo value) {
        privateCreationInfo = value;
    }

    public AsyncTaskParameters(AsyncTaskCreationInfo info, AsyncTasks dbAsyncTask) {
        setCreationInfo(info);
        setDbAsyncTask(dbAsyncTask);
    }

    public Guid getStoragePoolID() {
        return getCreationInfo().getStoragePoolID();
    }

    public Guid getVdsmTaskId() {
        return getCreationInfo().getVdsmTaskId();
    }

    private EntityInfo _entityInfo;

    public EntityInfo getEntityInfo() {
        return (_entityInfo == null) ? getDbAsyncTask().getActionParameters().getEntityInfo() : _entityInfo;
    }

    public void setEntityInfo(EntityInfo entityInfo) {
        _entityInfo = entityInfo;
    }

    public AsyncTaskParameters() {
    }
}
