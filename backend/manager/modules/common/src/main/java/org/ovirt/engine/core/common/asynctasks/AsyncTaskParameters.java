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

    public Guid getTaskID() {
        return getCreationInfo().getTaskID();
    }

    private Object _entityId;

    public Object getEntityId() {
        return (_entityId == null) ? getDbAsyncTask().getActionParameters().getEntityId() : _entityId;
    }

    public void setEntityId(Object value) {
        _entityId = value;
    }

    public AsyncTaskParameters() {
    }
}
