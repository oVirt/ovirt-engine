package org.ovirt.engine.core.dao;

import java.io.Serializable;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.ovirt.engine.core.common.VdcObjectType;
import org.ovirt.engine.core.common.action.VdcActionParametersBase;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.asynctasks.AsyncTaskType;
import org.ovirt.engine.core.common.businessentities.AsyncTaskResultEnum;
import org.ovirt.engine.core.common.businessentities.AsyncTaskStatusEnum;
import org.ovirt.engine.core.common.businessentities.AsyncTasks;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dal.dbbroker.CustomMapSqlParameterSource;
import org.ovirt.engine.core.dal.dbbroker.DbEngineDialect;
import org.ovirt.engine.core.dal.dbbroker.DbFacadeUtils;
import org.ovirt.engine.core.utils.ReflectionUtils;
import org.ovirt.engine.core.utils.SerializationFactory;
import org.ovirt.engine.core.utils.log.Log;
import org.ovirt.engine.core.utils.log.LogFactory;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;

/**
 * <code>AsyncTaskDAODbFacadeImpl</code> provides an implementation of {@link AsyncTaskDAO} using code refactored from
 * {@code DbFacade}.
 */
public class AsyncTaskDAODbFacadeImpl extends BaseDAODbFacade implements AsyncTaskDAO {
    private static final Guid[] EMPTY_GUIDS_ARRAY = new Guid[0];
    private static final Log log = LogFactory.getLog(AsyncTaskDAODbFacadeImpl.class);

    private static class IdRowMapper implements RowMapper<Guid> {
        public static final IdRowMapper instance = new IdRowMapper();

        @Override
        public Guid mapRow(ResultSet rs, int rowNum) throws SQLException {
            return Guid.createGuidFromStringDefaultEmpty(rs.getString("id"));
        }

    }

    private static class AsyncTaskRowMapper implements RowMapper<AsyncTasks> {
        public static final AsyncTaskRowMapper instance = new AsyncTaskRowMapper();

        @Override
        public AsyncTasks mapRow(ResultSet rs, int rowNum) throws SQLException {
            AsyncTasks entity = new AsyncTasks();
            entity.setaction_type(VdcActionType.forValue(rs.getInt("action_type")));
            entity.setresult(AsyncTaskResultEnum.forValue(rs.getInt("result")));
            entity.setstatus(AsyncTaskStatusEnum.forValue(rs.getInt("status")));
            entity.setTaskId(Guid.createGuidFromStringDefaultEmpty(rs.getString("task_id")));
            entity.setVdsmTaskId(Guid.createGuidFromString(rs.getString("vdsm_task_id")));
            entity.setActionParameters(deserializeParameters(rs.getString("action_parameters"),rs.getString("action_params_class")));
            entity.setTaskParameters(deserializeParameters(rs.getString("task_parameters"),rs.getString("task_params_class")));
            entity.setStepId(Guid.createGuidFromString(rs.getString("step_id")));
            entity.setCommandId(Guid.createGuidFromStringDefaultEmpty(rs.getString("command_id")));
            entity.setRootCommandId(Guid.createGuidFromStringDefaultEmpty(rs.getString("root_command_id")));
            entity.setStartTime(DbFacadeUtils.fromDate(rs.getTimestamp("started_at")));
            entity.setTaskType(AsyncTaskType.forValue(rs.getInt("task_type")));
            entity.setStoragePoolId(Guid.createGuidFromStringDefaultEmpty(rs.getString("storage_pool_id")));
            return entity;
        }

        @SuppressWarnings("unchecked")
        private static VdcActionParametersBase deserializeParameters(String payload, String className) {
            if (className == null) {
                return null;
            }
            Class<Serializable> actionParamsClass = (Class<Serializable>) ReflectionUtils.getClassFor(className);
            return (VdcActionParametersBase) SerializationFactory.getDeserializer().deserialize(payload,
                    actionParamsClass);
        }
    }

    private static class AsyncTaskParameterSource extends CustomMapSqlParameterSource {

        public AsyncTaskParameterSource(DbEngineDialect dialect,AsyncTasks task) {
            super(dialect);
            addValue("action_type", task.getaction_type());
            addValue("result", task.getresult());
            addValue("status", task.getstatus());
            addValue("vdsm_task_id", task.getVdsmTaskId());
            addValue("task_id", task.getTaskId());
            addValue("action_parameters", serializeParameters(task.getActionParameters()));
            addValue("action_params_class",task.getActionParameters().getClass().getName());
            addValue("task_parameters", serializeParameters(task.getTaskParameters()));
            addValue("task_params_class",task.getTaskParameters().getClass().getName());
            addValue("step_id", task.getStepId());
            addValue("command_id", task.getCommandId());
            addValue("root_command_id", task.getRootCommandId());
        }

        private static String serializeParameters(VdcActionParametersBase params) {
            return SerializationFactory.getSerializer().serialize(params);
        }
    }

    @Override
    public AsyncTasks get(Guid id) {
        MapSqlParameterSource parameterSource = getCustomMapSqlParameterSource()
                .addValue("task_id", id);

        return getCallsHandler().executeRead("Getasync_tasksBytask_id", AsyncTaskRowMapper.instance, parameterSource);
    }

    @Override
    public AsyncTasks getByVdsmTaskId(Guid vdsmTaskId) {
        MapSqlParameterSource parameterSource = getCustomMapSqlParameterSource()
                .addValue("vdsm_task_id", vdsmTaskId);
        return getCallsHandler().executeRead("GetAsyncTasksByVdsmTaskId", AsyncTaskRowMapper.instance, parameterSource);
    }

    @Override
    public List<AsyncTasks> getAll() {
        MapSqlParameterSource parameterSource = getCustomMapSqlParameterSource();

        return getCallsHandler().executeReadList("GetAllFromasync_tasks", AsyncTaskRowMapper.instance, parameterSource);
    }

    private AsyncTaskParameterSource getTaskParameterSource(AsyncTasks task) {
        return new AsyncTaskParameterSource(dialect,task);
    }

    @Override
    public void save(AsyncTasks task, VdcObjectType entityType, Guid... entityIds) {
        AsyncTaskParameterSource parameterSource = getTaskParameterSource(task);
        parameterSource.addValue("entity_type", (entityType != null) ? entityType.toString() : null);
        parameterSource.addValue("started_at", task.getStartTime());
        parameterSource.addValue("storage_pool_id",task.getStoragePoolId());
        parameterSource.addValue("async_task_type", task.getTaskType());
        parameterSource.addValue("entity_ids", StringUtils.join(entityIds, ","));
        getCallsHandler().executeModification("Insertasync_tasks", parameterSource);
    }

    @Override
    public void saveOrUpdate(AsyncTasks task, VdcObjectType entityType, Guid... entityIds) {
        AsyncTaskParameterSource parameterSource = getTaskParameterSource(task);
        parameterSource.addValue("entity_type", (entityType != null) ? entityType.toString() : null);
        parameterSource.addValue("started_at", task.getStartTime());
        parameterSource.addValue("storage_pool_id",task.getStoragePoolId());
        parameterSource.addValue("async_task_type", task.getTaskType());
        parameterSource.addValue("entity_ids", StringUtils.join(entityIds, ","));
        getCallsHandler().executeModification("InsertOrUpdateAsyncTasks", parameterSource);
    }

    @Override
    public void save(AsyncTasks task) {
        save(task, null, EMPTY_GUIDS_ARRAY);
    }


    @Override
    public void update(AsyncTasks task) {
        logNullParameters(task);

        AsyncTaskParameterSource parameterSource = getTaskParameterSource(task);
        getCallsHandler().executeModification("Updateasync_tasks", parameterSource);
    }

    private static void logNullParameters(AsyncTasks task) {
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
        return getCallsHandler().executeReadList("GetAsyncTasksIdsByEntityId", IdRowMapper.instance, parameterSource);
    }

    @Override
    public List<Guid> getAsyncTaskIdsByStoragePoolId(Guid storagePoolId) {
        MapSqlParameterSource parameterSource = getCustomMapSqlParameterSource()
                .addValue("storage_pool_id", storagePoolId);
        return getCallsHandler().executeReadList("GetAsyncTasksByStoragePoolId", IdRowMapper.instance, parameterSource);
    }

    @Override
    public List<AsyncTasks> getTasksByEntity(Guid entityId) {
        MapSqlParameterSource parameterSource = getCustomMapSqlParameterSource()
                .addValue("entity_id", entityId);
        return getCallsHandler().executeReadList("GetAsyncTasksByEntityId",
                AsyncTaskRowMapper.instance,
                parameterSource);
    }

    @Override
    public void insertAsyncTaskEntity(Guid taskId, Guid entityId, VdcObjectType entityType) {
        MapSqlParameterSource parameterSource = getCustomMapSqlParameterSource();
        parameterSource.addValue("task_id", taskId).
                addValue("entity_id", entityId).
                addValue("entity_type", entityType);
        getCallsHandler().executeModification("InsertAsyncTaskEntities", parameterSource);

    }
}
