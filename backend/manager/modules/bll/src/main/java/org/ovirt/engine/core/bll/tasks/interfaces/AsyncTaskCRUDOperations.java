package org.ovirt.engine.core.bll.tasks.interfaces;

import org.ovirt.engine.core.common.businessentities.AsyncTasks;
import org.ovirt.engine.core.compat.Guid;

import java.util.List;

public interface AsyncTaskCRUDOperations {
    public abstract List<AsyncTasks> getAllAsyncTasksFromDb();
    public abstract void saveAsyncTaskToDb(AsyncTasks asyncTask);
    public abstract AsyncTasks getAsyncTaskFromDb(Guid asyncTaskId);
    public abstract int removeTaskFromDbByTaskId(Guid taskId) throws RuntimeException;
    public abstract AsyncTasks getByVdsmTaskId(Guid vdsmTaskId);
    public abstract int removeByVdsmTaskId(Guid vdsmTaskId);
    public abstract void addOrUpdateTaskInDB(AsyncTasks asyncTask);
}
