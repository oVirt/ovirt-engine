package org.ovirt.engine.core.dao.gluster;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import javax.inject.Named;
import javax.inject.Singleton;

import org.apache.commons.lang.StringUtils;
import org.ovirt.engine.core.common.businessentities.gluster.BrickDetails;
import org.ovirt.engine.core.common.businessentities.gluster.BrickProperties;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterBrickEntity;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterStatus;
import org.ovirt.engine.core.common.utils.EnumUtils;
import org.ovirt.engine.core.common.utils.SizeConverter;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dal.dbbroker.MapSqlParameterMapper;
import org.ovirt.engine.core.dao.MassOperationsGenericDao;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;

@Named
@Singleton
public class GlusterBrickDaoImpl extends MassOperationsGenericDao<GlusterBrickEntity, Guid> implements GlusterBrickDao {
    private static final RowMapper<GlusterBrickEntity> brickRowMapper = (rs, rowNum) -> {
        GlusterBrickEntity brick = new GlusterBrickEntity();
        brick.setId(getGuidDefaultEmpty(rs, "id"));
        brick.setVolumeId(getGuidDefaultEmpty(rs, "volume_id"));
        brick.setVolumeName(rs.getString("volume_name"));

        Guid serverId = getGuidDefaultEmpty(rs, "server_id");
        brick.setServerId(serverId);
        brick.setServerName(rs.getString("vds_name"));

        brick.setBrickDirectory(rs.getString("brick_dir"));
        brick.setBrickOrder(rs.getInt("brick_order"));
        brick.setStatus(GlusterStatus.valueOf(rs.getString("status")));
        brick.getAsyncTask().setTaskId(getGuid(rs, "task_id"));

        brick.setNetworkId(getGuid(rs, "network_id"));
        brick.setNetworkAddress(rs.getString("interface_address"));
        brick.setUnSyncedEntries(
                (rs.getObject("unsynced_entries") != null) ? rs.getInt("unsynced_entries") : null);
        brick.setUnSyncedEntriesTrend(asIntList((String) rs.getObject("unsynced_entries_history")));
        brick.setIsArbiter(rs.getBoolean("is_arbiter"));
        return brick;
    };

    private static final RowMapper<BrickProperties> brickPropertiesRowMappeer = (rs, rowNum) -> {
        BrickProperties brickProperties = new BrickProperties();
        brickProperties.setTotalSize(rs.getDouble("total_space") / SizeConverter.BYTES_IN_MB);
        brickProperties.setFreeSize(rs.getDouble("free_space") / SizeConverter.BYTES_IN_MB);
        brickProperties.setConfirmedFreeSize(
                (rs.getObject("confirmed_free_space") != null) ?
                        rs.getDouble("confirmed_free_space") / SizeConverter.BYTES_IN_MB : null);
        brickProperties.setConfirmedTotalSize(
                (rs.getObject("confirmed_total_space") != null) ?
                        rs.getDouble("confirmed_total_space") / SizeConverter.BYTES_IN_MB : null);
        brickProperties.setVdoSavings(rs.getInt("vdo_savings"));
        return brickProperties;
    };

    public GlusterBrickDaoImpl() {
        super("GlusterBrick");
        setProcedureNameForGet("GetGlusterBrickById");
        setProcedureNameForRemove("DeleteGlusterVolumeBrick");
    }

    @Override
    public void save(GlusterBrickEntity brick) {
        getCallsHandler().executeModification("InsertGlusterVolumeBrick", createBrickParams(brick));
    }

    @Override
    public void removeBrick(Guid brickId) {
        getCallsHandler().executeModification("DeleteGlusterVolumeBrick",
                getCustomMapSqlParameterSource().addValue("id", brickId));
    }

    @Override
    public void removeAll(Collection<Guid> ids) {
        getCallsHandler().executeModification("DeleteGlusterVolumeBricks",
                getCustomMapSqlParameterSource().addValue("ids", StringUtils.join(ids, ',')));
    }

    @Override
    public void replaceBrick(GlusterBrickEntity oldBrick, GlusterBrickEntity newBrick) {
        getCallsHandler().executeModification("UpdateGlusterVolumeBrick",
                getCustomMapSqlParameterSource().addValue("id", oldBrick.getId())
                        .addValue("new_id", newBrick.getId())
                        .addValue("new_server_id", newBrick.getServerId())
                        .addValue("new_brick_dir", newBrick.getBrickDirectory())
                        .addValue("new_status", EnumUtils.nameOrNull(newBrick.getStatus()))
                        .addValue("new_network_id", newBrick.getNetworkId())
                        .addValue("is_arbiter", newBrick.getIsArbiter()));

    }

    @Override
    public void updateBrickStatus(Guid brickId, GlusterStatus status) {
        getCallsHandler().executeModification("UpdateGlusterVolumeBrickStatus",
                getCustomMapSqlParameterSource()
                        .addValue("id", brickId)
                        .addValue("status", EnumUtils.nameOrNull(status)));
    }

    @Override
    public void updateBrickStatuses(List<GlusterBrickEntity> bricks) {
        updateAllInBatch("UpdateGlusterVolumeBrickStatus", bricks, getBatchMapper());
    }

    @Override
    public void updateBrickProperties(List<GlusterBrickEntity> bricks) {
        updateAllInBatch("UpdateGlusterVolumeBrickDetails", bricks, getBrickPropertiesMapper());
    }

    @Override
    public void updateBrickOrder(Guid brickId, int brickOrder) {
        getCallsHandler().executeModification("UpdateGlusterVolumeBrickOrder",
                getCustomMapSqlParameterSource()
                        .addValue("id", brickId)
                        .addValue("brick_order", brickOrder));
    }

    @Override
    public GlusterBrickEntity getById(Guid id) {
        GlusterBrickEntity brick = getCallsHandler().executeRead(
                "GetGlusterBrickById", brickRowMapper,
                createIdParameterMapper(id));
        populateBrickDetails(brick);
        return brick;
    }

    @Override
    public List<GlusterBrickEntity> getBricksOfVolume(Guid volumeId) {
        List<GlusterBrickEntity> bricks = getCallsHandler().executeReadList(
                "GetBricksByGlusterVolumeGuid", brickRowMapper,
                getCustomMapSqlParameterSource().addValue("volume_id", volumeId));
        populateBrickDetails(bricks);
        return bricks;
    }

    private void populateBrickDetails(List<GlusterBrickEntity> bricks) {
        bricks.forEach(this::populateBrickDetails);
    }

    private void populateBrickDetails(GlusterBrickEntity brick) {
        if (brick != null) {
            BrickProperties brickProperties = fetchBrickProperties(brick.getId());
            if (brickProperties != null) {
                BrickDetails brickDetails = new BrickDetails();
                brickDetails.setBrickProperties(brickProperties);
                brick.setBrickDetails(brickDetails);
            }
        }
    }

    private BrickProperties fetchBrickProperties(Guid brickId) {
        return getCallsHandler().executeRead(
                "GetBrickDetailsByID",
                brickPropertiesRowMappeer,
                createBrickIdParams(brickId));
    }

    private MapSqlParameterSource createBrickParams(GlusterBrickEntity brick) {
        return getCustomMapSqlParameterSource().addValue("id", brick.getId())
                .addValue("volume_id", brick.getVolumeId())
                .addValue("server_id", brick.getServerId())
                .addValue("brick_dir", brick.getBrickDirectory())
                .addValue("brick_order", (brick.getBrickOrder() == null) ? Integer.valueOf(0) : brick.getBrickOrder())
                .addValue("status", EnumUtils.nameOrNull(brick.getStatus()))
                .addValue("network_id", brick.getNetworkId())
                .addValue("is_arbiter", brick.getIsArbiter());
    }

    private static List<Integer> asIntList(String str) {
        if (str == null || "".equals(str)) {
            return Collections.emptyList();
        }

        List<Integer> res = new ArrayList<>();
        for (String s : StringUtils.split(str, ",")) {
            try {
                res.add(Integer.parseInt(s));
            } catch (NumberFormatException e) {
                // add nothing if malformed
            }
        }

        return res;
    }

    @Override
    public List<GlusterBrickEntity> getGlusterVolumeBricksByServerId(Guid serverId) {
        List<GlusterBrickEntity> bricks = getCallsHandler().executeReadList(
                "GetGlusterVolumeBricksByServerGuid", brickRowMapper,
                getCustomMapSqlParameterSource().addValue("server_id", serverId));
        populateBrickDetails(bricks);
        return bricks;
    }

    @Override
    public GlusterBrickEntity getBrickByServerIdAndDirectory(Guid serverId, String brickDirectory) {
        return getCallsHandler().executeRead(
                "GetBrickByServerIdAndDirectory", brickRowMapper,
                getCustomMapSqlParameterSource()
                        .addValue("server_id", serverId)
                        .addValue("brick_dir", brickDirectory));
    }

    @Override
    public List<GlusterBrickEntity> getGlusterVolumeBricksByTaskId(Guid taskId) {
        return getCallsHandler().executeReadList("GetBricksByTaskId",
                brickRowMapper,
                getCustomMapSqlParameterSource().addValue("task_id", taskId));
    }

    @Override
    public List<GlusterBrickEntity> getAllByClusterAndNetworkId(Guid clusterId, Guid networkId) {
        return getCallsHandler().executeReadList("GetBricksByClusterIdAndNetworkId",
                brickRowMapper,
                getCustomMapSqlParameterSource()
                        .addValue("cluster_id", clusterId)
                        .addValue("network_id", networkId));
    }

    @Override
    protected MapSqlParameterSource createFullParametersMapper(GlusterBrickEntity brick) {
        return createBrickParams(brick);
    }

    @Override
    protected MapSqlParameterSource createIdParameterMapper(Guid id) {
        return getCustomMapSqlParameterSource().addValue("id", id);
    }

    @Override
    protected RowMapper<GlusterBrickEntity> createEntityRowMapper() {
        return brickRowMapper;
    }

    @Override
    public MapSqlParameterMapper<GlusterBrickEntity> getBatchMapper() {
        return entity -> new MapSqlParameterSource()
                .addValue("volume_id", entity.getVolumeId())
                .addValue("server_id", entity.getServerId())
                .addValue("brick_dir", entity.getBrickDirectory())
                .addValue("status", entity.getStatus().name())
                .addValue("id", entity.getId().toString())
                .addValue("brick_order", entity.getBrickOrder())
                .addValue("network_id", entity.getNetworkId())
                .addValue("task_id",
                        entity.getAsyncTask().getTaskId() != null ? entity.getAsyncTask()
                                .getTaskId()
                                .toString()
                                : "")
                .addValue("unsynced_entries", entity.getUnSyncedEntries())
                .addValue("unsynced_entries_history",
                        StringUtils.join(entity.getUnSyncedEntriesTrend(), ","))
                .addValue("is_arbiter", entity.getIsArbiter());
    }

    public MapSqlParameterMapper<GlusterBrickEntity> getBrickPropertiesMapper() {
        return brick -> createBrickPropertiesParam(brick.getBrickProperties());
    }

    @Override
    public void updateBrickTask(Guid brickId, Guid taskId) {
        getCallsHandler().executeModification("UpdateGlusterVolumeBrickAsyncTask",
                getCustomMapSqlParameterSource().
                        addValue("id", brickId).
                        addValue("task_id", taskId));
    }

    @Override
    public void updateBrickTasksInBatch(Collection<GlusterBrickEntity> bricks) {
        getCallsHandler().executeStoredProcAsBatch("UpdateGlusterVolumeBrickAsyncTask", bricks, getBatchMapper());
    }

    @Override
    public void updateBrickTaskByHostIdBrickDir(Guid serverId, String brickDir, Guid taskId) {
        getCallsHandler().executeModification("UpdateGlusterBrickTaskByServerIdBrickDir",
                getCustomMapSqlParameterSource().
                        addValue("server_id", serverId).
                        addValue("brick_dir", brickDir).
                        addValue("task_id", taskId));

    }

    @Override
    public void updateBrickNetworkId(Guid brickId, Guid networkId) {
        getCallsHandler().executeModification("UpdateGlusterVolumeBrickNetworkId",
                getCustomMapSqlParameterSource().
                        addValue("id", brickId).
                        addValue("network_id", networkId));
    }

    @Override
    public void updateAllBrickTasksByHostIdBrickDirInBatch(Collection<GlusterBrickEntity> bricks) {
        getCallsHandler().executeStoredProcAsBatch("UpdateGlusterBrickTaskByServerIdBrickDir",
                bricks, getBatchMapper());

    }

    @Override
    public void updateBrickProperties(BrickProperties brickProperties) {
        getCallsHandler().executeModification("UpdateGlusterVolumeBrickDetails",
                createBrickPropertiesParam(brickProperties));
    }

    @Override
    public void addBrickProperties(BrickProperties brickProperties) {
        getCallsHandler().executeModification("InsertGlusterVolumeBrickDetails",
                createBrickPropertiesParam(brickProperties));
    }

    @Override
    public void addBrickProperties(List<GlusterBrickEntity> bricks) {
        updateAllInBatch("InsertGlusterVolumeBrickDetails", bricks, getBrickPropertiesMapper());
    }

    private MapSqlParameterSource createBrickPropertiesParam(BrickProperties brickProperties) {
        Double confirmedFreeSize = brickProperties.getConfirmedFreeSize();
        if (confirmedFreeSize != null) {
            confirmedFreeSize = confirmedFreeSize * SizeConverter.BYTES_IN_MB;
        }
        Double confirmedTotalSize = brickProperties.getConfirmedTotalSize();
        if (confirmedTotalSize != null) {
            confirmedTotalSize = confirmedTotalSize * SizeConverter.BYTES_IN_MB;
        }
        return getCustomMapSqlParameterSource()
                .addValue("brick_id", brickProperties.getBrickId())
                .addValue("total_space", brickProperties.getTotalSize() * SizeConverter.BYTES_IN_MB)
                .addValue("used_space",
                        (brickProperties.getTotalSize() - brickProperties.getFreeSize()) * SizeConverter.BYTES_IN_MB)
                .addValue("free_space", brickProperties.getFreeSize() * SizeConverter.BYTES_IN_MB)
                .addValue("confirmed_free_space", confirmedFreeSize)
                .addValue("confirmed_total_space", confirmedTotalSize)
                .addValue("vdo_savings", brickProperties.getVdoSavings());
    }

    private MapSqlParameterSource createBrickIdParams(Guid brickId) {
        return getCustomMapSqlParameterSource().addValue("brick_id", brickId);
    }

    @Override
    public void updateUnSyncedEntries(List<GlusterBrickEntity> bricks) {
        getCallsHandler().executeStoredProcAsBatch("UpdateGlusterVolumeBrickUnSyncedEntries", bricks, getBatchMapper());
    }

}
