package org.ovirt.engine.core.dao;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import org.ovirt.engine.core.common.businessentities.DbGroup;
import org.ovirt.engine.core.common.utils.ExternalId;
import org.ovirt.engine.core.compat.Guid;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;

/**
 * Provides a concrete implementation of {@link DbGroupDAO} based on code from
 * {@link DbFacade}.
 */
public class DbGroupDAODbFacadeImpl extends BaseDAODbFacade implements DbGroupDAO {

    @Override
    public DbGroup get(Guid id) {
        return getCallsHandler().executeRead("GetGroupById",
                DbGroupRowMapper.instance,
                getCustomMapSqlParameterSource()
                        .addValue("id", id));
    }

    @Override
    public DbGroup getByExternalId(String domain, ExternalId externalId) {
        return getCallsHandler().executeRead("GetGroupByExternalId",
                DbGroupRowMapper.instance,
                getCustomMapSqlParameterSource()
                       .addValue("domain", domain)
                       .addValue("external_id", externalId.getBytes()));
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
        return jdbcTemplate.query(query, DbGroupRowMapper.instance);
    }

    @Override
    public void save(DbGroup group) {
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
                .addValue("active", group.isActive())
                .addValue("domain", group.getDomain())
                .addValue("distinguishedname", group.getDistinguishedName())
                .addValue("external_id", group.getExternalId()));
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
            entity.setActive(rs.getBoolean("active"));
            entity.setDomain(rs.getString("domain"));
            entity.setDistinguishedName(rs.getString("distinguishedname"));
            entity.setExternalId(new ExternalId(rs.getBytes("external_id")));
            return entity;
        }
    }

}
