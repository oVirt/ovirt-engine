package org.ovirt.engine.core.dao.gluster;

import java.sql.ResultSet;
import java.sql.SQLException;

import javax.inject.Named;

import org.ovirt.engine.core.common.businessentities.gluster.GlusterServer;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.DefaultGenericDaoDbFacade;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;

@Named
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
}
