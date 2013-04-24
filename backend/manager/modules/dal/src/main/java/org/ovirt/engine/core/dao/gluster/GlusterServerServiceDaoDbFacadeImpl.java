package org.ovirt.engine.core.dao.gluster;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import org.ovirt.engine.core.common.businessentities.gluster.GlusterServerService;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterServiceStatus;
import org.ovirt.engine.core.common.utils.EnumUtils;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.MassOperationsGenericDaoDbFacade;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.simple.ParameterizedRowMapper;
import org.springframework.jdbc.core.simple.SimpleJdbcTemplate;

/**
 * Implementation of the DB Facade for Services.
 */
@SuppressWarnings("deprecation")
public class GlusterServerServiceDaoDbFacadeImpl extends MassOperationsGenericDaoDbFacade<GlusterServerService, Guid> implements GlusterServerServiceDao {

    private static final ParameterizedRowMapper<GlusterServerService> serviceRowMapper =
            new GlusterServerServiceRowMapper();

    public GlusterServerServiceDaoDbFacadeImpl() {
        super("GlusterServerService");
    }

    @Override
    public List<GlusterServerService> getAllWithQuery(String query) {
        return new SimpleJdbcTemplate(jdbcTemplate).query(query, serviceRowMapper);
    }

    @Override
    protected MapSqlParameterSource createFullParametersMapper(GlusterServerService service) {
        return getCustomMapSqlParameterSource()
                .addValue("id", service.getId())
                .addValue("server_id", service.getServerId())
                .addValue("service_id", service.getServiceId())
                .addValue("pid", service.getPid())
                .addValue("status", EnumUtils.nameOrNull(service.getStatus()))
                .addValue("message", service.getMessage());
    }

    @Override
    protected ParameterizedRowMapper<GlusterServerService> createEntityRowMapper() {
        return serviceRowMapper;
    }

    @Override
    protected MapSqlParameterSource createIdParameterMapper(Guid id) {
        return getCustomMapSqlParameterSource().addValue("id", id);
    }

    @Override
    public List<GlusterServerService> getByClusterId(Guid clusterId) {
        return getCallsHandler().executeReadList("GetGlusterServerServicesByClusterId",
                serviceRowMapper, getCustomMapSqlParameterSource().addValue("cluster_id", clusterId));
    }

    @Override
    public List<GlusterServerService> getByServerId(Guid serverId) {
        return getCallsHandler().executeReadList("GetGlusterServerServicesByServerId",
                serviceRowMapper, getCustomMapSqlParameterSource().addValue("server_id", serverId));
    }

    private static final class GlusterServerServiceRowMapper implements ParameterizedRowMapper<GlusterServerService> {
        @Override
        public GlusterServerService mapRow(ResultSet rs, int rowNum)
                throws SQLException {
            GlusterServerService entity = new GlusterServerService();
            entity.setId(Guid.createGuidFromString(rs.getString("id")));
            entity.setServerId(Guid.createGuidFromString(rs.getString("server_id")));
            entity.setServiceId(Guid.createGuidFromString(rs.getString("service_id")));
            entity.setMessage(rs.getString("message"));
            entity.setPid(rs.getInt("pid"));
            entity.setStatus(GlusterServiceStatus.valueOf(rs.getString("status")));
            return entity;
        }
    }
}
