package org.ovirt.engine.core.dao.gluster;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterBrickEntity;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterStatus;
import org.ovirt.engine.core.common.utils.EnumUtils;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dal.dbbroker.MapSqlParameterMapper;
import org.ovirt.engine.core.dao.MassOperationsGenericDaoDbFacade;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;

public class GlusterBrickDaoDbFacadeImpl extends MassOperationsGenericDaoDbFacade<GlusterBrickEntity, Guid> implements GlusterBrickDao {
    // The brick row mapper can't be static as its' type (GlusterBrickRowMapper) is a non-static inner class
    // There will still be a single instance of it, as the DAO itself will be instantiated only once
    private final RowMapper<GlusterBrickEntity> brickRowMapper = new GlusterBrickRowMapper();

    public GlusterBrickDaoDbFacadeImpl() {
        super("GlusterBrick");
        setProcedureNameForGet("GetGlusterBrickById");
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
                        .addValue("new_status", EnumUtils.nameOrNull(newBrick.getStatus())));
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
        for(GlusterBrickEntity brick : bricks) {
            updateBrickStatus(brick.getId(), brick.getStatus());
        }
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
        return getCallsHandler().executeRead(
                "GetGlusterBrickById", brickRowMapper,
                createIdParameterMapper(id));
    }

    @Override
    public List<GlusterBrickEntity> getBricksOfVolume(Guid volumeId) {
        return getCallsHandler().executeReadList(
                "GetBricksByGlusterVolumeGuid", brickRowMapper,
                getCustomMapSqlParameterSource().addValue("volume_id", volumeId));
    }


    private MapSqlParameterSource createBrickParams(GlusterBrickEntity brick) {
        return getCustomMapSqlParameterSource().addValue("id", brick.getId())
                .addValue("volume_id", brick.getVolumeId())
                .addValue("server_id", brick.getServerId())
                .addValue("brick_dir", brick.getBrickDirectory())
                .addValue("brick_order", (brick.getBrickOrder() == null) ? Integer.valueOf(0) : brick.getBrickOrder())
                .addValue("status", EnumUtils.nameOrNull(brick.getStatus()));
    }

    /**
     * This is not a static class since it invokes a non-static method (getHostNameOfServer) of the parent class.
     */
    private final class GlusterBrickRowMapper implements RowMapper<GlusterBrickEntity> {
        @Override
        public GlusterBrickEntity mapRow(ResultSet rs, int rowNum)
                throws SQLException {
            GlusterBrickEntity brick = new GlusterBrickEntity();
            brick.setId(getGuidDefaultEmpty(rs, "id"));
            brick.setVolumeId(getGuidDefaultEmpty(rs, "volume_id"));

            Guid serverId = getGuidDefaultEmpty(rs, "server_id");
            brick.setServerId(serverId);
            // Update the brick with server name. This is useful as the brick is typically represented in the form
            // serverName:brickDirectory though the database table (gluster_volume_bricks) stores just the server id
            brick.setServerName(getHostNameOfServer(serverId));

            brick.setBrickDirectory(rs.getString("brick_dir"));
            brick.setBrickOrder(rs.getInt("brick_order"));
            brick.setStatus(GlusterStatus.valueOf(rs.getString("status")));
            return brick;
        }

        private String getHostNameOfServer(Guid serverId) {
            return jdbcTemplate.queryForObject("select host_name from vds_static where vds_id = ?",
                    String.class,
                    serverId.getUuid());
        }
    }

    @Override
    public List<GlusterBrickEntity> getGlusterVolumeBricksByServerId(Guid serverId) {
        return getCallsHandler().executeReadList(
                "GetGlusterVolumeBricksByServerGuid", brickRowMapper,
                getCustomMapSqlParameterSource().addValue("server_id", serverId));
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
        // TODO: Implement this
        throw new RuntimeException("Unsupported operation");
    }
}
