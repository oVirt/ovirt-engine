package org.ovirt.engine.core.dao;

import java.util.LinkedList;
import java.util.List;

import javax.inject.Named;
import javax.inject.Singleton;

import org.apache.commons.lang.StringUtils;
import org.ovirt.engine.core.common.businessentities.EngineSession;
import org.ovirt.engine.core.compat.Guid;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;

/**
 * {@code EngineSessionDaoImpl} provides an implementation of {@link EngineSessionDao}.
 */
@Named
@Singleton
public class EngineSessionDaoImpl extends BaseDao implements EngineSessionDao {

    private static final RowMapper<EngineSession> engineSessionRowMapper = (rs, rowNum) -> {
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
    };

    private static LinkedList<Guid> convertToGuidList(String str, char delimiter) {
        LinkedList<Guid> results = new LinkedList<>();
        if (str != null) {
            for (String id : str.split(String.format(" *%s *", delimiter))) {
                results.add(Guid.createGuidFromString(id));
            }
        }
        return results;
    }

    @Override
    public EngineSession get(long id) {
        return getCallsHandler().executeRead("GetEngineSession",
                engineSessionRowMapper,
                getCustomMapSqlParameterSource()
                        .addValue("id", id));
    }

    @Override
    public EngineSession getBySessionId(String id) {
        return getCallsHandler().executeRead("GetEngineSessionBySessionId",
                engineSessionRowMapper,
                getCustomMapSqlParameterSource()
                        .addValue("engine_session_id", id));
    }

    private MapSqlParameterSource getEngineSessionParameterSource(EngineSession session) {
        return getCustomMapSqlParameterSource()
                .addValue("id", session.getId())
                .addValue("engine_session_id", session.getEngineSessionId())
                .addValue("user_id", session.getUserId())
                .addValue("user_name", session.getUserName())
                .addValue("authz_name", session.getAuthzName())
                .addValue("source_ip", session.getSourceIp())
                .addValue("group_ids", StringUtils.join(session.getGroupIds(), ","))
                .addValue("role_ids", StringUtils.join(session.getRoleIds(), ","));

    }

    @Override
    public long save(EngineSession session) {
        return ((Integer) getCallsHandler()
                .executeModification("InsertEngineSession", getEngineSessionParameterSource(session))
                .get("id")).longValue();
    }

    @Override
    public int remove(long id) {
        return getCallsHandler().executeModificationReturnResult("DeleteEngineSession",
                getCustomMapSqlParameterSource()
                        .addValue("id", id));
    }

    @Override
    public int removeAll() {
        return getCallsHandler().executeModificationReturnResult("DeleteAllFromEngineSessions",
                getCustomMapSqlParameterSource());
    }

    @Override
    public List<EngineSession> getAllWithQuery(String query) {
        return getJdbcTemplate().query(query, engineSessionRowMapper);
    }
}
