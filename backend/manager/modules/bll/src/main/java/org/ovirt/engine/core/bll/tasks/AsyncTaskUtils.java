package org.ovirt.engine.core.bll.tasks;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.ovirt.engine.core.bll.tasks.interfaces.CommandCoordinator;
import org.ovirt.engine.core.bll.tasks.interfaces.SPMTask;
import org.ovirt.engine.core.common.VdcObjectType;
import org.ovirt.engine.core.common.businessentities.AsyncTaskEntity;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.TransactionScopeOption;
import org.ovirt.engine.core.dao.AsyncTaskDao;
import org.ovirt.engine.core.utils.transaction.TransactionSupport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Helper class for async tasks handling
 */
@Singleton
public class AsyncTaskUtils {
    private static final Logger log = LoggerFactory.getLogger(AsyncTaskUtils.class);

    @Inject
    private AsyncTaskDao asyncTaskDao;

    /**
     * Adds a task to DB or updates it if already exists in DB
     *
     * @param asyncTask
     *            task to be added or updated
     */
    public void addOrUpdateTaskInDB(final CommandCoordinator coco, final SPMTask asyncTask) {
        try {
            if (asyncTask.getParameters().getDbAsyncTask() != null) {
                TransactionSupport.executeInScope(TransactionScopeOption.Required, () -> {
                    coco.addOrUpdateTaskInDB(asyncTask.getParameters().getDbAsyncTask());
                    Map<Guid, VdcObjectType> entitiesMap = asyncTask.getEntitiesMap();
                    List<AsyncTaskEntity> asyncTaskEntities =
                            buildAsyncTaskEntities(asyncTask.getParameters().getDbAsyncTask().getTaskId(),
                                    entitiesMap);
                    asyncTaskDao.insertAsyncTaskEntities(asyncTaskEntities);
                    return null;
                });
            }
        } catch (RuntimeException e) {
            log.error("Adding/Updating task '{}' to DataBase threw an exception: {}",
                    Guid.isNullOrEmpty(asyncTask.getVdsmTaskId()) ? asyncTask.getCommandId() : asyncTask.getVdsmTaskId(),
                    e.getMessage());
            log.debug("Exception", e);
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
}
