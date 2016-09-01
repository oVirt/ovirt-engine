package org.ovirt.engine.core.dao.gluster;

import java.util.List;

import javax.inject.Named;
import javax.inject.Singleton;

import org.ovirt.engine.core.common.businessentities.gluster.GlusterClusterService;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterServiceStatus;
import org.ovirt.engine.core.common.businessentities.gluster.ServiceType;
import org.ovirt.engine.core.common.utils.EnumUtils;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.BaseDao;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;

@Named
@Singleton
public class GlusterClusterServiceDaoImpl extends BaseDao implements GlusterClusterServiceDao {
    private static final RowMapper<GlusterClusterService> serviceRowMapper = (rs, rowNum) -> {
        GlusterClusterService entity = new GlusterClusterService();
        entity.setClusterId(getGuidDefaultEmpty(rs, "cluster_id"));
        entity.setServiceType(ServiceType.valueOf(rs.getString("service_type")));
        entity.setStatus(GlusterServiceStatus.valueOf(rs.getString("status")));
        return entity;
    };

    @Override
    public List<GlusterClusterService> getByClusterId(Guid clusterId) {
        return getCallsHandler().executeReadList("GetGlusterClusterServicesByClusterId",
                serviceRowMapper, getCustomMapSqlParameterSource().addValue("cluster_id", clusterId));
    }

    @Override
    public GlusterClusterService getByClusterIdAndServiceType(Guid clusterId, ServiceType serviceType) {
        return getCallsHandler().executeRead("GetGlusterClusterServicesByClusterIdAndServiceType",
                serviceRowMapper,
                getCustomMapSqlParameterSource().
                        addValue("cluster_id", clusterId)
                        .addValue("service_type", EnumUtils.nameOrNull(serviceType)));
    }

    @Override
    public void save(GlusterClusterService service) {
        getCallsHandler().executeModification("InsertGlusterClusterService", createFullParametersMapper(service));
    }

    @Override
    public void update(GlusterClusterService service) {
        getCallsHandler().executeModification("UpdateGlusterClusterService", createFullParametersMapper(service));
    }

    private MapSqlParameterSource createFullParametersMapper(GlusterClusterService service) {
        return getCustomMapSqlParameterSource()
                .addValue("cluster_id", service.getClusterId())
                .addValue("service_type", EnumUtils.nameOrNull(service.getServiceType()))
                .addValue("status", EnumUtils.nameOrNull(service.getStatus()));
    }
}
