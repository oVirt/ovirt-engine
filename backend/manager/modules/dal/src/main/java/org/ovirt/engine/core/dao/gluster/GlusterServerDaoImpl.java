package org.ovirt.engine.core.dao.gluster;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.inject.Named;
import javax.inject.Singleton;

import org.apache.commons.lang.StringUtils;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterServer;
import org.ovirt.engine.core.common.businessentities.gluster.PeerStatus;
import org.ovirt.engine.core.common.utils.EnumUtils;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.DefaultGenericDao;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;

@Named
@Singleton
public class GlusterServerDaoImpl extends DefaultGenericDao<GlusterServer, Guid> implements GlusterServerDao {

    private static final RowMapper<GlusterServer> glusterServerRowMapper = (rs, rowNum) -> {
        GlusterServer glusterServer = new GlusterServer();
        glusterServer.setId(getGuidDefaultEmpty(rs, "server_id"));
        glusterServer.setGlusterServerUuid(getGuidDefaultEmpty(rs, "gluster_server_uuid"));
        glusterServer.setPeerStatus(PeerStatus.valueOf(rs.getString("peer_status")));
        String knownAddresses = rs.getString("known_addresses");
        if (StringUtils.isNotBlank(knownAddresses)) {
            String[] knownAddressArray = knownAddresses.split(",");
            glusterServer.setKnownAddresses(new ArrayList<>(Arrays.asList(knownAddressArray)));
        }
        return glusterServer;
    };

    public GlusterServerDaoImpl() {
        super("GlusterServer");
        setProcedureNameForGet("GetGlusterServerByServerId");
    }

    @Override
    public GlusterServer getByServerId(Guid id) {
        return getCallsHandler().executeRead("GetGlusterServerByServerId",
                glusterServerRowMapper,
                createIdParameterMapper(id));
    }

    @Override
    public GlusterServer getByGlusterServerUuid(Guid glusterServerUuid) {
        return getCallsHandler().executeRead("GetGlusterServerByGlusterServerUUID",
                glusterServerRowMapper,
                getCustomMapSqlParameterSource().addValue("gluster_server_uuid", glusterServerUuid));
    }

    @Override
    public void removeByGlusterServerUuid(Guid glusterServerUuid) {
        getCallsHandler().executeModification("DeleteGlusterServerByGlusterServerUUID",
                getCustomMapSqlParameterSource().addValue("gluster_server_uuid", glusterServerUuid));
    }

    @Override
    protected MapSqlParameterSource createFullParametersMapper(GlusterServer entity) {
        return createIdParameterMapper(entity.getId())
                .addValue("gluster_server_uuid", entity.getGlusterServerUuid())
                .addValue("peer_status", EnumUtils.nameOrNull(entity.getPeerStatus()));
    }

    @Override
    protected MapSqlParameterSource createIdParameterMapper(Guid id) {
        return getCustomMapSqlParameterSource().addValue("server_id", id);
    }

    @Override
    protected RowMapper<GlusterServer> createEntityRowMapper() {
        return glusterServerRowMapper;
    }

    @Override
    public void addKnownAddress(Guid serverId, String address) {
        getCallsHandler().executeModification("AddGlusterServerKnownAddress",
                getCustomMapSqlParameterSource().addValue("server_id", serverId)
                        .addValue("known_address", address));
    }

    @Override
    public void updateKnownAddresses(Guid serverId, List<String> addresses) {
        getCallsHandler().executeModification("UpdateGlusterServerKnownAddresses",
                getCustomMapSqlParameterSource().addValue("server_id", serverId)
                        .addValue("known_addresses", StringUtils.join(addresses, ",")));
    }

    @Override
    public void updatePeerStatus(Guid serverId, PeerStatus peerStatus) {
        getCallsHandler().executeModification("UpdateGlusterServerPeerStatus",
                getCustomMapSqlParameterSource().addValue("server_id", serverId)
                        .addValue("peer_status", EnumUtils.nameOrNull(peerStatus)));
    }
}
