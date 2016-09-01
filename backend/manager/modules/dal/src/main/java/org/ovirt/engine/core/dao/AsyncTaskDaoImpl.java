package org.ovirt.engine.core.dao;

import java.util.Collection;
import java.util.List;

import javax.inject.Named;
import javax.inject.Singleton;

import org.ovirt.engine.core.common.VdcObjectType;
import org.ovirt.engine.core.common.asynctasks.AsyncTaskType;
import org.ovirt.engine.core.common.businessentities.AsyncTask;
import org.ovirt.engine.core.common.businessentities.AsyncTaskEntity;
import org.ovirt.engine.core.common.businessentities.AsyncTaskResultEnum;
import org.ovirt.engine.core.common.businessentities.AsyncTaskStatusEnum;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dal.dbbroker.CustomMapSqlParameterSource;
import org.ovirt.engine.core.dal.dbbroker.DbEngineDialect;
import org.ovirt.engine.core.dal.dbbroker.DbFacadeUtils;
import org.ovirt.engine.core.dal.dbbroker.MapSqlParameterMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;

/**
 * {@code AsyncTaskDaoImpl} provides an implementation of {@link AsyncTaskDao}.
 */
@Named
@Singleton
public class AsyncTaskDaoImpl extends BaseDao implements AsyncTaskDao {
    private static final Logger log = LoggerFactory.getLogger(AsyncTaskDaoImpl.class);

    private static final RowMapper<Guid> idRowMapper = (rs, rowNum) -> getGuidDefaultEmpty(rs, "id");

    private static final RowMapper<AsyncTaskEntity> asyncTaskEntityRowMapper = (rs, rowNum) -> {
        AsyncTaskEntity entity = new AsyncTaskEntity();
        entity.setEntityId(getGuid(rs, "entity_id"));
        entity.setTaskId(getGuid(rs, "async_task_id"));
        entity.setEntityType(VdcObjectType.valueOf(rs.getString("entity_type")));
        return entity;
    };

    private static final RowMapper<AsyncTask> asyncTaskRowMapper = (rs, rowNum) -> {
        AsyncTask entity = new AsyncTask();
        entity.setresult(AsyncTaskResultEnum.forValue(rs.getInt("result")));
        entity.setstatus(AsyncTaskStatusEnum.forValue(rs.getInt("status")));
        entity.setUserId(getGuidDefaultEmpty(rs, "user_id"));
        entity.setTaskId(getGuidDefaultEmpty(rs, "task_id"));
        entity.setVdsmTaskId(getGuid(rs, "vdsm_task_id"));
        entity.setStepId(getGuid(rs, "step_id"));
        entity.setCommandId(getGuidDefaultEmpty(rs, "command_id"));
        entity.setRootCommandId(getGuidDefaultEmpty(rs, "root_command_id"));
        entity.setStartTime(DbFacadeUtils.fromDate(rs.getTimestamp("started_at")));
        entity.setTaskType(AsyncTaskType.forValue(rs.getInt("task_type")));
        entity.setStoragePoolId(getGuidDefaultEmpty(rs, "storage_pool_id"));
        return entity;
    };

    private static class AsyncTaskParameterSource extends CustomMapSqlParameterSource {

        public AsyncTaskParameterSource(DbEngineDialect dialect, AsyncTask task) {
            super(dialect);
            addValue("action_type", task.getActionType());
            addValue("result", task.getresult());
            addValue("status", task.getstatus());
            addValue("vdsm_task_id", task.getVdsmTaskId());
            addValue("user_id", task.getUserId());
            addValue("task_id", task.getTaskId());
            addValue("step_id", task.getStepId());
            addValue("command_id", task.getCommandId());
            addValue("root_command_id", task.getRootCommandId());
        }
    }

    private MapSqlParameterMapper<AsyncTaskEntity> mapper = entity -> {
        CustomMapSqlParameterSource paramSource = getCustomMapSqlParameterSource();
        paramSource.addValue("task_id", entity.getTaskId()).
                addValue("entity_id", entity.getEntityId()).
                addValue("entity_type", entity.getEntityType().toString());
        return paramSource;
    };

    @Override
    public AsyncTask get(Guid id) {
        MapSqlParameterSource parameterSource = getCustomMapSqlParameterSource()
                .addValue("task_id", id);

        return getCallsHandler().executeRead("Getasync_tasksBytask_id", asyncTaskRowMapper, parameterSource);
    }

    @Override
    public AsyncTask getByVdsmTaskId(Guid vdsmTaskId) {
        MapSqlParameterSource parameterSource = getCustomMapSqlParameterSource()
                .addValue("vdsm_task_id", vdsmTaskId);
        return getCallsHandler().executeRead("GetAsyncTasksByVdsmTaskId", asyncTaskRowMapper, parameterSource);
    }

    @Override
    public List<AsyncTask> getAll() {
        MapSqlParameterSource parameterSource = getCustomMapSqlParameterSource();

        return getCallsHandler().executeReadList("GetAllFromasync_tasks", asyncTaskRowMapper, parameterSource);
    }

    private AsyncTaskParameterSource getTaskParameterSource(AsyncTask task) {
        return new AsyncTaskParameterSource(getDialect(), task);
    }

    @Override
    public void saveOrUpdate(AsyncTask task) {
        AsyncTaskParameterSource parameterSource = getTaskParameterSource(task);
        parameterSource.addValue("started_at", task.getStartTime());
        parameterSource.addValue("storage_pool_id", task.getStoragePoolId());
        parameterSource.addValue("async_task_type", task.getTaskType());
        getCallsHandler().executeModification("InsertOrUpdateAsyncTasks", parameterSource);
    }

    @Override
    public void save(AsyncTask task) {
        AsyncTaskParameterSource parameterSource = getTaskParameterSource(task);
        parameterSource.addValue("started_at", task.getStartTime());
        parameterSource.addValue("storage_pool_id", task.getStoragePoolId());
        parameterSource.addValue("async_task_type", task.getTaskType());
        getCallsHandler().executeModification("Insertasync_tasks", parameterSource);
    }

    @Override
    public void update(AsyncTask task) {
        logNullParameters(task);

        AsyncTaskParameterSource parameterSource = getTaskParameterSource(task);
        parameterSource.addValue("storage_pool_id", task.getStoragePoolId());
        getCallsHandler().executeModification("Updateasync_tasks", parameterSource);
    }

    private static void logNullParameters(AsyncTask task) {
        if (task.getActionParameters() == null) {
            StringBuilder sb = new StringBuilder("Null action_parameters:\n");
            StackTraceElement[] st = Thread.currentThread().getStackTrace();

            for (StackTraceElement element : st) {
                sb.append(String.format("\tMethod: %1$s%n",
                        element.getMethodName()));
            }

            log.error(sb.toString());
        }
    }

    @Override
    public int remove(Guid id) {
        MapSqlParameterSource parameterSource = getCustomMapSqlParameterSource()
                .addValue("task_id", id);

        return getCallsHandler().executeModificationReturnResult("Deleteasync_tasks", parameterSource);
    }

    public int removeByVdsmTaskId(Guid vdsmTaskId) {
        MapSqlParameterSource parameterSource = getCustomMapSqlParameterSource()
                .addValue("vdsm_task_id", vdsmTaskId);
        return getCallsHandler().executeModificationReturnResult("DeleteAsyncTasksByVdsmTaskId", parameterSource);
    }

    @Override
    public List<Guid> getAsyncTaskIdsByEntity(Guid entityId) {
        MapSqlParameterSource parameterSource = getCustomMapSqlParameterSource()
                .addValue("entity_id", entityId);
        return getCallsHandler().executeReadList("GetAsyncTasksIdsByEntityId", idRowMapper, parameterSource);
    }

    @Override
    public List<Guid> getAsyncTaskIdsByStoragePoolId(Guid storagePoolId) {
        MapSqlParameterSource parameterSource = getCustomMapSqlParameterSource()
                .addValue("storage_pool_id", storagePoolId);
        return getCallsHandler().executeReadList("GetAsyncTasksByStoragePoolId", idRowMapper, parameterSource);
    }

    @Override
    public List<AsyncTask> getTasksByEntity(Guid entityId) {
        MapSqlParameterSource parameterSource = getCustomMapSqlParameterSource()
                .addValue("entity_id", entityId);
        return getCallsHandler().executeReadList("GetAsyncTasksByEntityId",
                asyncTaskRowMapper,
                parameterSource);
    }

    @Override
    public void insertAsyncTaskEntities(Collection<AsyncTaskEntity> asyncTaskEntities) {
        getCallsHandler().executeStoredProcAsBatch("InsertAsyncTaskEntities", asyncTaskEntities, mapper);
    }

    @Override
    public List<AsyncTaskEntity> getAllAsyncTaskEntitiesByTaskId(Guid taskId) {
        MapSqlParameterSource parameterSource = getCustomMapSqlParameterSource()
                .addValue("task_id", taskId);
        return getCallsHandler().executeReadList("GetAsyncTaskEntitiesByTaskId",
                asyncTaskEntityRowMapper,
                parameterSource);
    }
}
