package org.ovirt.engine.core.dao;

import java.util.ArrayList;
import java.util.List;

import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VDSType;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.NGuid;
import org.ovirt.engine.core.dal.dbbroker.VdsRowMapper;
import org.springframework.jdbc.core.simple.SimpleJdbcTemplate;

/**
 * <code>VdsDAODbFacadeImpl</code> provides an implementation of {@code VdsDAO} that uses previously written code from
 * {@code DbFacade}.
 */
public class VdsDAODbFacadeImpl extends BaseDAODbFacade implements VdsDAO {

    @Override
    public VDS get(NGuid id) {
        return getCallsHandler().executeRead("GetVdsByVdsId",
                new VdsRowMapper(),
                getCustomMapSqlParameterSource()
                        .addValue("vds_id", id));
    }

    @Override
    public List<VDS> getAllWithName(String name) {
        return getCallsHandler().executeReadList("GetVdsByName",
                new VdsRowMapper(),
                getCustomMapSqlParameterSource()
                        .addValue("vds_name", name));
    }

    @Override
    public List<VDS> getAllForHostname(String hostname) {
        return getCallsHandler().executeReadList("GetVdsByHostName",
                new VdsRowMapper(),
                getCustomMapSqlParameterSource()
                        .addValue("host_name", hostname));
    }

    @Override
    public List<VDS> getAllWithIpAddress(String address) {
        return getCallsHandler().executeReadList("GetVdsByIp",
                new VdsRowMapper(),
                getCustomMapSqlParameterSource()
                        .addValue("ip", address));
    }

    @Override
    public List<VDS> getAllWithUniqueId(String id) {
        return getCallsHandler().executeReadList("GetVdsByUniqueID",
                new VdsRowMapper(),
                getCustomMapSqlParameterSource()
                        .addValue("vds_unique_id", id));
    }

    @Override
    public List<VDS> getAllOfTypes(VDSType[] types) {
        List<VDS> list = new ArrayList<VDS>();
        for (VDSType type : types) {
            list.addAll(getAllOfType(type));
        }
        return list;
    }

    @Override
    public List<VDS> getAllOfType(VDSType type) {
        return getCallsHandler().executeReadList("GetVdsByType",
                new VdsRowMapper(),
                getCustomMapSqlParameterSource()
                        .addValue("vds_type", type));
    }

    @Override
    public List<VDS> getAllForVdsGroupWithoutMigrating(Guid id) {
        return getCallsHandler().executeReadList("GetVdsWithoutMigratingVmsByVdsGroupId",
                new VdsRowMapper(),
                getCustomMapSqlParameterSource()
                        .addValue("vds_group_id", id));
    }

    @Override
    public List<VDS> getAllWithQuery(String query) {
        return new SimpleJdbcTemplate(jdbcTemplate).query(query, new VdsRowMapper());
    }

    @Override
    public List<VDS> getAll() {
        return getCallsHandler().executeReadList("GetAllFromVds",
                new VdsRowMapper(),
                getCustomMapSqlParameterSource());
    }

    @Override
    public List<VDS> getAllForVdsGroup(Guid vdsGroupID) {
        return getCallsHandler().executeReadList("GetVdsByVdsGroupId",
                new VdsRowMapper(),
                getCustomMapSqlParameterSource()
                        .addValue("vds_group_id", vdsGroupID));
    }

}
