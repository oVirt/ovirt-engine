package org.ovirt.engine.core.dao;

import java.util.List;

import javax.inject.Named;
import javax.inject.Singleton;

import org.ovirt.engine.core.common.businessentities.storage.LibvirtSecret;
import org.ovirt.engine.core.common.businessentities.storage.LibvirtSecretUsageType;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dal.dbbroker.DbFacadeUtils;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;

/**
 * {@code LibvirtSecretDaoImpl} provides an implementation of {@link LibvirtSecretDao}.
 */
@Named
@Singleton
public class LibvirtSecretDaoImpl extends DefaultGenericDao<LibvirtSecret, Guid> implements LibvirtSecretDao {

    LibvirtSecretDaoImpl() {
        super("LibvirtSecret");
    }

    @Override
    protected MapSqlParameterSource createFullParametersMapper(LibvirtSecret entity) {
        return createIdParameterMapper(entity.getId())
                .addValue("secret_value", DbFacadeUtils.encryptPassword(entity.getValue()))
                .addValue("secret_usage_type", entity.getUsageType())
                .addValue("secret_description", entity.getDescription())
                .addValue("provider_id", entity.getProviderId())
                .addValue("_create_date", entity.getCreationDate());
    }

    @Override
    protected MapSqlParameterSource createIdParameterMapper(Guid uuid) {
        return getCustomMapSqlParameterSource().addValue("secret_id", uuid);
    }

    @Override
    protected RowMapper<LibvirtSecret> createEntityRowMapper() {
        return libvirtSecretRowMapper;
    }

    private static final RowMapper<LibvirtSecret> libvirtSecretRowMapper = (rs, rowNum) -> {
        LibvirtSecret entity = new LibvirtSecret();

        entity.setId(getGuid(rs, "secret_id"));
        entity.setValue(DbFacadeUtils.decryptPassword(rs.getString("secret_value")));
        entity.setUsageType(LibvirtSecretUsageType.forValue(rs.getInt("secret_usage_type")));
        entity.setDescription(rs.getString("secret_description"));
        entity.setProviderId(getGuid(rs, "provider_id"));
        entity.setCreationDate(DbFacadeUtils.fromDate(rs.getTimestamp("_create_date")));
        return entity;
    };

    @Override
    public List<LibvirtSecret> getAllByProviderId(Guid providerId) {
        return getCallsHandler().executeReadList("GetAllLibvirtSecretsByProviderId",
                libvirtSecretRowMapper,
                getCustomMapSqlParameterSource().addValue("provider_id", providerId));
    }

    @Override
    public List<LibvirtSecret> getAllByStoragePoolIdFilteredByActiveStorageDomains(Guid storagePoolId) {
        return getCallsHandler().executeReadList("GetLibvirtSecretsByPoolIdOnActiveDomains",
                libvirtSecretRowMapper,
                getCustomMapSqlParameterSource().addValue("storage_pool_id", storagePoolId));
    }
}
