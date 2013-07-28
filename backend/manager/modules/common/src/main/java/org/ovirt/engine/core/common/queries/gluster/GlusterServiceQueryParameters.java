package org.ovirt.engine.core.common.queries.gluster;

import org.ovirt.engine.core.common.businessentities.gluster.ServiceType;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.compat.Guid;

public class GlusterServiceQueryParameters extends IdQueryParameters {

    private static final long serialVersionUID = -7687304241216035729L;

    private ServiceType serviceType;

    public GlusterServiceQueryParameters() {
    }

    public GlusterServiceQueryParameters(Guid groupId) {
        super(groupId);
    }

    public GlusterServiceQueryParameters(Guid groupId, ServiceType serviceType) {
        super(groupId);
        this.serviceType = serviceType;
    }

    public ServiceType getServiceType() {
        return serviceType;
    }

    public void setServiceType(ServiceType serviceType) {
        this.serviceType = serviceType;
    }
}
