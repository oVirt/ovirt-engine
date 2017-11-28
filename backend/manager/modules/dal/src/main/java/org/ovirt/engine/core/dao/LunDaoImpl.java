package org.ovirt.engine.core.dao;

import java.util.List;

import javax.inject.Named;
import javax.inject.Singleton;

import org.ovirt.engine.core.common.businessentities.storage.LUNs;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;

/**
 * {@code LunDaoImpl} provides a concrete implementation of {@link LunDao}.
 */
@Named
@Singleton
public class LunDaoImpl extends MassOperationsGenericDao<LUNs, String> implements LunDao {

    public LunDaoImpl() {
        super("luns");
        setProcedureNameForGet("GetLUNByLUNId");
        setProcedureNameForGetAll("GetAllFromLUNs");
        setProcedureNameForRemove("DeleteLUN");
    }

    protected static final RowMapper<LUNs> MAPPER = (rs, rowNum) -> {
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
        entity.setDiscardMaxSize((Long) rs.getObject("discard_max_size"));
        return entity;
    };

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
    protected MapSqlParameterSource createIdParameterMapper(String id) {
        return getCustomMapSqlParameterSource().addValue("lun_id", id);
    }

    @Override
    protected RowMapper<LUNs> createEntityRowMapper() {
        return MAPPER;
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
                .addValue("device_size", lun.getDeviceSize())
                .addValue("discard_max_size", lun.getDiscardMaxSize());
    }
}
