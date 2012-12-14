package org.ovirt.engine.core.dao;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import org.ovirt.engine.core.common.businessentities.StorageDomainDynamic;
import org.ovirt.engine.core.compat.Guid;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.simple.ParameterizedRowMapper;

public class StorageDomainDynamicDAODbFacadeImpl extends BaseDAODbFacade implements StorageDomainDynamicDAO{

    @Override
    public StorageDomainDynamic get(Guid id) {
        MapSqlParameterSource parameterSource = getCustomMapSqlParameterSource()
                .addValue("id", id);

        ParameterizedRowMapper<StorageDomainDynamic> mapper = new ParameterizedRowMapper<StorageDomainDynamic>() {
            @Override
            public StorageDomainDynamic mapRow(ResultSet rs, int rowNum)
                    throws SQLException {
                StorageDomainDynamic entity = new StorageDomainDynamic();
                entity.setavailable_disk_size((Integer) rs
                        .getObject("available_disk_size"));
                entity.setId(Guid.createGuidFromString(rs.getString("id")));
                entity.setused_disk_size((Integer) rs
                        .getObject("used_disk_size"));
                return entity;
            }
        };

        return getCallsHandler().executeRead("Getstorage_domain_dynamicByid", mapper, parameterSource);
    }


    @Override
    public void save(StorageDomainDynamic domain) {
        MapSqlParameterSource parameterSource = getCustomMapSqlParameterSource()
                .addValue("available_disk_size",
                        domain.getavailable_disk_size())
                .addValue("id", domain.getId())
                .addValue("used_disk_size", domain.getused_disk_size());

        getCallsHandler().executeModification("Insertstorage_domain_dynamic", parameterSource);
    }

    @Override
    public void update(StorageDomainDynamic domain) {
        MapSqlParameterSource parameterSource = getCustomMapSqlParameterSource()
                .addValue("available_disk_size",
                        domain.getavailable_disk_size())
                .addValue("id", domain.getId())
                .addValue("used_disk_size", domain.getused_disk_size());

        getCallsHandler().executeModification("Updatestorage_domain_dynamic", parameterSource);
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

        ParameterizedRowMapper<StorageDomainDynamic> mapper = new ParameterizedRowMapper<StorageDomainDynamic>() {
            @Override
            public StorageDomainDynamic mapRow(ResultSet rs, int rowNum)
                    throws SQLException {
                StorageDomainDynamic entity = new StorageDomainDynamic();
                entity.setavailable_disk_size((Integer) rs
                        .getObject("available_disk_size"));
                entity.setId(Guid.createGuidFromString(rs.getString("id")));
                entity.setused_disk_size((Integer) rs
                        .getObject("used_disk_size"));
                return entity;
            }
        };

        return getCallsHandler().executeReadList("GetAllFromstorage_domain_dynamic", mapper,
                parameterSource);
    }

}
