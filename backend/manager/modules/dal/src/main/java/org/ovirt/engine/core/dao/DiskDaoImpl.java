package org.ovirt.engine.core.dao;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Named;
import javax.inject.Singleton;

import org.ovirt.engine.core.common.businessentities.storage.Disk;
import org.ovirt.engine.core.common.businessentities.storage.DiskImage;
import org.ovirt.engine.core.common.businessentities.storage.DiskStorageType;
import org.ovirt.engine.core.common.businessentities.storage.LunDisk;
import org.ovirt.engine.core.common.utils.Pair;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.DiskImageDaoImpl.CinderDiskRowMapper;
import org.ovirt.engine.core.dao.DiskImageDaoImpl.DiskImageRowMapper;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;

@Named
@Singleton
public class DiskDaoImpl extends BaseDao implements DiskDao {

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
        return getAllForVm(id, false);
    }

    @Override
    public List<Disk> getAllForVm(Guid id, boolean onlyPluggedDisks) {
        return getAllForVm(id, onlyPluggedDisks, null, false);
    }

    @Override
    public List<Disk> getAllForVm(Guid id, Guid userID, boolean isFiltered) {
        return getAllForVm(id, false, userID, isFiltered);
    }

    @Override
    public List<Disk> getAllForVm(Guid id, boolean onlyPluggedDisks, Guid userID, boolean isFiltered) {
        MapSqlParameterSource parameterSource = getCustomMapSqlParameterSource()
                        .addValue("vm_guid", id)
                        .addValue("only_plugged", onlyPluggedDisks)
                        .addValue("user_id", userID)
                        .addValue("is_filtered", isFiltered);
        return getCallsHandler().executeReadList("GetDisksVmGuid", DiskForVmRowMapper.instance, parameterSource);
    }

    @Override
    public List<Disk> getAllForVmPartialData(Guid vmId,
            boolean onlyPluggedDisks,
            Guid userID,
            boolean isFiltered) {
        MapSqlParameterSource parameterSource = getCustomMapSqlParameterSource()
                .addValue("vm_guid", vmId)
                .addValue("only_plugged", onlyPluggedDisks)
                .addValue("user_id", userID)
                .addValue("is_filtered", isFiltered);
        return getCallsHandler().executeReadList("GetDisksVmGuidBasicView",
                DiskBasicViewRowMapper.instance,
                parameterSource);
    }

    @Override
    public Map<Guid, List<Disk>> getAllForVms(Collection<Guid> vmIds) {
        MapSqlParameterSource parameterSource = getCustomMapSqlParameterSource()
                .addValue("vm_guids", createArrayOfUUIDs(vmIds));

        List<Pair<Guid, Disk>> pairs = getCallsHandler().executeReadList(
                "GetDisksVmGuids",
                DisksForVmsRowMapper.instance,
                parameterSource);

        Map<Guid, List<Disk>> resultMap = new HashMap<>();
        for (Pair<Guid, Disk> pair : pairs) {
            resultMap.putIfAbsent(pair.getFirst(), new ArrayList<>());
            resultMap.get(pair.getFirst()).add(pair.getSecond());
        }

        return resultMap;
    }

    @Override
    public List<Disk> getAll() {
        return getAll(null, false);
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
    public Disk getVmBootActiveDisk(Guid vmId) {
        return getCallsHandler().executeRead("GetVmBootActiveDisk", DiskRowMapper.instance,
                getCustomMapSqlParameterSource().addValue("vm_guid", vmId));
    }

    @Override
    public List<Disk> getAllFromDisksByDiskStorageType(DiskStorageType diskStorageType, Guid userID, boolean isFiltered) {
        MapSqlParameterSource parameterSource =
                getCustomMapSqlParameterSource()
                        .addValue("disk_storage_type", diskStorageType.getValue())
                        .addValue("user_id", userID)
                        .addValue("is_filtered", isFiltered);
        return getCallsHandler().executeReadList("GetAllFromDisksByDiskStorageType",
                DiskRowMapper.instance,
                parameterSource);
    }

    @Override
    public List<Disk> getAllWithQuery(String query) {
        return getJdbcTemplate().query(query, DiskRowMapper.instance);
    }

    private static class DiskRowMapper implements RowMapper<Disk> {

        public static DiskRowMapper instance = new DiskRowMapper();

        private DiskRowMapper() {
        }

        @Override
        public Disk mapRow(ResultSet rs, int rowNum) throws SQLException {
            Disk disk = null;
            DiskStorageType diskStorageType = DiskStorageType.forValue(rs.getInt("disk_storage_type"));

            switch (diskStorageType) {
            case IMAGE:
                disk = DiskImageRowMapper.instance.mapRow(rs, rowNum);
                break;

            case LUN:
                disk = LunDiskRowMapper.instance.mapRow(rs, rowNum);
                break;

            case CINDER:
                disk = CinderDiskRowMapper.instance.mapRow(rs, rowNum);
                break;
            }

            return disk;
        }
    }

    private static class DiskForVmRowMapper implements RowMapper<Disk> {

        public static final DiskForVmRowMapper instance = new DiskForVmRowMapper();

        private DiskForVmRowMapper() {
        }

        @Override
        public Disk mapRow(ResultSet rs, int rowNum) throws SQLException {
            Disk disk = DiskRowMapper.instance.mapRow(rs, rowNum);
            disk.setPlugged(rs.getBoolean("is_plugged"));
            disk.setReadOnly(rs.getBoolean("is_readonly"));
            disk.setLogicalName(rs.getString("logical_name"));
            if (disk.getDiskStorageType() == DiskStorageType.LUN) {
                ((LunDisk) disk).setUsingScsiReservation(rs.getBoolean("is_using_scsi_reservation"));
            }
            return disk;
        }
    }

    private static class DiskBasicViewRowMapper implements RowMapper<Disk> {

        public static DiskBasicViewRowMapper instance = new DiskBasicViewRowMapper();

        private DiskBasicViewRowMapper() {
        }

        @Override
        public Disk mapRow(ResultSet rs, int rowNum) throws SQLException {
            DiskImage disk = new DiskImage();
            disk.setDiskAlias(rs.getString("disk_alias"));
            disk.setSize(rs.getLong("size"));
            disk.setId(getGuid(rs, "disk_id"));

            return disk;
        }
    }

    private static class DisksForVmsRowMapper implements RowMapper<Pair<Guid, Disk>> {

        static final DisksForVmsRowMapper instance = new DisksForVmsRowMapper();

        private DisksForVmsRowMapper() {
        }

        @Override
        public Pair<Guid, Disk> mapRow(ResultSet rs, int rowNum) throws SQLException {
            Disk disk = DiskForVmRowMapper.instance.mapRow(rs, rowNum);
            Guid vmId = new Guid(rs.getString("vm_id"));
            return new Pair<>(vmId, disk);
        }
    }

    private static class LunDiskRowMapper extends AbstractDiskRowMapper<LunDisk> {

        public static LunDiskRowMapper instance = new LunDiskRowMapper();

        private LunDiskRowMapper() {
        }

        @Override
        public LunDisk mapRow(ResultSet rs, int rowNum) throws SQLException {
            LunDisk disk = super.mapRow(rs, rowNum);
            disk.setLun(LunDaoImpl.MAPPER.mapRow(rs, rowNum));
            return disk;
        }

        @Override
        protected LunDisk createDiskEntity() {
            return new LunDisk();
        }
    }
}
