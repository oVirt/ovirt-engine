package org.ovirt.engine.core.dao.gluster;

import java.util.List;

import javax.inject.Named;
import javax.inject.Singleton;

import org.ovirt.engine.core.common.businessentities.gluster.GlusterServerService;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterServiceStatus;
import org.ovirt.engine.core.common.businessentities.gluster.ServiceType;
import org.ovirt.engine.core.common.utils.EnumUtils;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.MassOperationsGenericDao;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

/**
 * Implementation of the DB Facade for Services.
 */
@Named
@Singleton
@SuppressWarnings("deprecation")
public class GlusterServerServiceDaoImpl extends MassOperationsGenericDao<GlusterServerService, Guid> implements GlusterServerServiceDao {

    private static final RowMapper<GlusterServerService> serviceRowMapper = (rs, rowNum) -> {
        GlusterServerService entity = new GlusterServerService();
        entity.setId(getGuidDefaultEmpty(rs, "id"));
        entity.setServerId(getGuidDefaultEmpty(rs, "server_id"));
        entity.setServiceId(getGuidDefaultEmpty(rs, "service_id"));
        entity.setServiceName(rs.getString("service_name"));
        entity.setServiceType(ServiceType.valueOf(rs.getString("service_type")));
        entity.setMessage(rs.getString("message"));
        entity.setPid(rs.getInt("pid"));
        entity.setStatus(GlusterServiceStatus.valueOf(rs.getString("status")));
        entity.setHostName(rs.getString("vds_name"));
        return entity;
    };

    public GlusterServerServiceDaoImpl() {
        super("GlusterServerService");
    }

    @Override
    public List<GlusterServerService> getAllWithQuery(String query) {
        return new NamedParameterJdbcTemplate(getJdbcTemplate()).query(query, serviceRowMapper);
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
    public void updateByServerIdAndServiceType(GlusterServerService service) {
        getCallsHandler().executeModification("UpdateGlusterServerServiceByServerIdAndServiceType",
                getCustomMapSqlParameterSource()
                .addValue("server_id", service.getServerId())
                .addValue("service_id", service.getServiceId())
                .addValue("pid", service.getPid())
                .addValue("status", EnumUtils.nameOrNull(service.getStatus()))
                .addValue("message", service.getMessage()));
    }

    @Override
    protected RowMapper<GlusterServerService> createEntityRowMapper() {
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

    @Override
    public List<GlusterServerService> getByClusterIdAndServiceType(Guid clusterId, ServiceType serviceType) {
        MapSqlParameterSource paramSource = getCustomMapSqlParameterSource();
        paramSource.addValue("cluster_id", clusterId);
        paramSource.addValue("service_type", EnumUtils.nameOrNull(serviceType));

        return getCallsHandler().executeReadList("GetGlusterServerServicesByClusterIdAndServiceType",
                serviceRowMapper, paramSource);
    }

    @Override
    public List<GlusterServerService> getByServerIdAndServiceType(Guid serverId, ServiceType serviceType) {
        MapSqlParameterSource paramSource = getCustomMapSqlParameterSource();
        paramSource.addValue("server_id", serverId);
        paramSource.addValue("service_type", EnumUtils.nameOrNull(serviceType));

        return getCallsHandler().executeReadList("GetGlusterServerServicesByServerIdAndServiceType",
                serviceRowMapper, paramSource);
    }
}
