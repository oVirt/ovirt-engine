package org.ovirt.engine.core.dao.gluster;

import java.util.Collection;
import java.util.List;

import javax.inject.Named;
import javax.inject.Singleton;

import org.apache.commons.lang.StringUtils;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterSnapshotStatus;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterVolumeSnapshotEntity;
import org.ovirt.engine.core.common.utils.EnumUtils;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dal.dbbroker.MapSqlParameterMapper;
import org.ovirt.engine.core.dao.MassOperationsGenericDao;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;

@Named
@Singleton
public class GlusterVolumeSnapshotDaoImpl extends MassOperationsGenericDao<GlusterVolumeSnapshotEntity, Guid> implements GlusterVolumeSnapshotDao {
    private static final RowMapper<GlusterVolumeSnapshotEntity> snapshotRowMapper = (rs, rowNum) -> {
        GlusterVolumeSnapshotEntity entity = new GlusterVolumeSnapshotEntity();
        entity.setSnapshotId(getGuidDefaultEmpty(rs, "snapshot_id"));
        entity.setClusterId(getGuidDefaultEmpty(rs, "cluster_id"));
        entity.setVolumeId(getGuidDefaultEmpty(rs, "volume_id"));
        entity.setSnapshotName(rs.getString("snapshot_name"));
        entity.setCreatedAt(rs.getTimestamp("_create_date"));
        entity.setDescription(rs.getString("description"));
        entity.setStatus(GlusterSnapshotStatus.from(rs.getString("status")));
        return entity;
    };

    public GlusterVolumeSnapshotDaoImpl() {
        super("GlusterVolumeSnapshot");
        setProcedureNameForGet("GetGlusterVolumeSnapshotById");

    }

    @Override
    public void save(GlusterVolumeSnapshotEntity snapshot) {
        getCallsHandler().executeModification("InsertGlusterVolumeSnapshot", createFullParametersMapper(snapshot));
    }

    @Override
    public void saveAll(List<GlusterVolumeSnapshotEntity> snapshots) {
        snapshots.forEach(this::save);
    }

    @Override
    public GlusterVolumeSnapshotEntity getById(Guid id) {
        return getCallsHandler().executeRead("GetGlusterVolumeSnapshotById",
                snapshotRowMapper,
                createSnapshotIdParams(id));
    }

    @Override
    public GlusterVolumeSnapshotEntity getByName(Guid volumeId, String snapshotName) {
        return getCallsHandler().executeRead("GetGlusterVolumeSnapshotByName",
                snapshotRowMapper,
                getCustomMapSqlParameterSource()
                        .addValue("volume_id", volumeId)
                        .addValue("snapshot_name", snapshotName));
    }

    @Override
    public List<GlusterVolumeSnapshotEntity> getAllByVolumeId(Guid volumeId) {
        return getCallsHandler().executeReadList("GetGlusterVolumeSnapshotsByVolumeId", snapshotRowMapper,
                getCustomMapSqlParameterSource()
                        .addValue("volume_id", volumeId));
    }

    @Override
    public List<GlusterVolumeSnapshotEntity> getAllByClusterId(Guid clusterId) {
        return getCallsHandler().executeReadList("GetGlusterVolumeSnapshotsByClusterId",
                snapshotRowMapper,
                getCustomMapSqlParameterSource().addValue("cluster_Id", clusterId));
    }

    @Override
    public void remove(Guid id) {
        getCallsHandler().executeModification("DeleteGlusterVolumeSnapshotByGuid", createSnapshotIdParams(id));
    }

    @Override
    public void removeAllByVolumeId(Guid volumeId) {
        getCallsHandler().executeModification("DeleteGlusterVolumeSnapshotsByVolumeId",
                getCustomMapSqlParameterSource()
                        .addValue("volume_id", volumeId));
    }

    @Override
    public void removeByName(Guid volumeId, String snapshotName) {
        getCallsHandler().executeModification("DeleteGlusterVolumeSnapshotByName",
                getCustomMapSqlParameterSource()
                        .addValue("volume_id", volumeId)
                        .addValue("snapshot_name", snapshotName));
    }

    @Override
    public void removeAll(Collection<Guid> ids) {
        getCallsHandler().executeModification("DeleteGlusterVolumesSnapshotByIds",
                getCustomMapSqlParameterSource().addValue("snapshot_ids", StringUtils.join(ids, ',')));
    }

    @Override
    public void updateSnapshotStatus(Guid snapshotId, GlusterSnapshotStatus status) {
        getCallsHandler().executeModification("UpdateGlusterVolumeSnapshotStatus",
                createSnapshotIdParams(snapshotId).addValue("status", EnumUtils.nameOrNull(status)));
    }

    @Override
    public void updateSnapshotStatusByName(Guid volumeId, String snapshotName, GlusterSnapshotStatus status) {
        getCallsHandler().executeModification("UpdateGlusterVolumeSnapshotStatusByName",
                getCustomMapSqlParameterSource()
                        .addValue("volume_id", volumeId)
                        .addValue("snapshot_name", snapshotName)
                        .addValue("status", EnumUtils.nameOrNull(status)));
    }

    @Override
    public List<GlusterVolumeSnapshotEntity> getAllWithQuery(String query) {
        return getJdbcTemplate().query(query, snapshotRowMapper);
    }

    @Override
    protected MapSqlParameterSource createFullParametersMapper(GlusterVolumeSnapshotEntity snapshot) {
        return getCustomMapSqlParameterSource()
                .addValue("snapshot_id", snapshot.getSnapshotId())
                .addValue("snapshot_name", snapshot.getSnapshotName())
                .addValue("volume_id", snapshot.getVolumeId())
                .addValue("description", snapshot.getDescription())
                .addValue("status", EnumUtils.nameOrNull(snapshot.getStatus()))
                .addValue("_create_date", snapshot.getCreatedAt());
    }

    private MapSqlParameterSource createSnapshotIdParams(Guid id) {
        return getCustomMapSqlParameterSource().addValue("snapshot_id", id);
    }

    @Override
    protected MapSqlParameterSource createIdParameterMapper(Guid id) {
        return createSnapshotIdParams(id);
    }

    @Override
    protected RowMapper<GlusterVolumeSnapshotEntity> createEntityRowMapper() {
        return snapshotRowMapper;
    }

    @Override
    public void updateAllInBatch(List<GlusterVolumeSnapshotEntity> snapshots) {
        updateAllInBatch("UpdateGlusterVolumeSnapshotStatus", snapshots, getBatchMapper());
    }

    @Override
    public MapSqlParameterMapper<GlusterVolumeSnapshotEntity> getBatchMapper() {
        return entity -> new MapSqlParameterSource()
                .addValue("snapshot_id", entity.getId())
                .addValue("snapshot_name", entity.getSnapshotName())
                .addValue("volume_id", entity.getVolumeId())
                .addValue("description", entity.getDescription())
                .addValue("status", EnumUtils.nameOrNull(entity.getStatus()))
                .addValue("_create_date", entity.getCreatedAt());
    }
}
