package org.ovirt.engine.core.dao;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

import org.ovirt.engine.core.common.businessentities.DbUser;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dal.dbbroker.CustomMapSqlParameterSource;
import org.ovirt.engine.core.dal.dbbroker.DbFacadeUtils;
import org.ovirt.engine.core.dal.dbbroker.user_sessions;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.simple.ParameterizedRowMapper;
import org.springframework.jdbc.core.simple.SimpleJdbcCall;
import org.springframework.jdbc.core.simple.SimpleJdbcTemplate;

/**
 * <code>DBUserDAODbFacadeImpl</code> provides an implementation of {@link DbUserDAO} with the previously developed
 * {@link org.ovirt.engine.core.dal.dbbroker.DbFacade.DbFacade} code.
 *
 */
public class DbUserDAODbFacadeImpl extends BaseDAODbFacade implements DbUserDAO {
    private static class DbUserRowMapper implements ParameterizedRowMapper<DbUser> {
        public static final DbUserRowMapper instance = new DbUserRowMapper();

        @Override
        public DbUser mapRow(ResultSet rs, int rowNum) throws SQLException {
            DbUser entity = new DbUser();
            entity.setdepartment(rs.getString("department"));
            entity.setdesktop_device(rs.getString("desktop_device"));
            entity.setdomain(rs.getString("domain"));
            entity.setemail(rs.getString("email"));
            entity.setgroups(rs.getString("groups"));
            entity.setname(rs.getString("name"));
            entity.setnote(rs.getString("note"));
            entity.setnote(rs.getString("note"));
            entity.setrole(rs.getString("role"));
            entity.setstatus(rs.getInt("status"));
            entity.setsurname(rs.getString("surname"));
            entity.setuser_icon_path(rs.getString("user_icon_path"));
            entity.setuser_id(Guid.createGuidFromString(rs.getString("user_id")));
            entity.setsession_count(rs.getInt("session_count"));
            entity.setusername(rs.getString("username"));
            entity.setLastAdminCheckStatus(rs.getBoolean("last_admin_check_status"));
            entity.setGroupIds(rs.getString("group_ids"));
            return entity;
        }
    }

    private class DbUserMapSqlParameterSource extends
            CustomMapSqlParameterSource {
        public DbUserMapSqlParameterSource(DbUser user) {
            super(dialect);
            addValue("department", user.getdepartment());
            addValue("desktop_device", user.getdesktop_device());
            addValue("domain", user.getdomain());
            addValue("email", user.getemail());
            addValue("groups", user.getgroups());
            addValue("name", user.getname());
            addValue("note", user.getnote());
            addValue("role", user.getrole());
            addValue("status", user.getstatus());
            addValue("surname", user.getsurname());
            addValue("user_icon_path", user.getuser_icon_path());
            addValue("user_id", user.getuser_id());
            addValue("session_count", user.getsession_count());
            addValue("username", user.getusername());
            addValue("last_admin_check_status", user.getLastAdminCheckStatus());
            addValue("group_ids", user.getGroupIds());
        }
    }

    @Override
    public DbUser get(Guid id) {
        MapSqlParameterSource parameterSource = getCustomMapSqlParameterSource()
                .addValue("user_id", id);

        return getCallsHandler().executeRead("GetUserByUserId", DbUserRowMapper.instance, parameterSource);
    }

    @Override
    public DbUser getByUsername(String username) {
        MapSqlParameterSource parameterSource = getCustomMapSqlParameterSource()
                .addValue("username", username);

        return getCallsHandler().executeRead("GetUserByUserName", DbUserRowMapper.instance, parameterSource);
    }

    @Override
    public List<DbUser> getAllForVm(Guid id) {
        MapSqlParameterSource parameterSource = getCustomMapSqlParameterSource()
                .addValue("vm_guid", id);

        return getCallsHandler().executeReadList("GetUsersByVmGuid", DbUserRowMapper.instance, parameterSource);
    }

    @Override
    public List<DbUser> getAllTimeLeasedUsersForVm(int vmid) {
        MapSqlParameterSource parameterSource = getCustomMapSqlParameterSource()
                .addValue("vm_pool_id", vmid);

        return getCallsHandler().executeReadList("Gettime_leasedusers_by_vm_pool_id",
                DbUserRowMapper.instance,
                parameterSource);
    }

    @Override
    public List<DbUser> getAllWithQuery(String query) {
        return new SimpleJdbcTemplate(jdbcTemplate).query(query, DbUserRowMapper.instance);
    }

    @Override
    public List<user_sessions> getAllUserSessions() {
        MapSqlParameterSource parameterSource = getCustomMapSqlParameterSource();

        ParameterizedRowMapper<user_sessions> mapper = new ParameterizedRowMapper<user_sessions>() {
            @Override
            public user_sessions mapRow(ResultSet rs, int rowNum)
                    throws SQLException {
                user_sessions entity = new user_sessions();
                entity.setbrowser(rs.getString("browser"));
                entity.setclient_type(rs.getString("client_type"));
                entity.setlogin_time(DbFacadeUtils.fromDate(rs
                        .getTimestamp("login_time")));
                entity.setos(rs.getString("os"));
                entity.setsession_id(rs.getString("session_id"));
                entity.setuser_id(Guid.createGuidFromString(rs
                        .getString("user_id")));
                return entity;
            }
        };

        return getCallsHandler().executeReadList("GetAllFromuser_sessions", mapper, parameterSource);
    }

    @Override
    public List<DbUser> getAll() {
        MapSqlParameterSource parameterSource = getCustomMapSqlParameterSource();

        return getCallsHandler().executeReadList("GetAllFromUsers", DbUserRowMapper.instance, parameterSource);
    }

    @Override
    public void save(DbUser user) {
        new SimpleJdbcCall(jdbcTemplate).withProcedureName("InsertUser").execute(new DbUserMapSqlParameterSource(user));
    }

    @Override
    public void saveSession(user_sessions session) {
        if (!"".equals(session.getsession_id())) {
            MapSqlParameterSource parameterSource = getCustomMapSqlParameterSource()
                    .addValue("browser", session.getbrowser())
                    .addValue("client_type", session.getclient_type())
                    .addValue("login_time", session.getlogin_time())
                    .addValue("os", session.getos())
                    .addValue("session_id", session.getsession_id())
                    .addValue("user_id", session.getuser_id());

            getCallsHandler().executeModification("Insertuser_sessions", parameterSource);
        }
    }

    @Override
    public void update(DbUser user) {
        getCallsHandler().executeModification("UpdateUser", new DbUserMapSqlParameterSource(user));
    }

    @Override
    public void remove(Guid id) {
        MapSqlParameterSource parameterSource = getCustomMapSqlParameterSource()
                .addValue("user_id", id);

        getCallsHandler().executeModification("DeleteUser", parameterSource);
    }

    @Override
    public void removeUserSession(String sessionid, Guid userid) {
        MapSqlParameterSource parameterSource = getCustomMapSqlParameterSource()
                .addValue("session_id", sessionid).addValue("user_id", userid);

        new SimpleJdbcCall(jdbcTemplate).withProcedureName("Deleteuser_sessions").execute(parameterSource);
    }

    @Override
    public void removeUserSessions(Map<String, Guid> sessionmap) {
        for (Map.Entry<String, Guid> entry : sessionmap.entrySet()) {
            removeUserSession(entry.getKey(), entry.getValue());
        }
    }

    @Override
    public void removeAllSessions() {
        MapSqlParameterSource parameterSource = getCustomMapSqlParameterSource();

        new SimpleJdbcCall(jdbcTemplate).withProcedureName("DeleteAlluser_sessions").execute(parameterSource);
    }
}
