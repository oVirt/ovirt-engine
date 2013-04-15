package org.ovirt.engine.core.dao;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import org.ovirt.engine.core.common.businessentities.DbUser;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dal.dbbroker.CustomMapSqlParameterSource;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.simple.SimpleJdbcCall;

/**
 * <code>DBUserDAODbFacadeImpl</code> provides an implementation of {@link DbUserDAO} with the previously developed
 * {@link org.ovirt.engine.core.dal.dbbroker.DbFacade.DbFacade} code.
 *
 */
public class DbUserDAODbFacadeImpl extends BaseDAODbFacade implements DbUserDAO {
    private static class DbUserRowMapper implements RowMapper<DbUser> {
        public static final DbUserRowMapper instance = new DbUserRowMapper();

        @Override
        public DbUser mapRow(ResultSet rs, int rowNum) throws SQLException {
            DbUser entity = new DbUser();
            entity.setdepartment(rs.getString("department"));
            entity.setdomain(rs.getString("domain"));
            entity.setemail(rs.getString("email"));
            entity.setgroups(rs.getString("groups"));
            entity.setname(rs.getString("name"));
            entity.setnote(rs.getString("note"));
            entity.setnote(rs.getString("note"));
            entity.setrole(rs.getString("role"));
            entity.setstatus(rs.getInt("status"));
            entity.setsurname(rs.getString("surname"));
            entity.setuser_id(Guid.createGuidFromString(rs.getString("user_id")));
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
            addValue("domain", user.getdomain());
            addValue("email", user.getemail());
            addValue("groups", user.getgroups());
            addValue("name", user.getname());
            addValue("note", user.getnote());
            addValue("role", user.getrole());
            addValue("status", user.getstatus());
            addValue("surname", user.getsurname());
            addValue("user_id", user.getuser_id());
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
    public List<DbUser> getAllWithQuery(String query) {
        return jdbcTemplate.query(query, DbUserRowMapper.instance);
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
    public void update(DbUser user) {
        getCallsHandler().executeModification("UpdateUser", new DbUserMapSqlParameterSource(user));
    }

    @Override
    public void remove(Guid id) {
        MapSqlParameterSource parameterSource = getCustomMapSqlParameterSource()
                .addValue("user_id", id);

        getCallsHandler().executeModification("DeleteUser", parameterSource);
    }
}
