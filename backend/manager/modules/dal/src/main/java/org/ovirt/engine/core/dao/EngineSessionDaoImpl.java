package org.ovirt.engine.core.dao;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedList;
import java.util.List;

import javax.inject.Named;
import javax.inject.Singleton;

import org.apache.commons.lang.StringUtils;
import org.ovirt.engine.core.common.businessentities.EngineSession;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dal.dbbroker.CustomMapSqlParameterSource;
import org.ovirt.engine.core.dal.dbbroker.DbEngineDialect;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;

/**
 * <code>EngineSessionDaoImpl</code> provides an implementation of {@link org.ovirt.engine.core.dao.EngineSessionDao} using code refactored from
 * {@code DbFacade}.
 */
@Named
@Singleton
public class EngineSessionDaoImpl extends BaseDao implements EngineSessionDao {

    private static class EngineSessionRowMapper implements RowMapper<EngineSession> {
        public static final EngineSessionRowMapper instance = new EngineSessionRowMapper();

        @Override
        public EngineSession mapRow(ResultSet rs, int rowNum) throws SQLException {
            EngineSession session = new EngineSession();
            session.setId(rs.getLong("id"));
            session.setEngineSessionId(rs.getString("engine_session_id"));
            session.setUserId(getGuidDefaultEmpty(rs, "user_id"));
            session.setUserName(rs.getString("user_name"));
            session.setAuthzName(rs.getString("authz_name"));
            session.setSourceIp(rs.getString("source_ip"));
            session.setGroupIds(convertToGuidList(rs.getString("group_ids"), ','));
            session.setRoleIds(convertToGuidList(rs.getString("role_ids"), ','));
            return session;
        }

        private LinkedList<Guid> convertToGuidList(String str, char delimiter) {
            LinkedList<Guid> results = new LinkedList<>();
            if (str != null) {
                for (String id : str.split(String.format(" *%s *", delimiter))) {
                    results.add(Guid.createGuidFromString(id));
                }
            }
            return results;
        }
    }

    private static class EngineSessionParameterSource extends CustomMapSqlParameterSource {

        public EngineSessionParameterSource(DbEngineDialect dialect, EngineSession session) {
            super(dialect);
            addValue("id", session.getId());
            addValue("engine_session_id", session.getEngineSessionId());
            addValue("user_id", session.getUserId());
            addValue("user_name", session.getUserName());
            addValue("authz_name", session.getAuthzName());
            addValue("source_ip", session.getSourceIp());
            addValue("group_ids", StringUtils.join(session.getGroupIds(), ","));
            addValue("role_ids", StringUtils.join(session.getRoleIds(), ","));
        }
    }

    @Override
    public EngineSession get(long id) {
        MapSqlParameterSource parameterSource = getCustomMapSqlParameterSource()
                .addValue("id", id);

        return getCallsHandler().executeRead("GetEngineSession", EngineSessionRowMapper.instance, parameterSource);
    }

    @Override
    public EngineSession getBySessionId(String id) {
        MapSqlParameterSource parameterSource = getCustomMapSqlParameterSource()
                .addValue("engine_session_id", id);

        return getCallsHandler().executeRead("GetEngineSessionBySessionId", EngineSessionRowMapper.instance, parameterSource);
    }

    private EngineSessionParameterSource getEngineSessionParameterSource(EngineSession session) {
        return new EngineSessionParameterSource(getDialect(), session);
    }

    @Override
    public long save(EngineSession session) {
        EngineSessionParameterSource parameterSource = getEngineSessionParameterSource(session);
        return ((Integer) getCallsHandler().executeModification("InsertEngineSession", parameterSource).get("id")).longValue();
    }

    @Override
    public int remove(long id) {
        MapSqlParameterSource parameterSource = getCustomMapSqlParameterSource()
                .addValue("id", id);

        return getCallsHandler().executeModificationReturnResult("DeleteEngineSession", parameterSource);
    }

    @Override
    public int removeAll() {
        MapSqlParameterSource parameterSource = getCustomMapSqlParameterSource();

        return getCallsHandler().executeModificationReturnResult("DeleteAllFromEngineSessions", parameterSource);
    }

    @Override
    public List<EngineSession> getAllWithQuery(String query) {
        return getJdbcTemplate().query(query, EngineSessionRowMapper.instance);
    }
}
