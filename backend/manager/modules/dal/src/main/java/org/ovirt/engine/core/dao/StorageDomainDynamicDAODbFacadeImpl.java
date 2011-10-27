package org.ovirt.engine.core.dao;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import org.ovirt.engine.core.common.businessentities.storage_domain_dynamic;
import org.ovirt.engine.core.compat.Guid;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.simple.ParameterizedRowMapper;

public class StorageDomainDynamicDAODbFacadeImpl extends BaseDAODbFacade implements StorageDomainDynamicDAO{

    @Override
    public storage_domain_dynamic get(Guid id) {
        MapSqlParameterSource parameterSource = getCustomMapSqlParameterSource()
                .addValue("id", id);

        ParameterizedRowMapper<storage_domain_dynamic> mapper = new ParameterizedRowMapper<storage_domain_dynamic>() {
            @Override
            public storage_domain_dynamic mapRow(ResultSet rs, int rowNum)
                    throws SQLException {
                storage_domain_dynamic entity = new storage_domain_dynamic();
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
    public void save(storage_domain_dynamic domain) {
        MapSqlParameterSource parameterSource = getCustomMapSqlParameterSource()
                .addValue("available_disk_size",
                        domain.getavailable_disk_size())
                .addValue("id", domain.getId())
                .addValue("used_disk_size", domain.getused_disk_size());

        getCallsHandler().executeModification("Insertstorage_domain_dynamic", parameterSource);
    }

    @Override
    public void update(storage_domain_dynamic domain) {
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
    public List<storage_domain_dynamic> getAll() {
        MapSqlParameterSource parameterSource = getCustomMapSqlParameterSource();

        ParameterizedRowMapper<storage_domain_dynamic> mapper = new ParameterizedRowMapper<storage_domain_dynamic>() {
            @Override
            public storage_domain_dynamic mapRow(ResultSet rs, int rowNum)
                    throws SQLException {
                storage_domain_dynamic entity = new storage_domain_dynamic();
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
