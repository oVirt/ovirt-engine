package org.ovirt.engine.core.dao;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.businessentities.async_tasks;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.LogCompat;
import org.ovirt.engine.core.compat.LogFactoryCompat;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.simple.ParameterizedRowMapper;

/**
 * <code>AsyncTaskDAODbFacadeImpl</code> provides an implementation of {@link AsyncTaskDAO} using code refactored from
 * {@link DbFacade}.
 *
 *
 */
public class AsyncTaskDAODbFacadeImpl extends BaseDAODbFacade implements AsyncTaskDAO {
    private static LogCompat log = LogFactoryCompat
                                            .getLog(AsyncTaskDAODbFacadeImpl.class);

    @Override
    public async_tasks get(Guid id) {
        MapSqlParameterSource parameterSource = getCustomMapSqlParameterSource()
                .addValue("task_id", id);

        ParameterizedRowMapper<async_tasks> mapper = new ParameterizedRowMapper<async_tasks>() {
            @Override
            public async_tasks mapRow(ResultSet rs, int rowNum)
                    throws SQLException {
                async_tasks entity = new async_tasks();
                entity.setaction_type(VdcActionType.forValue(rs
                        .getInt("action_type")));
                entity.setresult(org.ovirt.engine.core.common.businessentities.AsyncTaskResultEnum
                        .forValue(rs.getInt("result")));
                entity.setstatus(org.ovirt.engine.core.common.businessentities.AsyncTaskStatusEnum
                        .forValue(rs.getInt("status")));
                entity.settask_id(Guid.createGuidFromString(rs
                        .getString("task_id")));
                entity.setSerializedForm(rs
                        .getBinaryStream("action_parameters"));
                return entity;
            }
        };

        return getCallsHandler().executeRead("Getasync_tasksBytask_id", mapper, parameterSource);
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<async_tasks> getAll() {
        MapSqlParameterSource parameterSource = getCustomMapSqlParameterSource();

        ParameterizedRowMapper<async_tasks> mapper = new ParameterizedRowMapper<async_tasks>() {
            @Override
            public async_tasks mapRow(ResultSet rs, int rowNum)
                    throws SQLException {
                async_tasks entity = new async_tasks();
                entity.setaction_type(VdcActionType.forValue(rs
                        .getInt("action_type")));
                entity.setresult(org.ovirt.engine.core.common.businessentities.AsyncTaskResultEnum
                        .forValue(rs.getInt("result")));
                entity.setstatus(org.ovirt.engine.core.common.businessentities.AsyncTaskStatusEnum
                        .forValue(rs.getInt("status")));
                entity.settask_id(Guid.createGuidFromString(rs
                        .getString("task_id")));
                entity.setSerializedForm(rs
                        .getBinaryStream("action_parameters"));
                return entity;
            }
        };

        return getCallsHandler().executeReadList("GetAllFromasync_tasks", mapper, parameterSource);
    }

    @Override
    public void save(async_tasks task) {
        MapSqlParameterSource parameterSource = getCustomMapSqlParameterSource()
                .addValue("action_type", task.getaction_type())
                .addValue("result", task.getresult())
                .addValue("status", task.getstatus())
                .addValue("task_id", task.gettask_id())
                .addValue("action_parameters", task.getSerializedForm());

        getCallsHandler().executeModification("Insertasync_tasks", parameterSource);
    }

    @Override
    public void update(async_tasks task) {
        logNullParameters(task);

        MapSqlParameterSource parameterSource = getCustomMapSqlParameterSource()
                .addValue("action_type", task.getaction_type())
                .addValue("result", task.getresult())
                .addValue("status", task.getstatus())
                .addValue("task_id", task.gettask_id())
                .addValue("action_parameters", task.getSerializedForm());
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
    public void remove(Guid id) {
        MapSqlParameterSource parameterSource = getCustomMapSqlParameterSource()
                .addValue("task_id", id);

        getCallsHandler().executeModification("Deleteasync_tasks", parameterSource);
    }
}
