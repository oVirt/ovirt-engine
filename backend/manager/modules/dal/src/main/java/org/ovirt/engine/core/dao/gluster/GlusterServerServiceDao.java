package org.ovirt.engine.core.dao.gluster;

import java.util.List;

import org.ovirt.engine.core.common.businessentities.gluster.GlusterServerService;
import org.ovirt.engine.core.common.businessentities.gluster.ServiceType;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.Dao;
import org.ovirt.engine.core.dao.MassOperationsDao;
import org.ovirt.engine.core.dao.ReadDao;
import org.ovirt.engine.core.dao.SearchDao;

/**
 * Interface for DB operations on Gluster Services (server level).
 */
public interface GlusterServerServiceDao extends Dao, SearchDao<GlusterServerService>, MassOperationsDao<GlusterServerService, Guid>, ReadDao<GlusterServerService, Guid> {
    public List<GlusterServerService> getByClusterId(Guid clusterId);

    public List<GlusterServerService> getByServerId(Guid serverId);

    public List<GlusterServerService> getByClusterIdAndServiceType(Guid clusterId, ServiceType serviceType);

    public List<GlusterServerService> getByServerIdAndServiceType(Guid serverId, ServiceType serviceType);

    @Override
    public List<GlusterServerService> getAllWithQuery(String query);

    public void save(GlusterServerService service);

    public void update(GlusterServerService service);

    public void updateByServerIdAndServiceType(GlusterServerService service);
}
