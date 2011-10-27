package org.ovirt.engine.core.common.asynctasks;

import org.ovirt.engine.core.compat.*;
import org.ovirt.engine.core.common.businessentities.*;

public class AsyncTaskParameters {
    private async_tasks privateDbAsyncTask;

    public async_tasks getDbAsyncTask() {
        return privateDbAsyncTask;
    }

    protected void setDbAsyncTask(async_tasks value) {
        privateDbAsyncTask = value;
    }

    private AsyncTaskCreationInfo privateCreationInfo;

    protected AsyncTaskCreationInfo getCreationInfo() {
        return privateCreationInfo;
    }

    protected void setCreationInfo(AsyncTaskCreationInfo value) {
        privateCreationInfo = value;
    }

    public AsyncTaskParameters(AsyncTaskCreationInfo info, async_tasks dbAsyncTask) {
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
        return (_entityId == null) ? getDbAsyncTask().getaction_parameters().getEntityId() : _entityId;
    }

    public void setEntityId(Object value) {
        _entityId = value;
    }

    public AsyncTaskParameters() {
    }
}
