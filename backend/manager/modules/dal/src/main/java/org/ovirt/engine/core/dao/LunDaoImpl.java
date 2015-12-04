package org.ovirt.engine.core.dao;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import javax.inject.Named;
import javax.inject.Singleton;

import org.ovirt.engine.core.common.businessentities.storage.LUNs;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;

/**
 * <code>LunDaoImpl</code> provides a concrete implementation of {@link LunDao}. The original code was
 * refactored from the {@link org.ovirt.engine.core.dal.dbbroker.DbFacade} class.
 */
@Named
@Singleton
public class LunDaoImpl extends MassOperationsGenericDao<LUNs, String> implements LunDao {

    public LunDaoImpl() {
        super("luns");
        setProcedureNameForGet("GetLUNByLUNId");
        setProcedureNameForGetAll("GetAllFromLUNs");
    }

    protected static final RowMapper<LUNs> MAPPER = new RowMapper<LUNs>() {
        @Override
        public LUNs mapRow(ResultSet rs, int rowNum) throws SQLException {
            LUNs entity = new LUNs();
            entity.setLUNId(rs.getString("lun_id"));
            entity.setPhysicalVolumeId(rs.getString("physical_volume_id"));
            entity.setVolumeGroupId(rs.getString("volume_group_id"));
            entity.setSerial(rs.getString("serial"));
            Integer lunMapping = (Integer) rs.getObject("lun_mapping");
            if (lunMapping != null) {
                entity.setLunMapping(lunMapping);
            }
            entity.setVendorId(rs.getString("vendor_id"));
            entity.setProductId(rs.getString("product_id"));
            entity.setDeviceSize(rs.getInt("device_size"));
            entity.setDiskId(getGuid(rs, "disk_id"));
            entity.setDiskAlias(rs.getString("disk_alias"));
            entity.setStorageDomainId(getGuid(rs, "storage_id"));
            entity.setStorageDomainName(rs.getString("storage_name"));
            return entity;
        }
    };

    @Override
    public LUNs get(String id) {
        MapSqlParameterSource parameterSource = getCustomMapSqlParameterSource()
                .addValue("lun_id", id);

        return getCallsHandler().executeRead("GetLUNByLUNId", MAPPER, parameterSource);
    }

    @Override
    public List<LUNs> getAllForStorageServerConnection(String id) {
        MapSqlParameterSource parameterSource = getCustomMapSqlParameterSource()
                .addValue("storage_server_connection", id);

        return getCallsHandler().executeReadList("GetLUNsBystorage_server_connection", MAPPER, parameterSource);
    }

    @Override
    public List<LUNs> getAllForVolumeGroup(String id) {
        MapSqlParameterSource parameterSource = getCustomMapSqlParameterSource()
                .addValue("volume_group_id", id);

        return getCallsHandler().executeReadList("GetLUNsByVolumeGroupId", MAPPER, parameterSource);
    }

    @Override
    public List<LUNs> getAll() {
        MapSqlParameterSource parameterSource = getCustomMapSqlParameterSource();

        return getCallsHandler().executeReadList("GetAllFromLUNs", MAPPER, parameterSource);
    }

    @Override
    protected MapSqlParameterSource createIdParameterMapper(String id) {
        return getCustomMapSqlParameterSource().addValue("lun_id", id);
    }

    @Override
    protected RowMapper<LUNs> createEntityRowMapper() {
        return MAPPER;
    }

    @Override
    public void save(LUNs lun) {
        MapSqlParameterSource parameterSource = getCustomMapSqlParameterSource()
                .addValue("lun_id", lun.getLUNId())
                .addValue("physical_volume_id", lun.getPhysicalVolumeId())
                .addValue("volume_group_id", lun.getVolumeGroupId())
                .addValue("serial", lun.getSerial())
                .addValue("lun_mapping", lun.getLunMapping())
                .addValue("vendor_id", lun.getVendorId())
                .addValue("product_id", lun.getProductId())
                .addValue("device_size", lun.getDeviceSize());

        getCallsHandler().executeModification("InsertLUNs", parameterSource);
    }

    @Override
    public void update(LUNs lun) {
        MapSqlParameterSource parameterSource = getCustomMapSqlParameterSource()
                .addValue("lun_id", lun.getLUNId())
                .addValue("physical_volume_id", lun.getPhysicalVolumeId())
                .addValue("volume_group_id", lun.getVolumeGroupId())
                .addValue("serial", lun.getSerial())
                .addValue("lun_mapping", lun.getLunMapping())
                .addValue("vendor_id", lun.getVendorId())
                .addValue("product_id", lun.getProductId())
                .addValue("device_size", lun.getDeviceSize());

        getCallsHandler().executeModification("UpdateLUNs", parameterSource);
    }

    @Override
    public void remove(String id) {
        MapSqlParameterSource parameterSource = getCustomMapSqlParameterSource()
                .addValue("lun_id", id);

        getCallsHandler().executeModification("DeleteLUN", parameterSource);
    }

    @Override
    protected MapSqlParameterSource createFullParametersMapper(LUNs lun) {
        return createIdParameterMapper(lun.getId())
                .addValue("physical_volume_id", lun.getPhysicalVolumeId())
                .addValue("volume_group_id", lun.getVolumeGroupId())
                .addValue("serial", lun.getSerial())
                .addValue("lun_mapping", lun.getLunMapping())
                .addValue("vendor_id", lun.getVendorId())
                .addValue("product_id", lun.getProductId())
                .addValue("device_size", lun.getDeviceSize());
    }
}
