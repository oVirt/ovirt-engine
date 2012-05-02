package org.ovirt.engine.core.dao;

import java.io.Serializable;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import org.ovirt.engine.core.common.action.VdcActionParametersBase;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.businessentities.async_tasks;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.NGuid;
import org.ovirt.engine.core.dal.dbbroker.CustomMapSqlParameterSource;
import org.ovirt.engine.core.dal.dbbroker.DbEngineDialect;
import org.ovirt.engine.core.utils.ReflectionUtils;
import org.ovirt.engine.core.utils.log.Log;
import org.ovirt.engine.core.utils.log.LogFactory;
import org.ovirt.engine.core.utils.serialization.json.JsonObjectDeserializer;
import org.ovirt.engine.core.utils.serialization.json.JsonObjectSerializer;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.simple.ParameterizedRowMapper;

/**
 * <code>AsyncTaskDAODbFacadeImpl</code> provides an implementation of {@link AsyncTaskDAO} using code refactored from
 * {@code DbFacade}.
 */
public class AsyncTaskDAODbFacadeImpl extends BaseDAODbFacade implements AsyncTaskDAO {
    private static Log log = LogFactory
                                            .getLog(AsyncTaskDAODbFacadeImpl.class);

    private static class AsyncTaskRowMapper implements ParameterizedRowMapper<async_tasks> {

        @Override
        public async_tasks mapRow(ResultSet rs, int rowNum) throws SQLException {
            async_tasks entity = new async_tasks();
            entity.setaction_type(VdcActionType.forValue(rs
                            .getInt("action_type")));
            entity.setresult(org.ovirt.engine.core.common.businessentities.AsyncTaskResultEnum
                            .forValue(rs.getInt("result")));
            entity.setstatus(org.ovirt.engine.core.common.businessentities.AsyncTaskStatusEnum
                            .forValue(rs.getInt("status")));
            entity.settask_id(Guid.createGuidFromString(rs
                            .getString("task_id")));
            entity.setaction_parameters(deserializeParameters(rs.getString("action_parameters"),rs.getString("action_params_class")));
            entity.setStepId(NGuid.createGuidFromString(rs
                    .getString("step_id")));
            entity.setCommandId(Guid.createGuidFromString(rs.getString("command_id")));
            return entity;
        }

        @SuppressWarnings("unchecked")
        private VdcActionParametersBase deserializeParameters(String payload, String className) {
            if (className == null) {
                return null;
            }
            Class<Serializable> actionParamsClass = (Class<Serializable>)ReflectionUtils.getClassFor(className);
            return (VdcActionParametersBase)new JsonObjectDeserializer().deserialize(payload, actionParamsClass);
        }
    }

    private static class AsyncTaskParameterSource extends CustomMapSqlParameterSource {

        public AsyncTaskParameterSource(DbEngineDialect dialect,async_tasks task) {
            super(dialect);
            addValue("action_type", task.getaction_type());
            addValue("result", task.getresult());
            addValue("status", task.getstatus());
            addValue("task_id", task.gettask_id());
            addValue("action_parameters", serializeParameters(task.getaction_parameters()));
            addValue("action_params_class",task.getaction_parameters().getClass().getName());
            addValue("step_id", task.getStepId());
            addValue("command_id", task.getCommandId());
        }

        private String serializeParameters(VdcActionParametersBase params) {
            VdcActionParametersBase parentParams = params.getParentParameters();
            params.setParentParemeters(null);
            String jsonStr = new JsonObjectSerializer().serialize(params);
            params.setParentParemeters(parentParams);
            return jsonStr;
        }
    }

    @Override
    public async_tasks get(Guid id) {
        MapSqlParameterSource parameterSource = getCustomMapSqlParameterSource()
                .addValue("task_id", id);

        AsyncTaskRowMapper mapper = new AsyncTaskRowMapper();
        return getCallsHandler().executeRead("Getasync_tasksBytask_id", mapper, parameterSource);
    }

    @Override
    public List<async_tasks> getAll() {
        MapSqlParameterSource parameterSource = getCustomMapSqlParameterSource();

        AsyncTaskRowMapper mapper = new AsyncTaskRowMapper();
        return getCallsHandler().executeReadList("GetAllFromasync_tasks", mapper, parameterSource);
    }

    private AsyncTaskParameterSource getTaskParameterSource(async_tasks task) {
        return new AsyncTaskParameterSource(dialect,task);
    }

    @Override
    public void save(async_tasks task) {
        AsyncTaskParameterSource parameterSource = getTaskParameterSource(task);
        getCallsHandler().executeModification("Insertasync_tasks", parameterSource);
    }


    @Override
    public void update(async_tasks task) {
        logNullParameters(task);

        AsyncTaskParameterSource parameterSource = getTaskParameterSource(task);
        getCallsHandler().executeModification("Updateasync_tasks", parameterSource);
    }

    private void logNullParameters(async_tasks task) {
        if (task.getaction_parameters() == null) {
            StringBuilder sb = new StringBuilder("Null action_parameters:\n");
            java.lang.StackTraceElement[] st = java.lang.Thread.currentThread()
                    .getStackTrace();

            for (int i = 0; i < st.length; i++) {
                sb.append(String.format("\tMethod: %1$s\n",
                        st[i].getMethodName()));
            }

            log.error(sb.toString());
        }
    }

    @Override
    public int remove(Guid id) {
        MapSqlParameterSource parameterSource = getCustomMapSqlParameterSource()
                .addValue("task_id", id);

        return getCallsHandler().executeModificationRowsAffected("Deleteasync_tasks", parameterSource);
    }
}
