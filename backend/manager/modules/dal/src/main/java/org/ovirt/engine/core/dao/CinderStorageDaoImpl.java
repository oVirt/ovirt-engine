package org.ovirt.engine.core.dao;

import java.util.List;
import java.util.Map;

import javax.inject.Named;
import javax.inject.Singleton;

import org.ovirt.engine.core.common.businessentities.storage.ManagedBlockStorage;
import org.ovirt.engine.core.common.utils.ObjectUtils;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dal.dbbroker.DbFacadeUtils;
import org.ovirt.engine.core.utils.JsonHelper;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;

@Named
@Singleton
public class CinderStorageDaoImpl extends DefaultGenericDao<ManagedBlockStorage, Guid> implements CinderStorageDao {

    public CinderStorageDaoImpl() {
        super("CinderStorage");
        setProcedureNameForGet("GetCinderStorage");
    }

    @Override
    public List<ManagedBlockStorage> getCinderStorageByDrivers(Map<String, Object> driverOptions) {
        return getCallsHandler().executeReadList("GetCinderStorageByDrivers",
                cinderStorageDomainStaticRowMapper,
                getCustomMapSqlParameterSource()
                        .addValue("driver_options",
                                ObjectUtils.mapNullable(driverOptions, JsonHelper::mapToJsonUnchecked)));
    }

    @Override
    protected MapSqlParameterSource createFullParametersMapper(ManagedBlockStorage storage) {
        return createIdParameterMapper(storage.getId())
                .addValue("driver_options",
                        ObjectUtils.mapNullable(storage.getDriverOptions(), JsonHelper::mapToJsonUnchecked))
                .addValue("driver_sensitive_options",
                        DbFacadeUtils.encryptPassword(ObjectUtils.mapNullable(storage.getDriverSensitiveOptions(),
                                JsonHelper::mapToJsonUnchecked)));
    }

    @Override
    protected MapSqlParameterSource createIdParameterMapper(Guid guid) {
        return getCustomMapSqlParameterSource().addValue("storage_domain_id", guid);
    }

    @Override
    protected RowMapper<ManagedBlockStorage> createEntityRowMapper() {
        return cinderStorageDomainStaticRowMapper;
    }

    private static final RowMapper<ManagedBlockStorage> cinderStorageDomainStaticRowMapper = (rs, rowNum) -> {
        ManagedBlockStorage entity = new ManagedBlockStorage();
        entity.setId(getGuidDefaultNewGuid(rs, "storage_domain_id"));
        entity.setDriverOptions(ObjectUtils.mapNullable(rs.getString("driver_options"), JsonHelper::jsonToMapUnchecked));
        entity.setDriverSensitiveOptions(ObjectUtils.mapNullable(DbFacadeUtils.decryptPassword(rs.getString("driver_sensitive_options")),
                JsonHelper::jsonToMapUnchecked));
        return entity;
    };
}
