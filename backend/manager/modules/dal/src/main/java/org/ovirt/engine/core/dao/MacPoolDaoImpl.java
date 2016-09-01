package org.ovirt.engine.core.dao;

import java.util.List;

import javax.inject.Named;
import javax.inject.Singleton;

import org.ovirt.engine.core.common.businessentities.MacPool;
import org.ovirt.engine.core.common.businessentities.MacRange;
import org.ovirt.engine.core.compat.Guid;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.SingleColumnRowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;

@Named
@Singleton
public class MacPoolDaoImpl extends DefaultGenericDao<MacPool, Guid> implements MacPoolDao {
    public MacPoolDaoImpl() {
        super("MacPool");
    }

    @Override
    public void save(MacPool entity) {
        super.save(entity);

        for (MacRange macRange : entity.getRanges()) {
            macRange.setMacPoolId(entity.getId());
            saveRange(macRange);
        }
    }

    @Override
    public void update(MacPool entity) {
        super.update(entity);

        // If ranges definition is also sent, update them as well.
        if (entity.getRanges() != null && !entity.getRanges().isEmpty()) {
            deleteAllRangesForMacPool(entity.getId());

            for (MacRange macRange : entity.getRanges()) {
                macRange.setMacPoolId(entity.getId());
                saveRange(macRange);
            }
        }
    }

    @Override
    public void remove(Guid guid) {
        deleteAllRangesForMacPool(guid);
        super.remove(guid);
    }

    @Override
    public MacPool getDefaultPool() {
        return getCallsHandler().executeRead("GetDefaultMacPool",
                macPoolRowMapper,
                getCustomMapSqlParameterSource());
    }

    @Override
    public List<String> getAllMacsForMacPool(Guid macPoolId) {
        return getCallsHandler().executeReadList("GetAllMacsByMacPoolId",
                SingleColumnRowMapper.newInstance(String.class),
                createIdParameterMapper(macPoolId));
    }

    @Override
    public MacPool getByClusterId(Guid dataCenterId) {
        MapSqlParameterSource parameterSource = getCustomMapSqlParameterSource()
                .addValue("cluster_id", dataCenterId);

        return getCallsHandler().executeRead("GetMacPoolByClusterId",
                macPoolRowMapper,
                parameterSource);
    }

    @Override
    protected MapSqlParameterSource createFullParametersMapper(MacPool entity) {
        return createIdParameterMapper(entity.getId())
                .addValue("name", entity.getName())
                .addValue("allow_duplicate_mac_addresses", entity.isAllowDuplicateMacAddresses())
                .addValue("description", entity.getDescription());
    }

    @Override
    protected MapSqlParameterSource createIdParameterMapper(Guid id) {
        return getCustomMapSqlParameterSource().addValue("id", id);
    }

    @Override
    protected RowMapper<MacPool> createEntityRowMapper() {
        return macPoolRowMapper;
    }

    private void deleteAllRangesForMacPool(Guid macPoolId) {
        MapSqlParameterSource parameterSource = getCustomMapSqlParameterSource().addValue("id", macPoolId);

        getCallsHandler().executeModification("DeleteMacPoolRangesByMacPoolId", parameterSource);
    }

    private void saveRange(MacRange entity) {
        MapSqlParameterSource parameterSource = getCustomMapSqlParameterSource()
                .addValue("mac_pool_id", entity.getMacPoolId())
                .addValue("from_mac", entity.getMacFrom().toLowerCase())
                .addValue("to_mac", entity.getMacTo().toLowerCase());

        getCallsHandler().executeModification("InsertMacPoolRange", parameterSource);
    }

    private List<MacRange> getMacPoolRangesForPool(Guid id) {
        return getCallsHandler().executeReadList("GetAllMacPoolRangesByMacPoolId",
                macPoolRangeRowMapper,
                createIdParameterMapper(id));
    }

    private static final RowMapper<MacRange> macPoolRangeRowMapper = (rs, rowNum) -> {
        MacRange macRange = new MacRange();
        macRange.setMacPoolId(getGuid(rs, "mac_pool_id"));
        macRange.setMacFrom(rs.getString("from_mac"));
        macRange.setMacTo(rs.getString("to_mac"));

        return macRange;
    };

    private final RowMapper<MacPool> macPoolRowMapper = (rs, rowNum) -> {
        MacPool macPool = new MacPool();
        macPool.setId(getGuid(rs, "id"));
        macPool.setName(rs.getString("name"));
        macPool.setAllowDuplicateMacAddresses(rs.getBoolean("allow_duplicate_mac_addresses"));
        macPool.setDescription(rs.getString("description"));
        macPool.setDefaultPool(rs.getBoolean("default_pool"));
        macPool.setRanges(getMacPoolRangesForPool(macPool.getId()));

        return macPool;
    };
}
