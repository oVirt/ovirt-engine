package org.ovirt.engine.core.common.businessentities.gluster;

import java.util.Objects;

import org.ovirt.engine.core.common.businessentities.BusinessEntity;
import org.ovirt.engine.core.common.businessentities.Queryable;
import org.ovirt.engine.core.compat.Guid;

public class GlusterService implements Queryable, BusinessEntity<Guid> {
    private static final long serialVersionUID = 2313305635486777212L;

    private Guid id;
    private ServiceType serviceType;
    private String serviceName;

    @Override
    public Guid getId() {
        return id;
    }

    @Override
    public void setId(Guid id) {
        this.id = id;
    }

    @Override
    public Object getQueryableId() {
        return getId();
    }

    public ServiceType getServiceType() {
        return serviceType;
    }

    public void setServiceType(ServiceType serviceType) {
        this.serviceType = serviceType;
    }

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof GlusterService)) {
            return false;
        }

        GlusterService other = (GlusterService) obj;
        return Objects.equals(id, other.id)
                && serviceType == other.serviceType
                && Objects.equals(serviceName, other.serviceName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(
                id,
                serviceType,
                serviceName
        );
    }
}
