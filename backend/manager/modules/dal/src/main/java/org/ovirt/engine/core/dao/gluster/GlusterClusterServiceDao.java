package org.ovirt.engine.core.dao.gluster;

import java.util.List;

import org.ovirt.engine.core.common.businessentities.gluster.GlusterClusterService;
import org.ovirt.engine.core.common.businessentities.gluster.ServiceType;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.Dao;

/**
 * Interface for DB operations on Gluster Services (cluster-wide).
 */
public interface GlusterClusterServiceDao extends Dao {
    public List<GlusterClusterService> getByClusterId(Guid clusterId);

    public GlusterClusterService getByClusterIdAndServiceType(Guid clusterId, ServiceType serviceType);

    public void save(GlusterClusterService service);

    public void update(GlusterClusterService service);
}
