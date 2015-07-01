package org.ovirt.engine.core.dao.gluster;

import java.util.List;

import org.ovirt.engine.core.common.businessentities.gluster.GlusterService;
import org.ovirt.engine.core.common.businessentities.gluster.ServiceType;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.Dao;
import org.ovirt.engine.core.dao.ReadDao;

public interface GlusterServiceDao extends Dao, ReadDao<GlusterService, Guid> {
    /**
     * Returns list of all gluster services of given service type
     * @param type Service type whose services are to be returned
     * @return list of all gluster services of given service type
     */
    public List<GlusterService> getByServiceType(ServiceType type);

    /**
     * Returns the service having given service type and name
     * @param type Service type
     * @param name Service name
     * @return the service having given service type and name
     */
    public GlusterService getByServiceTypeAndName(ServiceType type, String name);
}
