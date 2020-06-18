package org.ovirt.engine.core.dao;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.List;

import javax.inject.Named;
import javax.inject.Singleton;

import org.ovirt.engine.core.common.businessentities.Snapshot;
import org.ovirt.engine.core.common.businessentities.Snapshot.SnapshotStatus;
import org.ovirt.engine.core.common.businessentities.Snapshot.SnapshotType;
import org.ovirt.engine.core.common.utils.EnumUtils;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.utils.SerializationFactory;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.SingleColumnRowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;

@Named
@Singleton
public class SnapshotDaoImpl extends DefaultGenericDao<Snapshot, Guid> implements SnapshotDao {

    private static final RowMapper<Snapshot> ROW_MAPPER = new SnapshotRowMapper();

    private static final RowMapper<Snapshot> NO_CONFIG_ROW_MAPPER =
            new SnapshotRowMapperWithConfigurationAvailable();

    public SnapshotDaoImpl() {
        super("Snapshot");
    }

    @Override
    protected MapSqlParameterSource createIdParameterMapper(Guid id) {
        return getCustomMapSqlParameterSource().addValue("snapshot_id", id);
    }

    @Override
    protected MapSqlParameterSource createFullParametersMapper(Snapshot entity) {
        return createIdParameterMapper(entity.getId())
                .addValue("vm_id", entity.getVmId())
                .addValue("snapshot_type", EnumUtils.nameOrNull(entity.getType()))
                .addValue("status", EnumUtils.nameOrNull(entity.getStatus()))
                .addValue("description", entity.getDescription())
                .addValue("creation_date", entity.getCreationDate())
                .addValue("app_list", entity.getAppList())
                .addValue("vm_configuration", entity.getVmConfiguration())
                .addValue("memory_dump_disk_id", entity.getMemoryDiskId())
                .addValue("memory_metadata_disk_id", entity.getMetadataDiskId())
                .addValue("vm_configuration_broken", entity.isVmConfigurationBroken())
                .addValue("changed_fields", SerializationFactory.getSerializer().serialize(entity.getChangedFields()));
    }

    @Override
    protected RowMapper<Snapshot> createEntityRowMapper() {
        return ROW_MAPPER;
    }

    @Override
    public void updateStatus(Guid id, SnapshotStatus status) {
        MapSqlParameterSource parameterSource = createIdParameterMapper(id)
                .addValue("status", EnumUtils.nameOrNull(status));
        getCallsHandler().executeModification("UpdateSnapshotStatus", parameterSource);
    }

    @Override
    public void updateId(Guid snapshotId, Guid newSnapshotId) {
        MapSqlParameterSource parameterSource = getCustomMapSqlParameterSource()
                .addValue("snapshot_id", snapshotId)
                .addValue("new_snapshot_id", newSnapshotId);
        getCallsHandler().executeModification("UpdateSnapshotId", parameterSource);
    }

    @Override
    public Guid getId(Guid vmId, SnapshotType type) {
        MapSqlParameterSource parameterSource = getCustomMapSqlParameterSource()
                .addValue("vm_id", vmId)
                .addValue("snapshot_type", EnumUtils.nameOrNull(type));

        return getCallsHandler().executeRead("GetSnapshotIdsByVmIdAndType",
                createGuidMapper(),
                parameterSource);
    }

    @Override
    public Guid getId(Guid vmId, SnapshotType type, SnapshotStatus status) {
        MapSqlParameterSource parameterSource = getCustomMapSqlParameterSource()
                .addValue("vm_id", vmId)
                .addValue("snapshot_type", EnumUtils.nameOrNull(type))
                .addValue("status", EnumUtils.nameOrNull(status));

        return getCallsHandler().executeRead("GetSnapshotIdsByVmIdAndTypeAndStatus",
                createGuidMapper(),
                parameterSource);
    }

    @Override
    public Snapshot get(Guid vmId, SnapshotType type, SnapshotStatus status) {
        MapSqlParameterSource parameterSource = getCustomMapSqlParameterSource()
                .addValue("vm_id", vmId)
                .addValue("snapshot_type", EnumUtils.nameOrNull(type))
                .addValue("status", EnumUtils.nameOrNull(status));

        return getCallsHandler().executeRead("GetSnapshotByVmIdAndTypeAndStatus",
                createEntityRowMapper(),
                parameterSource);
    }

    @Override
    public List<Snapshot> getAllWithConfiguration(Guid vmId) {
        return getAll(vmId, null, false, true);
    }

    @Override
    public List<Snapshot> getAll(Guid vmId) {
        return getAll(vmId, null, false);
    }

    @Override
    public List<Snapshot> getAll(Guid vmId, Guid userId, boolean isFiltered) {
        return getAll(vmId, userId, isFiltered, false);
    }

    private List<Snapshot> getAll(Guid vmId, Guid userId, boolean isFiltered, boolean fillConfiguration) {
        MapSqlParameterSource parameterSource = getCustomMapSqlParameterSource().addValue("vm_id", vmId)
                .addValue("user_id", userId)
                .addValue("is_filtered", isFiltered)
                .addValue("fill_configuration", fillConfiguration);

        return getCallsHandler().executeReadList("GetAllFromSnapshotsByVmId", NO_CONFIG_ROW_MAPPER, parameterSource);
    }

    @Override
    public List<Snapshot> getAllByStorageDomain(Guid storageId) {
        MapSqlParameterSource parameterSource = getCustomMapSqlParameterSource()
                .addValue("storage_id", storageId);

        return getCallsHandler().executeReadList("GetAllSnapshotsByStorageDomainId",
                createEntityRowMapper(),
                parameterSource);
    }

    @Override
    public boolean exists(Guid vmId, SnapshotType type) {
        MapSqlParameterSource parameterSource = getCustomMapSqlParameterSource()
                .addValue("vm_id", vmId)
                .addValue("snapshot_type", EnumUtils.nameOrNull(type));

        return getCallsHandler().executeRead("CheckIfSnapshotExistsByVmIdAndType",
                SingleColumnRowMapper.newInstance(Boolean.class),
                parameterSource);
    }

    @Override
    public boolean exists(Guid vmId, SnapshotStatus status) {
        MapSqlParameterSource parameterSource = getCustomMapSqlParameterSource()
                .addValue("vm_id", vmId)
                .addValue("status", EnumUtils.nameOrNull(status));

        return getCallsHandler().executeRead("CheckIfSnapshotExistsByVmIdAndStatus",
                SingleColumnRowMapper.newInstance(Boolean.class),
                parameterSource);
    }

    @Override
    public boolean exists(Guid vmId, Guid snapshotId) {
        MapSqlParameterSource parameterSource = getCustomMapSqlParameterSource()
                .addValue("vm_id", vmId)
                .addValue("snapshot_id", snapshotId);

        return getCallsHandler().executeRead("CheckIfSnapshotExistsByVmIdAndSnapshotId",
                SingleColumnRowMapper.newInstance(Boolean.class),
                parameterSource);
    }

    private static class SnapshotRowMapper implements RowMapper<Snapshot> {

        @Override
        public Snapshot mapRow(ResultSet rs, int rowNum) throws SQLException {
            Snapshot snapshot = createInitialSnapshotEntity(rs);

            snapshot.setId(getGuidDefaultEmpty(rs, "snapshot_id"));
            snapshot.setVmId(getGuidDefaultEmpty(rs, "vm_id"));
            snapshot.setType(SnapshotType.valueOf(rs.getString("snapshot_type")));
            snapshot.setStatus(SnapshotStatus.valueOf(rs.getString("status")));
            snapshot.setDescription(rs.getString("description"));
            snapshot.setCreationDate(new Date(rs.getTimestamp("creation_date").getTime()));
            snapshot.setAppList(rs.getString("app_list"));
            snapshot.setVmConfiguration(rs.getString("vm_configuration"));
            snapshot.setMemoryDiskId(getGuid(rs, "memory_dump_disk_id"));
            snapshot.setMetadataDiskId(getGuid(rs, "memory_metadata_disk_id"));
            snapshot.setVmConfigurationBroken(rs.getBoolean("vm_configuration_broken"));
            snapshot.setChangedFields(SerializationFactory.getDeserializer().deserialize(rs.getString("changed_fields"), HashSet.class));

            return snapshot;
        }

        protected Snapshot createInitialSnapshotEntity(ResultSet rs) throws SQLException {
            return new Snapshot();
        }
    }

    /**
     * Mapper that will also map the {@link Snapshot#isVmConfigurationAvailable()} field.
     */
    private static class SnapshotRowMapperWithConfigurationAvailable extends SnapshotRowMapper {

        @Override
        protected Snapshot createInitialSnapshotEntity(ResultSet rs) throws SQLException {
            return new Snapshot(rs.getBoolean("vm_configuration_available"));
        }
    }

    @Override
    public Snapshot get(Guid id) {
        return get(id, null, false);
    }

    @Override
    public Snapshot get(Guid id, Guid userId, boolean isFiltered) {
        MapSqlParameterSource parameterSource = createIdParameterMapper(id)
                .addValue("user_id", userId)
                .addValue("is_filtered", isFiltered);
        return getCallsHandler().executeRead(getProcedureNameForGet(), createEntityRowMapper(), parameterSource);
    }

    @Override
    public Snapshot get(Guid vmId, SnapshotType type) {
        return get(vmId, type, null, false);
    }

    @Override
    public Snapshot get(Guid vmId, SnapshotType type, Guid userId, boolean isFiltered) {
        MapSqlParameterSource parameterSource = getCustomMapSqlParameterSource()
                .addValue("vm_id", vmId)
                .addValue("snapshot_type", EnumUtils.nameOrNull(type))
                .addValue("user_id", userId)
                .addValue("is_filtered", isFiltered);

        return getCallsHandler().executeRead("GetSnapshotByVmIdAndType",
                createEntityRowMapper(),
                parameterSource);
    }

    @Override
    public Snapshot get(Guid vmId, SnapshotStatus status) {
        MapSqlParameterSource parameterSource = getCustomMapSqlParameterSource()
                .addValue("vm_id", vmId)
                .addValue("status", EnumUtils.nameOrNull(status));

        return getCallsHandler().executeRead("GetSnapshotByVmIdAndStatus",
                createEntityRowMapper(),
                parameterSource);
    }

    @Override
    public int getNumOfSnapshotsByDisks(Snapshot snapshot) {
        MapSqlParameterSource parameterSource = getCustomMapSqlParameterSource()
                .addValue("memory_disk_ids",
                        createArrayOfUUIDs(Arrays.asList(snapshot.getMemoryDiskId(), snapshot.getMetadataDiskId())));

        return getCallsHandler().executeRead("GetNumOfSnapshotsByMemoryVolume",
                SingleColumnRowMapper.newInstance(Integer.class),
                parameterSource);
    }

    @Override
    public void removeMemoryFromActiveSnapshot(Guid vmId) {
        MapSqlParameterSource parameterSource = getCustomMapSqlParameterSource()
                .addValue("vm_id", vmId)
                .addValue("snapshot_type", SnapshotType.ACTIVE.name());

        getCallsHandler().executeModification("RemoveMemoryFromSnapshotByVmIdAndType",
                parameterSource);
    }

    @Override
    public void removeMemoryFromSnapshot(Guid snapshotId) {
        MapSqlParameterSource parameterSource = getCustomMapSqlParameterSource()
                .addValue("snapshot_id", snapshotId);

        getCallsHandler().executeModification("RemoveMemoryFromSnapshotBySnapshotId",
                parameterSource);
    }

    @Override
    public void updateHibernationMemory(Guid vmId, Guid memoryDumpDiskId, Guid memoryMetadataDiskId) {
        MapSqlParameterSource parameterSource = getCustomMapSqlParameterSource()
                .addValue("memory_dump_disk_id", memoryDumpDiskId)
                .addValue("memory_metadata_disk_id", memoryMetadataDiskId)
                .addValue("vm_id", vmId)
                .addValue("snapshot_type", SnapshotType.ACTIVE.name());

        getCallsHandler().executeModification("UpdateMemory",
                parameterSource);
    }

    @Override
    public List<Snapshot> getSnapshotsByMemoryDiskId(Guid memoryDiskId) {
        MapSqlParameterSource parameterSource = getCustomMapSqlParameterSource()
                .addValue("memory_disk_id", memoryDiskId);

        return getCallsHandler().executeReadList("GetAllSnapshotsByMemoryDisk", createEntityRowMapper(), parameterSource);
    }
}
