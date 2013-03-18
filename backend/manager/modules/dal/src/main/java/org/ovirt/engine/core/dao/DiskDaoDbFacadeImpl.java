package org.ovirt.engine.core.dao;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import org.ovirt.engine.core.common.businessentities.Disk;
import org.ovirt.engine.core.common.businessentities.Disk.DiskStorageType;
import org.ovirt.engine.core.common.businessentities.LunDisk;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.DiskImageDAODbFacadeImpl.DiskImageRowMapper;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.simple.SimpleJdbcTemplate;

public class DiskDaoDbFacadeImpl extends BaseDAODbFacade implements DiskDao {

    @Override
    public Disk get(Guid id) {
        return get(id, null, false);
    }

    @Override
    public Disk get(Guid id, Guid userID, boolean isFiltered) {
        return getCallsHandler().executeRead("GetDiskByDiskId", DiskRowMapper.instance, getCustomMapSqlParameterSource()
                .addValue("disk_id", id).addValue("user_id", userID).addValue("is_filtered", isFiltered));
    }

    @Override
    public List<Disk> getAllForVm(Guid id) {
        return getAllForVm(id, null, false);
    }

    @Override
    public List<Disk> getAll() {
        return getAll(null, false);
    }

    @Override
    public List<Disk> getAllForVm(Guid id, Guid userID, boolean isFiltered) {
        MapSqlParameterSource parameterSource = getCustomMapSqlParameterSource()
                .addValue("vm_guid", id).addValue("user_id", userID).addValue("is_filtered", isFiltered);
        return getCallsHandler().executeReadList("GetDisksVmGuid", DiskRowMapper.instance, parameterSource);
    }

    @Override
    public List<Disk> getAll(Guid userID, boolean isFiltered) {
        MapSqlParameterSource parameterSource = getCustomMapSqlParameterSource()
                .addValue("user_id", userID).addValue("is_filtered", isFiltered);
        return getCallsHandler().executeReadList("GetAllFromDisks", DiskRowMapper.instance, parameterSource);
    }

    @Override
    public List<Disk> getAllAttachableDisksByPoolId(Guid poolId, Guid vmId, Guid userId, boolean isFiltered) {
        MapSqlParameterSource parameterSource = getCustomMapSqlParameterSource()
                .addValue("storage_pool_id", poolId)
                .addValue("vm_id", vmId)
                .addValue("user_id", userId)
                .addValue("is_filtered", isFiltered);

        return getCallsHandler().executeReadList("GetAllAttachableDisksByPoolId",
                DiskRowMapper.instance,
                parameterSource);

    }

    @Override
    public List<Disk> getAllWithQuery(String query) {
        return new SimpleJdbcTemplate(jdbcTemplate).query(query, DiskRowMapper.instance);
    }

    private static class DiskRowMapper implements RowMapper<Disk> {

        public static DiskRowMapper instance = new DiskRowMapper();

        private DiskRowMapper() {
        }

        @Override
        public Disk mapRow(ResultSet rs, int rowNum) throws SQLException {
            Disk disk = null;
            DiskStorageType diskStorageType = Disk.DiskStorageType.forValue(rs.getInt("disk_storage_type"));

            switch (diskStorageType) {
            case IMAGE:
                disk = DiskImageRowMapper.instance.mapRow(rs, rowNum);
                break;

            case LUN:
                disk = LunDiskRowMapper.instance.mapRow(rs, rowNum);
                break;
            }

            return disk;
        }
    }

    private static class LunDiskRowMapper extends AbstractDiskRowMapper<LunDisk> {

        public static LunDiskRowMapper instance = new LunDiskRowMapper();

        private LunDiskRowMapper() {
        }

        @Override
        public LunDisk mapRow(ResultSet rs, int rowNum) throws SQLException {
            LunDisk disk = super.mapRow(rs, rowNum);
            disk.setLun(LunDAODbFacadeImpl.MAPPER.mapRow(rs, rowNum));
            return disk;
        }

        @Override
        protected LunDisk createDiskEntity() {
            return new LunDisk();
        }
    }
}
