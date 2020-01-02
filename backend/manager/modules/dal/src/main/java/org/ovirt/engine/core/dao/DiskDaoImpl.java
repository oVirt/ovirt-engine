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
import org.ovirt.engine.core.dao.DiskImageDaoImpl.ManagedBlockStorageRowMapper;
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
    public Disk get(Guid diskId, Guid userID, boolean isFiltered) {
        return getCallsHandler().executeRead("GetDiskByDiskId", diskRowMapper, getCustomMapSqlParameterSource()
                .addValue("disk_id", diskId).addValue("user_id", userID).addValue("is_filtered", isFiltered));
    }

    @Override
    public List<Disk> getAllForVm(Guid vmId) {
        return getAllForVm(vmId, false);
    }

    @Override
    public List<Disk> getAllForVm(Guid vmId, boolean onlyPluggedDisks) {
        return getAllForVm(vmId, onlyPluggedDisks, null, false);
    }

    @Override
    public List<Disk> getAllForVm(Guid vmId, Guid userID, boolean isFiltered) {
        return getAllForVm(vmId, false, userID, isFiltered);
    }

    @Override
    public List<Disk> getAllForVm(Guid vmId, boolean onlyPluggedDisks, Guid userID, boolean isFiltered) {
        MapSqlParameterSource parameterSource = getCustomMapSqlParameterSource()
                        .addValue("vm_guid", vmId)
                        .addValue("only_plugged", onlyPluggedDisks)
                        .addValue("user_id", userID)
                        .addValue("is_filtered", isFiltered);
        return getCallsHandler().executeReadList("GetDisksVmGuid", diskForVmRowMapper, parameterSource);
    }

    @Override
    public List<Guid> getImagesWithDamagedSnapshotForVm(Guid vmId) {
        return getImagesWithDamagedSnapshotForVm(vmId, null, false);
    }

    @Override
    public List<Guid> getImagesWithDamagedSnapshotForVm(Guid vmId, Guid userID, boolean isFiltered) {
        MapSqlParameterSource parameterSource = getCustomMapSqlParameterSource()
                .addValue("vm_guid", vmId)
                .addValue("user_id", userID)
                .addValue("is_filtered", isFiltered);
        return getCallsHandler().executeReadList("GetImagesWithDamagedSnapshotForVm", createGuidMapper(), parameterSource);
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
                diskBasicViewRowMapper,
                parameterSource);
    }

    @Override
    public Map<Guid, List<Disk>> getAllForVms(Collection<Guid> vmIds) {
        MapSqlParameterSource parameterSource = getCustomMapSqlParameterSource()
                .addValue("vm_guids", createArrayOfUUIDs(vmIds));

        List<Pair<Guid, Disk>> pairs = getCallsHandler().executeReadList(
                "GetDisksVmGuids",
                disksForVmsRowMapper,
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
        return getCallsHandler().executeReadList("GetAllFromDisks", diskRowMapper, parameterSource);
    }

    @Override
    public List<Disk> getAllAttachableDisksByPoolId(Guid poolId, Guid vmId, Guid userId, boolean isFiltered) {
        MapSqlParameterSource parameterSource = getCustomMapSqlParameterSource()
                .addValue("storage_pool_id", poolId)
                .addValue("vm_id", vmId)
                .addValue("user_id", userId)
                .addValue("is_filtered", isFiltered);

        return getCallsHandler().executeReadList("GetAllAttachableDisksByPoolId",
                diskRowMapper,
                parameterSource);

    }

    @Override
    public Disk getVmBootActiveDisk(Guid vmId) {
        return getCallsHandler().executeRead("GetVmBootActiveDisk", diskRowMapper,
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
                diskRowMapper,
                parameterSource);
    }

    @Override
    public List<Disk> getAllFromDisksIncludingSnapshots(Guid userID, boolean isFiltered) {
        MapSqlParameterSource parameterSource = getCustomMapSqlParameterSource()
                .addValue("user_id", userID).addValue("is_filtered", isFiltered);
        return getCallsHandler().executeReadList("GetAllFromDisksIncludingSnapshots", diskRowMapper, parameterSource);
    }

    @Override
    public List<Disk> getAllFromDisksIncludingSnapshotsByDiskId(Guid diskId, Guid userID, boolean isFiltered) {
        MapSqlParameterSource parameterSource = getCustomMapSqlParameterSource()
                .addValue("disk_id", diskId).addValue("user_id", userID).addValue("is_filtered", isFiltered);
        return getCallsHandler().executeReadList("GetDiskAndSnapshotsByDiskId", diskRowMapper, parameterSource);
    }

    @Override
    public List<Disk> getAllWithQuery(String query) {
        return getJdbcTemplate().query(query, diskRowMapper);
    }

    private static final RowMapper<Disk> diskRowMapper = (rs, rowNum) -> {
            Disk disk = null;
            DiskStorageType diskStorageType = DiskStorageType.forValue(rs.getInt("disk_storage_type"));

            switch (diskStorageType) {
            case MANAGED_BLOCK_STORAGE:
                disk = ManagedBlockStorageRowMapper.instance.mapRow(rs, rowNum);
                break;

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
    };

    private static final RowMapper<Disk> diskForVmRowMapper = (rs, rowNum) -> {
        Disk disk = diskRowMapper.mapRow(rs, rowNum);
        disk.setPlugged(rs.getBoolean("is_plugged"));
        disk.setLogicalName(rs.getString("logical_name"));
        return disk;
    };

    private static final RowMapper<Disk> diskBasicViewRowMapper = (rs, rowNum) -> {
        DiskImage disk = new DiskImage();
        disk.setDiskAlias(rs.getString("disk_alias"));
        disk.setSize(rs.getLong("size"));
        disk.setId(getGuid(rs, "disk_id"));

        return disk;
    };

    private static final RowMapper<Pair<Guid, Disk>> disksForVmsRowMapper = (rs, rowNum) -> {
        Disk disk = diskForVmRowMapper.mapRow(rs, rowNum);
        Guid vmId = new Guid(rs.getString("vm_id"));
        return new Pair<>(vmId, disk);
    };

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
