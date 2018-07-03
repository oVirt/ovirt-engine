package org.ovirt.engine.core.dao;

import java.util.List;

import javax.inject.Named;
import javax.inject.Singleton;

import org.ovirt.engine.core.common.businessentities.ExternalStatus;
import org.ovirt.engine.core.common.businessentities.StorageDomainDynamic;
import org.ovirt.engine.core.compat.Guid;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;

@Named
@Singleton
public class StorageDomainDynamicDaoImpl extends BaseDao implements StorageDomainDynamicDao{

    private static final RowMapper<StorageDomainDynamic> storageDomainDynamicRowMapper = (rs, rowNum) -> {
        StorageDomainDynamic entity = new StorageDomainDynamic();
        entity.setAvailableDiskSize((Integer) rs.getObject("available_disk_size"));
        entity.setConfirmedAvailableDiskSize((Integer) rs.getObject("confirmed_available_disk_size"));
        entity.setId(getGuidDefaultEmpty(rs, "id"));
        entity.setUsedDiskSize((Integer) rs.getObject("used_disk_size"));
        entity.setExternalStatus(ExternalStatus.forValue(rs.getInt("external_status")));
        return entity;
    };


    @Override
    public StorageDomainDynamic get(Guid id) {
        MapSqlParameterSource parameterSource = getCustomMapSqlParameterSource()
                .addValue("id", id);
        return getCallsHandler().executeRead("Getstorage_domain_dynamicByid",
                storageDomainDynamicRowMapper,
                parameterSource);
    }


    @Override
    public void save(StorageDomainDynamic domain) {
        MapSqlParameterSource parameterSource = getCustomMapSqlParameterSource()
                .addValue("available_disk_size",
                        domain.getAvailableDiskSize())
                .addValue("id", domain.getId())
                .addValue("used_disk_size", domain.getUsedDiskSize());

        getCallsHandler().executeModification("Insertstorage_domain_dynamic", parameterSource);
    }

    @Override
    public void update(StorageDomainDynamic domain) {
        MapSqlParameterSource parameterSource = getCustomMapSqlParameterSource()
                .addValue("available_disk_size",
                        domain.getAvailableDiskSize())
                .addValue("id", domain.getId())
                .addValue("used_disk_size", domain.getUsedDiskSize());

        getCallsHandler().executeModification("Updatestorage_domain_dynamic", parameterSource);
    }

    @Override
    public void updateConfirmedSize(StorageDomainDynamic domain) {
        MapSqlParameterSource parameterSource = getCustomMapSqlParameterSource()
                .addValue("confirmed_available_disk_size",
                        domain.getConfirmedAvailableDiskSize())
                .addValue("vdo_savings", domain.getVdoSavings())
                .addValue("id", domain.getId());

        getCallsHandler().executeModification("UpdateStorageDomainConfirmedSize", parameterSource);
    }

    @Override
    public void updateExternalStatus(Guid id, ExternalStatus status) {
        MapSqlParameterSource parameterSource = getCustomMapSqlParameterSource().addValue("storage_id", id)
                .addValue("external_status", status);
        getCallsHandler().executeModification("UpdateStorageDomainExternalStatus", parameterSource);
    }

    @Override
    public void remove(Guid id) {
        MapSqlParameterSource parameterSource = getCustomMapSqlParameterSource()
                .addValue("id", id);

        getCallsHandler().executeModification("Deletestorage_domain_dynamic", parameterSource);
    }


    @Override
    public List<StorageDomainDynamic> getAll() {
        MapSqlParameterSource parameterSource = getCustomMapSqlParameterSource();
        return getCallsHandler().executeReadList("GetAllFromstorage_domain_dynamic",
                storageDomainDynamicRowMapper,
                parameterSource);
    }

}
