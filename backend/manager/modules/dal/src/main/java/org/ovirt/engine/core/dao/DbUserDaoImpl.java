package org.ovirt.engine.core.dao;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import javax.inject.Named;
import javax.inject.Singleton;

import org.ovirt.engine.core.common.businessentities.aaa.DbUser;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dal.dbbroker.CustomMapSqlParameterSource;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.simple.SimpleJdbcCall;

/**
 * <code>DBUserDaoImpl</code> provides an implementation of {@link DbUserDao} with the previously developed
 * {@link org.ovirt.engine.core.dal.dbbroker.DbFacade.DbFacade} code.
 *
 */
@Named
@Singleton
public class DbUserDaoImpl extends BaseDao implements DbUserDao {
    private static class DbUserRowMapper implements RowMapper<DbUser> {
        public static final DbUserRowMapper instance = new DbUserRowMapper();

        @Override
        public DbUser mapRow(ResultSet rs, int rowNum) throws SQLException {
            DbUser entity = new DbUser();
            entity.setDepartment(rs.getString("department"));
            entity.setDomain(rs.getString("domain"));
            entity.setEmail(rs.getString("email"));
            entity.setFirstName(rs.getString("name"));
            entity.setNote(rs.getString("note"));
            entity.setLastName(rs.getString("surname"));
            entity.setId(getGuidDefaultEmpty(rs, "user_id"));
            entity.setLoginName(rs.getString("username"));
            entity.setAdmin(rs.getBoolean("last_admin_check_status"));
            entity.setExternalId(rs.getString("external_id"));
            entity.setNamespace(rs.getString("namespace"));
            return entity;
        }

    }

    private class DbUserMapSqlParameterSource extends
            CustomMapSqlParameterSource {
        public DbUserMapSqlParameterSource(DbUser user) {
            super(getDialect());
            addValue("department", user.getDepartment());
            addValue("domain", user.getDomain());
            addValue("email", user.getEmail());
            addValue("name", user.getFirstName());
            addValue("note", user.getNote());
            addValue("surname", user.getLastName());
            addValue("user_id", user.getId());
            addValue("username", user.getLoginName());
            addValue("last_admin_check_status", user.isAdmin());
            addValue("external_id", user.getExternalId());
            addValue("namespace", user.getNamespace());
        }
    }

    @Override
    public DbUser get(Guid id) {
        return get(id, false);
    }

    @Override
    public DbUser get(Guid id, boolean isFiltered) {
        MapSqlParameterSource parameterSource = getCustomMapSqlParameterSource()
                .addValue("user_id", id)
                .addValue("is_filtered", isFiltered);

        return getCallsHandler().executeRead("GetUserByUserId", DbUserRowMapper.instance, parameterSource);
    }

    @Override
    public DbUser getByUsernameAndDomain(String username, String domainName) {
        MapSqlParameterSource parameterSource = getCustomMapSqlParameterSource()
                .addValue("username", username)
                .addValue("domain", domainName);

        return getCallsHandler().executeRead("GetUserByUserNameAndDomain", DbUserRowMapper.instance, parameterSource);
    }

    @Override
    public DbUser getByExternalId(String domain, String externalId) {
        MapSqlParameterSource parameterSource = getCustomMapSqlParameterSource()
                .addValue("domain", domain)
                .addValue("external_id", externalId);

        return getCallsHandler().executeRead("GetUserByExternalId", DbUserRowMapper.instance, parameterSource);
    }

    @Override
    public DbUser getByIdOrExternalId(Guid id, String domain, String externalId) {
        // Check if there is a user with the given internal identifier:
        if (id != null) {
            DbUser existing = get(id);
            if (existing != null) {
                return existing;
            }
        }

        // Check if there is an existing user for the given external identifier:
        if (domain != null && externalId != null) {
            DbUser existing = getByExternalId(domain, externalId);
            if (existing != null) {
                return existing;
            }
        }

        // In older versions of the engine the internal and external identifiers were the same, so we also need to check
        // if the internal id is really an external id:
        if (domain != null && id != null) {
            DbUser existing = getByExternalId(domain, id.toString());
            if (existing != null) {
                return existing;
            }
        }

        // There is no such existing user:
        return null;
    }

    @Override
    public List<DbUser> getAllForVm(Guid id) {
        MapSqlParameterSource parameterSource = getCustomMapSqlParameterSource()
                .addValue("vm_guid", id);

        return getCallsHandler().executeReadList("GetUsersByVmGuid", DbUserRowMapper.instance, parameterSource);
    }

    @Override
    public List<DbUser> getAllWithQuery(String query) {
        return getJdbcTemplate().query(query, DbUserRowMapper.instance);
    }

    @Override
    public List<DbUser> getAll() {
        return getAll(null, false);
    }

    @Override
    public List<DbUser> getAll(Guid userID, boolean isFiltered) {
        MapSqlParameterSource parameterSource = getCustomMapSqlParameterSource()
                .addValue("user_id", userID).addValue("is_filtered", isFiltered);
        return getCallsHandler().executeReadList("GetAllFromUsers", DbUserRowMapper.instance, parameterSource);
    }

    @Override
    public void save(DbUser user) {
        setIdIfNeeded(user);
        new SimpleJdbcCall(getJdbcTemplate()).withProcedureName("InsertUser")
                .execute(new DbUserMapSqlParameterSource(user));
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
    public void saveOrUpdate(DbUser user) {
        setIdIfNeeded(user);
        new SimpleJdbcCall(getJdbcTemplate()).withProcedureName("InsertOrUpdateUser")
                .execute(new DbUserMapSqlParameterSource(user));
    }

    private void setIdIfNeeded(DbUser user) {
        if (Guid.isNullOrEmpty(user.getId())) {
            user.setId(Guid.newGuid());
        }
    }

}
