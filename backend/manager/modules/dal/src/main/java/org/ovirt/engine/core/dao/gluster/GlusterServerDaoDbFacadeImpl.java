package org.ovirt.engine.core.dao.gluster;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterServer;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.DefaultGenericDaoDbFacade;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;

public class GlusterServerDaoDbFacadeImpl extends DefaultGenericDaoDbFacade<GlusterServer, Guid> implements GlusterServerDao {

    private static final RowMapper<GlusterServer> glusterServerRowMapper = new GlusterServerRowMapper();

    public GlusterServerDaoDbFacadeImpl() {
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

    private static final class GlusterServerRowMapper implements RowMapper<GlusterServer> {
        @Override
        public GlusterServer mapRow(ResultSet rs, int rowNum)
                throws SQLException {
            GlusterServer glusterServer = new GlusterServer();
            glusterServer.setId(getGuidDefaultEmpty(rs, "server_id"));
            glusterServer.setGlusterServerUuid(getGuidDefaultEmpty(rs, "gluster_server_uuid"));
            String knownAddresses = rs.getString("known_addresses");
            if (StringUtils.isNotBlank(knownAddresses)) {
                String[] knownAddressArray = knownAddresses.split(",");
                ArrayList<String> knownAddressList = new ArrayList<>();
                for (String addr : knownAddressArray) {
                    knownAddressList.add(addr);
                }
                glusterServer.setKnownAddresses(knownAddressList);
            }
            return glusterServer;
        }
    }

    @Override
    protected MapSqlParameterSource createFullParametersMapper(GlusterServer entity) {
        return createIdParameterMapper(entity.getId())
                .addValue("gluster_server_uuid", entity.getGlusterServerUuid());
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
}
