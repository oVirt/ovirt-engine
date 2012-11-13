package org.ovirt.engine.core.dao;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import org.ovirt.engine.core.common.businessentities.LUNs;
import org.ovirt.engine.core.compat.NGuid;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.simple.ParameterizedRowMapper;

/**
 * <code>LunDAODbFacadeImpl</code> provides a concrete implementation of {@link LunDAO}. The original code was
 * refactored from the {@link org.ovirt.engine.core.dal.dbbroker.DbFacade} class.
 */
public class LunDAODbFacadeImpl extends BaseDAODbFacade implements LunDAO {

    protected static final ParameterizedRowMapper<LUNs> MAPPER = new ParameterizedRowMapper<LUNs>() {
        @Override
        public LUNs mapRow(ResultSet rs, int rowNum) throws SQLException {
            LUNs entity = new LUNs();
            entity.setLUN_id(rs.getString("LUN_id"));
            entity.setphysical_volume_id(rs.getString("physical_volume_id"));
            entity.setvolume_group_id(rs.getString("volume_group_id"));
            entity.setSerial(rs.getString("serial"));
            Integer lunMapping = (Integer) rs.getObject("lun_mapping");
            if (lunMapping != null) {
                entity.setLunMapping(lunMapping);
            }
            entity.setVendorId(rs.getString("vendor_id"));
            entity.setProductId(rs.getString("product_id"));
            entity.setDeviceSize(rs.getInt("device_size"));
            entity.setDiskId(NGuid.createGuidFromString(rs.getString("disk_id")));
            entity.setDiskAlias(rs.getString("disk_alias"));
            entity.setStorageDomainId(NGuid.createGuidFromString(rs.getString("storage_id")));
            entity.setStorageDomainName(rs.getString("storage_name"));
            return entity;
        }
    };

    @Override
    public LUNs get(String id) {
        MapSqlParameterSource parameterSource = getCustomMapSqlParameterSource()
                .addValue("LUN_id", id);

        return getCallsHandler().executeRead("GetLUNByLUNId", MAPPER, parameterSource);
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<LUNs> getAllForStorageServerConnection(String id) {
        MapSqlParameterSource parameterSource = getCustomMapSqlParameterSource()
                .addValue("storage_server_connection", id);

        return getCallsHandler().executeReadList("GetLUNsBystorage_server_connection", MAPPER, parameterSource);
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<LUNs> getAllForVolumeGroup(String id) {
        MapSqlParameterSource parameterSource = getCustomMapSqlParameterSource()
                .addValue("volume_group_id", id);

        return getCallsHandler().executeReadList("GetLUNsByVolumeGroupId", MAPPER, parameterSource);
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<LUNs> getAll() {
        MapSqlParameterSource parameterSource = getCustomMapSqlParameterSource();

        return getCallsHandler().executeReadList("GetAllFromLUNs", MAPPER, parameterSource);
    }

    @Override
    public void updateLUNsVolumeGroupId(String id, String volumeGroupId) {
        MapSqlParameterSource parameterSource = getCustomMapSqlParameterSource().addValue("LUN_id", id)
                .addValue("volume_group_id", volumeGroupId);

        getCallsHandler().executeModification("UpdateLUNsVolumeGroupId", parameterSource);
    }

    @Override
    public void save(LUNs lun) {
        MapSqlParameterSource parameterSource = getCustomMapSqlParameterSource()
                .addValue("LUN_id", lun.getLUN_id())
                .addValue("physical_volume_id", lun.getphysical_volume_id())
                .addValue("volume_group_id", lun.getvolume_group_id())
                .addValue("serial", lun.getSerial())
                .addValue("lun_mapping", lun.getLunMapping())
                .addValue("vendor_id", lun.getVendorId())
                .addValue("product_id", lun.getProductId())
                .addValue("device_size", lun.getDeviceSize());

        getCallsHandler().executeModification("InsertLUNs", parameterSource);
    }

    @Override
    public void remove(String id) {
        MapSqlParameterSource parameterSource = getCustomMapSqlParameterSource()
                .addValue("LUN_id", id);

        getCallsHandler().executeModification("DeleteLUN", parameterSource);
    }
}
