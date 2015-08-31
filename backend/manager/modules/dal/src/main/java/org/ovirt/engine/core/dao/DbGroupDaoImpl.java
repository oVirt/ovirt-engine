package org.ovirt.engine.core.dao;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import javax.inject.Named;
import javax.inject.Singleton;

import org.ovirt.engine.core.common.businessentities.aaa.DbGroup;
import org.ovirt.engine.core.compat.Guid;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;

/**
 * Provides a concrete implementation of {@link DbGroupDao} based on code from
 * {@link DbFacade}.
 */
@Named
@Singleton
public class DbGroupDaoImpl extends BaseDao implements DbGroupDao {

    @Override
    public DbGroup get(Guid id) {
        return getCallsHandler().executeRead("GetGroupById",
                DbGroupRowMapper.instance,
                getCustomMapSqlParameterSource()
                        .addValue("id", id));
    }

    @Override
    public DbGroup getByExternalId(String domain, String externalId) {
        return getCallsHandler().executeRead("GetGroupByExternalId",
                DbGroupRowMapper.instance,
                getCustomMapSqlParameterSource()
                       .addValue("domain", domain)
                        .addValue("external_id", externalId));
    }


    @Override
    public DbGroup getByIdOrExternalId(Guid id, String domain, String externalId) {
        // Check if there is a user with the given internal identifier:
        if (id != null) {
            DbGroup existing = get(id);
            if (existing != null) {
                return existing;
            }
        }

        // Check if there is an existing user for the given external identifier:
        if (domain != null && externalId != null) {
            DbGroup existing = getByExternalId(domain, externalId);
            if (existing != null) {
                return existing;
            }
        }

        // In older versions of the engine the internal and external identifiers were the same, so we also need to check
        // if the internal id is really an external id:
        if (domain != null && id != null) {
            DbGroup existing = getByExternalId(domain, id.toString());
            if (existing != null) {
                return existing;
            }
        }

        // There is no such existing user:
        return null;
    }
    @Override
    public DbGroup getByName(String name) {
        return getCallsHandler().executeRead("GetGroupByName",
                DbGroupRowMapper.instance,
                getCustomMapSqlParameterSource()
                        .addValue("name", name));
    }

    @Override
    public List<DbGroup> getAll() {
        return getCallsHandler().executeReadList("GetAllGroups",
                DbGroupRowMapper.instance,
                getCustomMapSqlParameterSource());
    }


    @Override
    public List<DbGroup> getAllWithQuery(String query) {
        return getJdbcTemplate().query(query, DbGroupRowMapper.instance);
    }

    @Override
    public void save(DbGroup group) {
        if (Guid.isNullOrEmpty(group.getId())) {
            group.setId(Guid.newGuid());
        }
        insertOrUpdate(group, "InsertGroup");
    }

    @Override
    public void update(DbGroup group) {
        insertOrUpdate(group, "UpdateGroup");
    }

    private void insertOrUpdate(final DbGroup group, final String storedProcName) {
        getCallsHandler().executeModification(storedProcName, getCustomMapSqlParameterSource()
                .addValue("id", group.getId())
                .addValue("name", group.getName())
                .addValue("domain", group.getDomain())
                .addValue("distinguishedname", group.getDistinguishedName())
                .addValue("external_id", group.getExternalId())
                .addValue("namespace", group.getNamespace()));
    }

    @Override
    public void remove(Guid id) {
        MapSqlParameterSource parameterSource = getCustomMapSqlParameterSource()
                .addValue("id", id);

        getCallsHandler().executeModification("DeleteGroup", parameterSource);
    }

    private static final class DbGroupRowMapper implements RowMapper<DbGroup> {
        public static final DbGroupRowMapper instance = new DbGroupRowMapper();

        @Override
        public DbGroup mapRow(final ResultSet rs, final int rowNum) throws SQLException {
            DbGroup entity = new DbGroup();
            entity.setId(getGuidDefaultEmpty(rs, "id"));
            entity.setName(rs.getString("name"));
            entity.setDomain(rs.getString("domain"));
            entity.setDistinguishedName(rs.getString("distinguishedname"));
            entity.setExternalId(rs.getString("external_id"));
            entity.setNamespace(rs.getString("namespace"));
            return entity;
        }
    }

}
