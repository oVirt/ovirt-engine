package org.ovirt.engine.core.dao.gluster;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;

import org.ovirt.engine.core.common.businessentities.gluster.AccessProtocol;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterBrickEntity;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterBrickStatus;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterVolumeEntity;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterVolumeOption;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterVolumeStatus;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterVolumeType;
import org.ovirt.engine.core.common.businessentities.gluster.TransportType;
import org.ovirt.engine.core.common.utils.EnumUtils;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.BaseDAODbFacade;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.simple.ParameterizedRowMapper;
import org.springframework.jdbc.core.simple.SimpleJdbcTemplate;

/**
 * Implementation of the DB Facade for Gluster Volumes.
 */
public class GlusterVolumeDaoDbFacadeImpl extends BaseDAODbFacade implements
        GlusterVolumeDao {

    private static final ParameterizedRowMapper<GlusterVolumeEntity> volumeRowMapper = new GlusterVolumeRowMapper();
    private static final ParameterizedRowMapper<GlusterVolumeOption> optionRowMapper = new VolumeOptionRowMapper();
    private static final ParameterizedRowMapper<AccessProtocol> accessProtocolRowMapper = new AccessProtocolRowMapper();
    // The brick row mapper can't be static as its' type (GlusterBrickRowMapper) is a non-static inner class
    // There will still be a single instance of it, as the DAO itself will be instantiated only once
    private final ParameterizedRowMapper<GlusterBrickEntity> brickRowMapper = new GlusterBrickRowMapper();

    @Override
    public void save(GlusterVolumeEntity volume) {
        insertVolumeEntity(volume);
        insertVolumeBricks(volume);
        insertVolumeOptions(volume);
        insertVolumeAccessProtocols(volume);
    }

    @Override
    public GlusterVolumeEntity getById(Guid id) {
        GlusterVolumeEntity volume = getCallsHandler().executeRead(
                "GetGlusterVolumeById", volumeRowMapper,
                createVolumeIdParams(id));
        fetchRelatedEntities(volume);
        return volume;
    }

    @Override
    public GlusterVolumeEntity getByName(Guid clusterId, String volName) {
        GlusterVolumeEntity volume = getCallsHandler().executeRead(
                "GetGlusterVolumeByName", volumeRowMapper,
                getCustomMapSqlParameterSource()
                        .addValue("cluster_id", clusterId.getUuid())
                        .addValue("vol_name", volName));

        fetchRelatedEntities(volume);
        return volume;
    }

    @Override
    public List<GlusterVolumeEntity> getByClusterId(Guid clusterId) {
        List<GlusterVolumeEntity> volumes =
                getCallsHandler().executeReadList("GetGlusterVolumesByClusterGuid",
                        volumeRowMapper,
                        getCustomMapSqlParameterSource().addValue("cluster_id", clusterId.getUuid()));
        fetchRelatedEntities(volumes);
        return volumes;
    }

    @Override
    public List<GlusterVolumeEntity> getAllWithQuery(String query) {
        List<GlusterVolumeEntity> volumes = new SimpleJdbcTemplate(jdbcTemplate).query(query, volumeRowMapper);
        fetchRelatedEntities(volumes);
        return volumes;
    }

    @Override
    public void remove(Guid id) {
        getCallsHandler().executeModification("DeleteGlusterVolumeByGuid",
                createVolumeIdParams(id));
    }

    @Override
    public void removeByName(Guid clusterId, String volName) {
        getCallsHandler().executeModification("DeleteGlusterVolumeByName",
                getCustomMapSqlParameterSource()
                        .addValue("cluster_id", clusterId.getUuid())
                        .addValue("vol_name", volName));
    }

    @Override
    public void updateVolumeStatus(Guid volumeId, GlusterVolumeStatus status) {
        getCallsHandler().executeModification("UpdateGlusterVolumeStatus",
                createVolumeIdParams(volumeId).addValue("status", EnumUtils.nameOrNull(status)));
    }

    @Override
    public void updateVolumeStatusByName(Guid clusterId, String volumeName, GlusterVolumeStatus status) {
        getCallsHandler().executeModification("UpdateGlusterVolumeStatusByName",
                getCustomMapSqlParameterSource()
                        .addValue("cluster_id", clusterId.getUuid())
                        .addValue("vol_name", volumeName)
                        .addValue("status", EnumUtils.nameOrNull(status)));
    }

    @Override
    public void addBrickToVolume(GlusterBrickEntity brick) {
        getCallsHandler().executeModification("InsertGlusterVolumeBrick", createBrickParams(brick));
    }

    @Override
    public void removeBrickFromVolume(GlusterBrickEntity brick) {
        getCallsHandler().executeModification("DeleteGlusterVolumeBrick",
                createVolumeIdParams(brick.getVolumeId())
                        .addValue("server_id", brick.getServerId().getUuid())
                        .addValue("brick_dir", brick.getBrickDirectory()));
    }

    @Override
    public void replaceVolumeBrick(GlusterBrickEntity oldBrick, GlusterBrickEntity newBrick) {
        getCallsHandler().executeModification("UpdateGlusterVolumeBrick",
                createVolumeIdParams(oldBrick.getVolumeId()).addValue("old_server_id", oldBrick.getServerId().getUuid())
                        .addValue("old_brick_dir", oldBrick.getBrickDirectory())
                        .addValue("new_server_id", newBrick.getServerId().getUuid())
                        .addValue("new_brick_dir", newBrick.getBrickDirectory())
                        .addValue("new_status", EnumUtils.nameOrNull(newBrick.getStatus())));
    }

    @Override
    public void updateBrickStatus(GlusterBrickEntity brick) {
        getCallsHandler().executeModification("UpdateGlusterVolumeBrickStatus", createBrickParams(brick));
    }

    @Override
    public void addVolumeOption(GlusterVolumeOption option) {
        getCallsHandler().executeModification("InsertGlusterVolumeOption", createVolumeOptionParams(option));
    }

    @Override
    public void updateVolumeOption(GlusterVolumeOption option) {
        getCallsHandler().executeModification("UpdateGlusterVolumeOption", createVolumeOptionParams(option));
    }

    @Override
    public void removeVolumeOption(GlusterVolumeOption option) {
        getCallsHandler().executeModification("DeleteGlusterVolumeOption",
                createVolumeIdParams(option.getVolumeId()).addValue("option_key", option.getKey()));
    }

    @Override
    public void addAccessProtocol(Guid volumeId, AccessProtocol protocol) {
        getCallsHandler().executeModification("InsertGlusterVolumeAccessProtocol",
                createAccessProtocolParams(volumeId, protocol));
    }

    @Override
    public void removeAccessProtocol(Guid volumeId, AccessProtocol protocol) {
        getCallsHandler().executeModification("DeleteGlusterVolumeAccessProtocol",
                createAccessProtocolParams(volumeId, protocol));
    }

    private List<GlusterBrickEntity> getBricksOfVolume(Guid volumeId) {
        List<GlusterBrickEntity> bricks = getCallsHandler().executeReadList(
                "GetBricksByGlusterVolumeGuid", brickRowMapper,
                createVolumeIdParams(volumeId));

        /**
         * Update the server name on each brick
         */
        for (GlusterBrickEntity brick : bricks) {
            brick.setServerName(getHostNameOfServer(brick.getServerId()));
        }

        return bricks;
    }

    private List<GlusterVolumeOption> getOptionsOfVolume(Guid volumeId) {
        return getCallsHandler().executeReadList(
                "GetOptionsByGlusterVolumeGuid", optionRowMapper,
                createVolumeIdParams(volumeId));
    }

    private List<AccessProtocol> getAccessProtocolsOfVolume(Guid volumeId) {
        return getCallsHandler().executeReadList(
                "GetAccessProtocolsByGlusterVolumeGuid",
                accessProtocolRowMapper,
                createVolumeIdParams(volumeId));
    }

    private MapSqlParameterSource createVolumeIdParams(Guid id) {
        return getCustomMapSqlParameterSource().addValue("volume_id", id.getUuid());
    }

    private MapSqlParameterSource createBrickParams(GlusterBrickEntity brick) {
        return createVolumeIdParams(brick.getVolumeId())
                .addValue("server_id", brick.getServerId().getUuid())
                .addValue("brick_dir", brick.getBrickDirectory())
                .addValue("status", EnumUtils.nameOrNull(brick.getStatus()));
    }

    private MapSqlParameterSource createVolumeOptionParams(GlusterVolumeOption option) {
        return createVolumeIdParams(option.getVolumeId())
                .addValue("option_key", option.getKey())
                .addValue("option_val", option.getValue());
    }

    private MapSqlParameterSource createAccessProtocolParams(Guid volumeId, AccessProtocol protocol) {
        return createVolumeIdParams(volumeId).addValue("access_protocol", EnumUtils.nameOrNull(protocol));
    }

    private void insertVolumeEntity(GlusterVolumeEntity volume) {
        getCallsHandler().executeModification(
                "InsertGlusterVolume",
                getCustomMapSqlParameterSource()
                        .addValue("id", volume.getId().getUuid())
                        .addValue("cluster_id", volume.getClusterId().getUuid())
                        .addValue("vol_name", volume.getName())
                        .addValue("vol_type", EnumUtils.nameOrNull(volume.getVolumeType()))
                        .addValue("transport_type", EnumUtils.nameOrNull(volume.getTransportType()))
                        .addValue("status", EnumUtils.nameOrNull(volume.getStatus()))
                        .addValue("replica_count", volume.getReplicaCount())
                        .addValue("stripe_count", volume.getStripeCount()));
    }

    private void insertVolumeBricks(GlusterVolumeEntity volume) {
        List<GlusterBrickEntity> bricks = volume.getBricks();
        for (GlusterBrickEntity brick : bricks) {
            if (brick.getVolumeId() == null) {
                brick.setVolumeId(volume.getId());
            }
            addBrickToVolume(brick);
        }
    }

    private void insertVolumeOptions(GlusterVolumeEntity volume) {
        Collection<GlusterVolumeOption> options = volume.getOptions();
        for (GlusterVolumeOption option : options) {
            if (option.getVolumeId() == null) {
                option.setVolumeId(volume.getId());
            }
            addVolumeOption(option);
        }
    }

    private void insertVolumeAccessProtocols(GlusterVolumeEntity volume) {
        for (AccessProtocol protocol : volume.getAccessProtocols()) {
            addAccessProtocol(volume.getId(), protocol);
        }
    }

    /**
     * Fetches and populates related entities like bricks, options, access protocols for the given volumes
     *
     * @param volumes
     */
    private void fetchRelatedEntities(List<GlusterVolumeEntity> volumes) {
        for (GlusterVolumeEntity volume : volumes) {
            fetchRelatedEntities(volume);
        }
    }

    /**
     * Fetches and populates related entities like bricks, options, access protocols for the given volume
     *
     * @param volume
     */
    private void fetchRelatedEntities(GlusterVolumeEntity volume) {
        if (volume != null) {
            volume.setBricks(getBricksOfVolume(volume.getId()));
            volume.setOptions(getOptionsOfVolume(volume.getId()));
            volume.setAccessProtocols(new HashSet<AccessProtocol>(getAccessProtocolsOfVolume(volume.getId())));
        }
    }

    private String getHostNameOfServer(Guid serverId) {
        return new SimpleJdbcTemplate(jdbcTemplate).queryForObject("select host_name from vds_static where vds_id = ?",
                String.class,
                serverId.getUuid());
    }

    private static final class GlusterVolumeRowMapper implements ParameterizedRowMapper<GlusterVolumeEntity> {
        @Override
        public GlusterVolumeEntity mapRow(ResultSet rs, int rowNum)
                throws SQLException {
            GlusterVolumeEntity entity = new GlusterVolumeEntity();
            entity.setId(Guid.createGuidFromString(rs.getString("id")));
            entity.setClusterId(Guid.createGuidFromString(rs
                    .getString("cluster_id")));
            entity.setName(rs.getString("vol_name"));
            entity.setVolumeType(GlusterVolumeType.valueOf(rs.getString("vol_type")));
            entity.setTransportType(TransportType.valueOf(rs.getString("transport_type")));
            entity.setStatus(GlusterVolumeStatus.valueOf(rs.getString("status")));
            entity.setReplicaCount(rs.getInt("replica_count"));
            entity.setStripeCount(rs.getInt("stripe_count"));
            return entity;
        }
    }

    /**
     * This is not a static class since it invokes a non-static method (getHostNameOfServer) of the parent class.
     */
    private final class GlusterBrickRowMapper implements ParameterizedRowMapper<GlusterBrickEntity> {
        @Override
        public GlusterBrickEntity mapRow(ResultSet rs, int rowNum)
                throws SQLException {
            GlusterBrickEntity brick = new GlusterBrickEntity();
            brick.setVolumeId(Guid.createGuidFromString(rs.getString("volume_id")));

            Guid serverId = Guid.createGuidFromString(rs.getString("server_id"));
            brick.setServerId(serverId);
            // Update the brick with server name. This is useful as the brick is typically represented in the form
            // serverName:brickDirectory though the database table (gluster_volume_bricks) stores just the server id
            brick.setServerName(getHostNameOfServer(serverId));

            brick.setBrickDirectory(rs.getString("brick_dir"));
            brick.setStatus(GlusterBrickStatus.valueOf(rs.getString("status")));
            return brick;
        }
    }

    private static final class VolumeOptionRowMapper implements ParameterizedRowMapper<GlusterVolumeOption> {
        @Override
        public GlusterVolumeOption mapRow(ResultSet rs, int rowNum)
                throws SQLException {
            GlusterVolumeOption option = new GlusterVolumeOption();
            option.setVolumeId(Guid.createGuidFromString(rs.getString("volume_id")));
            option.setKey(rs.getString("option_key"));
            option.setValue(rs.getString("option_val"));
            return option;
        }
    }

    private static final class AccessProtocolRowMapper implements ParameterizedRowMapper<AccessProtocol> {
        @Override
        public AccessProtocol mapRow(ResultSet rs, int rowNum)
                throws SQLException {
            return AccessProtocol.valueOf(rs.getString("access_protocol"));
        }
    }
}
