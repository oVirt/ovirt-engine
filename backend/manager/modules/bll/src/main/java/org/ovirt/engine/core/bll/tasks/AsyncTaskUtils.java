package org.ovirt.engine.core.bll.tasks;

import java.util.Map;
import java.util.Map.Entry;

import org.ovirt.engine.core.bll.SPMAsyncTask;
import org.ovirt.engine.core.common.VdcObjectType;
import org.ovirt.engine.core.common.businessentities.AsyncTasks;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.TransactionScopeOption;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;
import org.ovirt.engine.core.dao.AsyncTaskDAO;
import org.ovirt.engine.core.utils.log.Log;
import org.ovirt.engine.core.utils.log.LogFactory;
import org.ovirt.engine.core.utils.transaction.TransactionMethod;
import org.ovirt.engine.core.utils.transaction.TransactionSupport;

/**
 * Helper class for async tasks handling
 */
public class AsyncTaskUtils {

    /**
     * Adds a task to DB or updates it if already
     * exists in DB
     * @param asyncTask task to be added or updated
     */
    public static void addOrUpdateTaskInDB(final SPMAsyncTask asyncTask) {
        try {
            if (asyncTask.getParameters().getDbAsyncTask() != null) {
                TransactionSupport.executeInScope(TransactionScopeOption.Required, new TransactionMethod<Void>() {

                    @Override
                    public Void runInTransaction() {
                        addOrUpdateTaskInDB(asyncTask.getParameters().getDbAsyncTask());
                        Map<Guid, VdcObjectType> entitiesMap = asyncTask.getEntitiesMap();
                        for (Entry<Guid, VdcObjectType> entry : entitiesMap.entrySet()) {
                            getAsyncTaskDao().insertAsyncTaskEntity(asyncTask.getParameters()
                                    .getDbAsyncTask().getTaskId(), entry.getKey(), entry.getValue());
                        }
                        return null;
                    }
                });
            }
        } catch (RuntimeException e) {
            log.error(String.format(
                    "Adding/Updating task %1$s to DataBase threw an exception.",
                    asyncTask.getVdsmTaskId()), e);
        }
    }

    private static void addOrUpdateTaskInDB(AsyncTasks asyncTask) {
        getAsyncTaskDao().saveOrUpdate(asyncTask, null, (Guid[]) null);
    }

    public static void addOrUpdateTaskInDB(
            AsyncTasks task,
            VdcObjectType entityType,
            Guid... entityIds) {
        try {
            if (task != null) {
                getAsyncTaskDao()
                        .saveOrUpdate(task,
                                entityType,
                                entityIds);
            }
        } catch (RuntimeException e) {
            log.error(String.format(
                    "Adding/Updating task %1$s to DataBase threw an exception.",
                    task.getTaskId()), e);
        }
    }

    private static AsyncTaskDAO getAsyncTaskDao() {
        return DbFacade.getInstance().getAsyncTaskDao();
    }

    private static final Log log = LogFactory.getLog(AsyncTaskUtils.class);

}
