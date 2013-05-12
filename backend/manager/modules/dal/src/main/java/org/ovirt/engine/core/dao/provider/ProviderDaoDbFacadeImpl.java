package org.ovirt.engine.core.dao.provider;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import org.ovirt.engine.core.common.businessentities.Provider;
import org.ovirt.engine.core.common.businessentities.ProviderType;
import org.ovirt.engine.core.common.utils.EnumUtils;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dal.dbbroker.DbFacadeUtils;
import org.ovirt.engine.core.dao.DefaultGenericDaoDbFacade;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.simple.ParameterizedRowMapper;

public class ProviderDaoDbFacadeImpl extends DefaultGenericDaoDbFacade<Provider, Guid> implements ProviderDao {

    public ProviderDaoDbFacadeImpl() {
        super("Provider");
    }

    @Override
    protected MapSqlParameterSource createFullParametersMapper(Provider entity) {
        return createIdParameterMapper(entity.getId())
                .addValue("name", entity.getName())
                .addValue("description", entity.getDescription())
                .addValue("url", entity.getUrl())
                .addValue("provider_type", EnumUtils.nameOrNull(entity.getType()))
                .addValue("auth_required", entity.isRequiringAuthentication())
                .addValue("auth_username", entity.getUsername())
                .addValue("auth_password", DbFacadeUtils.encryptPassword(entity.getPassword()));
    }

    @Override
    protected MapSqlParameterSource createIdParameterMapper(Guid id) {
        return getCustomMapSqlParameterSource().addValue("id", id);
    }

    @Override
    protected ParameterizedRowMapper<Provider> createEntityRowMapper() {
        return ProviderRowMapper.INSTANCE;
    }

    @Override
    public Provider getByName(String name) {
        return getCallsHandler().executeRead("GetProviderByName",
                createEntityRowMapper(),
                getCustomMapSqlParameterSource().addValue("name", name));
    }

    private static class ProviderRowMapper implements ParameterizedRowMapper<Provider> {

        public final static ProviderRowMapper INSTANCE = new ProviderRowMapper();

        private ProviderRowMapper() {
        }

        @Override
        public Provider mapRow(ResultSet rs, int index) throws SQLException {
            Provider entity = new Provider();
            entity.setId(Guid.createGuidFromString(rs.getString("id")));
            entity.setName(rs.getString("name"));
            entity.setDescription(rs.getString("description"));
            entity.setUrl(rs.getString("url"));
            entity.setType(ProviderType.valueOf(rs.getString("provider_type")));
            entity.setRequiringAuthentication(rs.getBoolean("auth_required"));
            entity.setUsername(rs.getString("auth_username"));
            entity.setPassword(DbFacadeUtils.decryptPassword(rs.getString("auth_password")));
            return entity;
        }
    }

    @Override
    public List<Provider> getAllByType(ProviderType providerType) {
        return getCallsHandler().executeReadList("GetAllFromProvidersByType",
                                                 ProviderRowMapper.INSTANCE,
                                                 getCustomMapSqlParameterSource().addValue("provider_type", providerType.toString()));
    }

    public List<Provider> getAllWithQuery(String query) {
        return jdbcTemplate.query(query, ProviderRowMapper.INSTANCE);
    }
}
