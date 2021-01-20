package org.ovirt.engine.core.dao;

import java.util.List;

import javax.inject.Named;
import javax.inject.Singleton;

import org.apache.commons.lang.StringUtils;
import org.ovirt.engine.core.common.businessentities.aaa.DbUser;
import org.ovirt.engine.core.compat.Guid;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.simple.SimpleJdbcCall;

/**
 * {@code DBUserDaoImpl} provides an implementation of {@link DbUserDao}.
 */
@Named
@Singleton
public class DbUserDaoImpl extends BaseDao implements DbUserDao {
    private static final RowMapper<DbUser> dbUserRowMapper = (rs, rowNum) -> {
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
    };

    private MapSqlParameterSource getDbUserParameterSource(DbUser user) {
        return getCustomMapSqlParameterSource()
                .addValue("department", user.getDepartment())
                .addValue("domain", user.getDomain())
                .addValue("email", user.getEmail())
                .addValue("name", user.getFirstName())
                .addValue("note", user.getNote())
                .addValue("surname", user.getLastName())
                .addValue("user_id", user.getId())
                .addValue("username", user.getLoginName())
                .addValue("last_admin_check_status", user.isAdmin())
                .addValue("external_id", user.getExternalId())
                .addValue("namespace", user.getNamespace());
    }

    @Override
    public DbUser get(Guid id) {
        return get(id, false);
    }

    @Override
    public DbUser get(Guid id, boolean isFiltered) {
        return getCallsHandler().executeRead("GetUserByUserId",
                dbUserRowMapper,
                getCustomMapSqlParameterSource()
                        .addValue("user_id", id)
                        .addValue("is_filtered", isFiltered));
    }

    @Override
    public DbUser getByUsernameAndDomain(String username, String domainName) {
        return getCallsHandler().executeRead("GetUserByUserNameAndDomain",
                dbUserRowMapper,
                getCustomMapSqlParameterSource()
                        .addValue("username", username)
                        .addValue("domain", domainName));
    }

    @Override
    public DbUser getByExternalId(String domain, String externalId) {
        return getCallsHandler().executeRead("GetUserByExternalId",
                dbUserRowMapper,
                getCustomMapSqlParameterSource()
                        .addValue("domain", domain)
                        .addValue("external_id", externalId));
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
        return getCallsHandler().executeReadList("GetUsersByVmGuid",
                dbUserRowMapper,
                getCustomMapSqlParameterSource()
                        .addValue("vm_guid", id));
    }

    @Override
    public List<DbUser> getAllForTemplate(Guid id) {
        return getCallsHandler().executeReadList("GetUsersByTemplateGuid",
                dbUserRowMapper,
                getCustomMapSqlParameterSource()
                        .addValue("template_guid", id));
    }

    @Override
    public List<DbUser> getAllWithQuery(String query) {
        return getJdbcTemplate().query(query, dbUserRowMapper);
    }

    @Override
    public List<DbUser> getAll() {
        return getAll(null, false);
    }

    @Override
    public List<DbUser> getAll(Guid userID, boolean isFiltered) {
        return getCallsHandler().executeReadList("GetAllFromUsers",
                dbUserRowMapper,
                getCustomMapSqlParameterSource()
                        .addValue("user_id", userID)
                        .addValue("is_filtered", isFiltered));
    }

    @Override
    public void save(DbUser user) {
        setIdIfNeeded(user);
        new SimpleJdbcCall(getJdbcTemplate()).withProcedureName("InsertUser")
                .execute(getDbUserParameterSource(user));
    }

    @Override
    public void update(DbUser user) {
        getCallsHandler().executeModification("UpdateUser", getDbUserParameterSource(user));
    }

    @Override
    public void remove(Guid id) {
        getCallsHandler().executeModification("DeleteUser",
                getCustomMapSqlParameterSource()
                        .addValue("user_id", id));
    }

    @Override
    public void saveOrUpdate(DbUser user) {
        setIdIfNeeded(user);
        new SimpleJdbcCall(getJdbcTemplate()).withProcedureName("InsertOrUpdateUser")
                .execute(getDbUserParameterSource(user));
    }

    @Override
    public void updateLastAdminCheckStatus(Guid... userIds) {
        new SimpleJdbcCall(getJdbcTemplate()).withProcedureName("UpdateLastAdminCheckStatus")
                .execute(getCustomMapSqlParameterSource().addValue("userIds", StringUtils.join(userIds, ",")));
    }

    private void setIdIfNeeded(DbUser user) {
        if (Guid.isNullOrEmpty(user.getId())) {
            user.setId(Guid.newGuid());
        }
    }

}
