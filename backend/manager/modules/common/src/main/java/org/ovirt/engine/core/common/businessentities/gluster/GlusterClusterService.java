package org.ovirt.engine.core.common.businessentities.gluster;

import java.io.Serializable;

import org.ovirt.engine.core.compat.Guid;

/**
 * This class represents a cluster-wide service (type). If a service type is enabled on a services, all the services of
 * that type should be in RUNNING state, and vice versa.
 */
public class GlusterClusterService implements Serializable {

    private static final long serialVersionUID = 8971469111782110647L;

    private ServiceType serviceType;
    private Guid clusterId;
    private GlusterServiceStatus status;

    public ServiceType getServiceType() {
        return serviceType;
    }

    public void setServiceType(ServiceType serviceType) {
        this.serviceType = serviceType;
    }

    public GlusterServiceStatus getStatus() {
        return status;
    }

    public void setStatus(GlusterServiceStatus status) {
        this.status = status;
    }

    public Guid getClusterId() {
        return clusterId;
    }

    public void setClusterId(Guid clusterId) {
        this.clusterId = clusterId;
    }
}
