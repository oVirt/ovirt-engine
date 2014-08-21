package org.ovirt.engine.core.bll.tasks;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.ovirt.engine.core.bll.tasks.interfaces.SPMTask;
import org.ovirt.engine.core.common.VdcObjectType;
import org.ovirt.engine.core.common.businessentities.AsyncTask;
import org.ovirt.engine.core.common.businessentities.AsyncTaskEntity;
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
     * Adds a task to DB or updates it if already exists in DB
     *
     * @param asyncTask
     *            task to be added or updated
     */
    public static void addOrUpdateTaskInDB(final SPMTask asyncTask) {
        try {
            if (asyncTask.getParameters().getDbAsyncTask() != null) {
                TransactionSupport.executeInScope(TransactionScopeOption.Required, new TransactionMethod<Void>() {

                    @Override
                    public Void runInTransaction() {
                        addOrUpdateTaskInDB(asyncTask.getParameters().getDbAsyncTask());
                        Map<Guid, VdcObjectType> entitiesMap = asyncTask.getEntitiesMap();
                        List<AsyncTaskEntity> asyncTaskEntities =
                                buildAsyncTaskEntities(asyncTask.getParameters().getDbAsyncTask().getTaskId(),
                                        entitiesMap);
                        getAsyncTaskDao().insertAsyncTaskEntities(asyncTaskEntities);
                        return null;
                    }

                });
            }
        } catch (RuntimeException e) {
            log.error(String.format(
                    "Adding/Updating task %1$s to DataBase threw an exception.",
                    Guid.isNullOrEmpty(asyncTask.getVdsmTaskId()) ? asyncTask.getCommandId() : asyncTask.getVdsmTaskId()),
                    e);
        }
    }

    private static List<AsyncTaskEntity> buildAsyncTaskEntities(Guid taskId, Map<Guid, VdcObjectType> entitiesMap) {
        if (entitiesMap == null) {
            entitiesMap = Collections.emptyMap();
        }
        List<AsyncTaskEntity> results = new ArrayList<>(entitiesMap.size());
        for (Map.Entry<Guid, VdcObjectType> entry : entitiesMap.entrySet()) {
            results.add(new AsyncTaskEntity(taskId, entry.getValue(), entry.getKey()));

        }
        return results;
    }

    private static void addOrUpdateTaskInDB(AsyncTask asyncTask) {
        CommandCoordinatorUtil.addOrUpdateTaskInDB(asyncTask);
    }

    private static AsyncTaskDAO getAsyncTaskDao() {
        return DbFacade.getInstance().getAsyncTaskDao();
    }

    private static final Log log = LogFactory.getLog(AsyncTaskUtils.class);

}
