package org.ovirt.engine.core.bll.tasks.interfaces;

import java.util.List;

import org.ovirt.engine.core.common.businessentities.AsyncTask;
import org.ovirt.engine.core.compat.Guid;

public interface AsyncTaskCRUDOperations {

    List<AsyncTask> getAllAsyncTasksFromDb();

    void saveAsyncTaskToDb(AsyncTask asyncTask);

    AsyncTask getAsyncTaskFromDb(Guid asyncTaskId);

    int removeTaskFromDbByTaskId(Guid taskId) throws RuntimeException;

    AsyncTask getByVdsmTaskId(Guid vdsmTaskId);

    int removeByVdsmTaskId(Guid vdsmTaskId);

    void addOrUpdateTaskInDB(AsyncTask asyncTask);
}
