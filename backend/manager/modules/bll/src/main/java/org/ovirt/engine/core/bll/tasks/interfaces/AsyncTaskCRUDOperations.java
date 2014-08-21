package org.ovirt.engine.core.bll.tasks.interfaces;

import org.ovirt.engine.core.common.businessentities.AsyncTask;
import org.ovirt.engine.core.compat.Guid;

import java.util.List;

public interface AsyncTaskCRUDOperations {
    public abstract List<AsyncTask> getAllAsyncTasksFromDb();
    public abstract void saveAsyncTaskToDb(AsyncTask asyncTask);
    public abstract AsyncTask getAsyncTaskFromDb(Guid asyncTaskId);
    public abstract int removeTaskFromDbByTaskId(Guid taskId) throws RuntimeException;
    public abstract AsyncTask getByVdsmTaskId(Guid vdsmTaskId);
    public abstract int removeByVdsmTaskId(Guid vdsmTaskId);
    public abstract void addOrUpdateTaskInDB(AsyncTask asyncTask);
}
